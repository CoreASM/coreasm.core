/*	
 * LetRulePlugin.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 George Ma
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.letrule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.letrule.CompilerLetRulePlugin;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.turboasm.TurboASMPlugin;

/** 
 *	Plugin for let rule
 *   
 *  @author  George Ma
 *  
 */
public class LetRulePlugin extends Plugin implements ParserPlugin, InterpreterPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 9, 1, "");
	   
	public static final String PLUGIN_NAME = LetRulePlugin.class.getSimpleName();
	
	private Map<String, GrammarRule> parsers = null;

	private final String[] keywords = {"let", "in"};
	private final String[] operators = {"=", ",", "{", "[", "]", "}"};
	
	private final CompilerPlugin compilerPlugin = new CompilerLetRulePlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	/* (non-Javadoc)
     * @see org.coreasm.engine.Plugin#interpret(org.coreasm.engine.interpreter.Node)
     */
    public ASTNode interpret(Interpreter interpreter, ASTNode pos) {
        if (pos instanceof LetRuleNode) {
           LetRuleNode letNode = (LetRuleNode) (pos);
           Map<String, ASTNode> variableMap = null;
           AbstractStorage storage = capi.getStorage();

           try {
               variableMap = letNode.getVariableMap();
           } 
           catch (Exception e) {
               capi.error(e.getMessage(), pos, interpreter);
               return pos;
           }
           
           // evaluate all the terms that will be aliased
           if (!letNode.isLetResultRule()) {
        	   for (ASTNode n :variableMap.values()) {
                   if (!n.isEvaluated())
                       return n;
               }
           }
           else {
	           for (ASTNode n :variableMap.values()) {
	               if (!n.isEvaluated()) {
            		   ASTNode loc = (ASTNode)n.cloneTree();
            		   loc.getFirst().setToken("-");
            		   FunctionRuleTermNode rule = (FunctionRuleTermNode)n;

            		   // If the rule part is of the form 'x' or 'x(...)'
            		   if (rule.hasName() && storage.isRuleName(rule.getName())) {
            			   String x = rule.getName();
            			   // If the rule part is of the form 'x' with no arguments
            			   if (!rule.hasArguments())
            				   pos = ruleCallWithResult(interpreter, storage.getRule(x), null, loc, pos);
            			   else // if the rule part 'x(...)' (with arguments)
            				   pos = ruleCallWithResult(interpreter, storage.getRule(x), rule.getArguments(), loc, pos);
            			   if (!pos.isEvaluated())
            				   return pos;
            			   Set<Update> aggregatedUpdate = storage.performAggregation(pos.getUpdates());
            			   pos.setNode(null, null, null);
    	    			   if (storage.isConsistent(aggregatedUpdate)) {
    	    				   UpdateMultiset newUpdates = new UpdateMultiset();
    	    				   storage.pushState();
    	    				   storage.apply(aggregatedUpdate);
    	    				   Element value = null;
    	    				   for (Update u: aggregatedUpdate) {
    	    					   if ("-".equals(u.loc.name)) {
    	    						   try {
    	    							   value = storage.getValue(u.loc);
    	    						   } catch (InvalidLocationException e) {
    	    						   }
    	    					   }
    	    					   else
    	    						   newUpdates.add(u);
    	    				   }
    	    				   storage.popState();
    	    				   n.setNode(n.getLocation(), newUpdates, value);
    	    				   return letNode;
    	    			   }
            		   }
            	   }
	           }
           }
           
           if (!letNode.getInRule().isEvaluated()) {
               // add the aliased variables to the environment
               for (String v: variableMap.keySet()) {
                   interpreter.addEnv(v,variableMap.get(v).getValue());
               }
               
               // evaluate the rule
               return letNode.getInRule();
           }
           else {
        	   UpdateMultiset updates = letNode.getInRule().getUpdates();
               // remove the aliased variables from the environment
               for (String v: variableMap.keySet()) {
            	   updates.addAll(variableMap.get(v).getUpdates());
                   interpreter.removeEnv(v);
               }
               
               // get the updates
               pos.setNode(null,updates,null);
               return pos;
               
           }
        }
        
        return pos;
    }

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}
	

	/**
	 * @return <code>null</code>
	 */
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}

    public Map<String, GrammarRule> getParsers() {
    	if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			
			Parser<Object[]> letTermParser = pTools.csplus(pTools.seq(
					idParser,
					pTools.getOprParser("="),
					termParser
					));

			Parser<Node> letRuleParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("let", PLUGIN_NAME),
					Parsers.or(	pTools.seq(pTools.getOprParser("{"), letTermParser, pTools.getOprParser("}")),
								pTools.seq(pTools.getOprParser("["), letTermParser, pTools.getOprParser("]")),
								letTermParser),
					pTools.getKeywParser("in", PLUGIN_NAME),
					ruleParser
					}).map(
					new LetRuleParseMap());
			parsers.put("Rule",	
					new GrammarRule("LetRule", 
							"'let' ID '=' Term (',' ID '=' Term )* 'in' Rule", 
							letRuleParser, PLUGIN_NAME));
			
			Parser<Object[]> letResultTermParser = pTools.csplus(pTools.seq(
					idParser,
					pTools.getOprParser(TurboASMPlugin.RETURN_RESULT_TOKEN),
					termParser
					));

			Parser<Node> letResultRuleParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("let", PLUGIN_NAME),
					Parsers.or(	pTools.seq(pTools.getOprParser("{"), letResultTermParser, pTools.getOprParser("}")),
								pTools.seq(pTools.getOprParser("["), letResultTermParser, pTools.getOprParser("]")),
								letResultTermParser),
					pTools.getKeywParser("in", PLUGIN_NAME),
					ruleParser
					}).map(
					new LetRuleParseMap());
			parsers.put("Rule",	
					new GrammarRule("LetResultRule", 
							"'let' ID '<-' Term (',' ID '<-' Term )* 'in' Rule", 
							letResultRuleParser, PLUGIN_NAME));
    	}
    	
    	return parsers;
    }
    
    /**
	 * Handles a call to a rule that has <b>result</b>.
	 * 
	 * @param name rule name
	 * @param args arguments
	 * @param pos current node being interpreted
	 */
	private ASTNode ruleCallWithResult(Interpreter interpreter, RuleElement rule, List<ASTNode> args, ASTNode loc, ASTNode pos) {
		
		List<String> exParams = new ArrayList<String>(rule.getParam());
		List<ASTNode> exArgs = new ArrayList<ASTNode>();
		if (args != null)
			exArgs.addAll(args);
		exArgs.add(loc);
		exParams.add(TurboASMPlugin.RESULT_KEYWORD);
		
		return interpreter.ruleCall(rule, exParams, exArgs, pos);
	}
    
    /* (non-Javadoc)
     * @see org.coreasm.engine.Plugin#initialize()
     */
    @Override
    public void initialize() {
        
    }

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	
	public static class LetRuleParseMap //extends ParseMapN<Node> {
	extends ParserTools.ArrayParseMap {
	
		public LetRuleParseMap() {
			super(PLUGIN_NAME);
		}

		String nextChildName = "alpha";
		
		public Node map(Object[] vals) {
			nextChildName = "alpha";
			LetRuleNode node = new LetRuleNode(((Node)vals[0]).getScannerInfo());
			if (vals[1] instanceof Object[] && ((Object[])vals[1])[0] instanceof Node) {
				Node n = ((Node)((Object[])vals[1])[0]);
				if ("[".equals(n.getToken()))
					addLetChildren(node, unpackChildren(new ArrayList<Node>(), vals));
				else
					addChildren(node, vals);
			}
			else
				addChildren(node, vals);
			return node;
		}
		
		private List<Node> unpackChildren(List<Node> nodes, Object[] vals) {
			for (Object child: vals) {
				if (child != null) {
					if (child instanceof Object[])
						unpackChildren(nodes, (Object[])child);
					else
						if (child instanceof Node)
							nodes.add((Node)child);
				}
			}
			return nodes;
		}
		
		private void addLetChildren(LetRuleNode root, List<Node> children) {
			for (Node child: children) {
				if (child instanceof ASTNode) {
					if (!"alpha".equals(nextChildName) || root.getFirst() == null)
						addChild(root, child);
					else {
						LetRuleNode newRoot = new LetRuleNode(child.getScannerInfo());
						addChild(newRoot, child);
						nextChildName = "gamma";
						addChild(root, newRoot);
						root = newRoot;
					}
				} else
					addChild(root, child);
			}
		}

		@Override
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode) {
				parent.addChild(nextChildName, child);
			} else {
				parent.addChild(child);
				if (child.getToken().equals("=") || child.getToken().equals(TurboASMPlugin.RETURN_RESULT_TOKEN))				// Term
					nextChildName = "beta";
				else
					if (child.getToken().equals(","))			// ID
						nextChildName = "alpha";
					else
						if (child.getToken().equals("in"))		// Rule
							nextChildName = "gamma";
			}
		}
		
	}
}
