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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.letrule.CompilerLetRulePlugin;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
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
	
	private ThreadLocal<Map<Node, Map<Node, LetResultChildNode>>> letResultChildNodes;
	
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
	           for (Entry<String, ASTNode> entry :variableMap.entrySet()) {
	        	   ASTNode n = entry.getValue();
	               if (!n.isEvaluated()) {
            		   ASTNode loc = (ASTNode)n.cloneTree();
            		   loc.getFirst().setToken("-" + entry.getKey());
            		   while (loc.getFirst().getNextCSTNode() != null)	// Remove arguments from copy
            			   loc.getFirst().getNextCSTNode().removeFromTree();
            		   FunctionRuleTermNode rule = (FunctionRuleTermNode)n;

            		   // If the rule part is of the form 'x' or 'x(...)'
            		   if (rule.hasName() && storage.isRuleName(rule.getName())) {
            			   String x = rule.getName();
            			   // If the rule part is of the form 'x' with no arguments
            			   if (!rule.hasArguments())
            				   pos = ruleCallWithResult(interpreter, storage.getRule(x), null, loc, getLetResultChildNodes(letNode, n));
            			   else // if the rule part 'x(...)' (with arguments)
            				   pos = ruleCallWithResult(interpreter, storage.getRule(x), rule.getArguments(), loc, getLetResultChildNodes(letNode, n));
            			   if (!pos.isEvaluated())
            				   return pos;
	    				   UpdateMultiset newUpdates = new UpdateMultiset();
	    				   Element value = null;
	    				   for (Update u: pos.getUpdates()) {
	    					   if (("-" + entry.getKey()).equals(u.loc.name))
    							   value = u.value;
	    					   else
	    						   newUpdates.add(u);
	    				   }
	    				   if (value == null) {
	    					   value = Element.UNDEF;
	    					   capi.warning(PLUGIN_NAME, "result hasn't been set by the rule " + x + ".", n, interpreter);
	    				   }
	    				   pos.setNode(null, null, null);	// The updates got stored into pos by ruleCallWithResult but they should be stored in n instead
	    				   n.setNode(n.getLocation(), newUpdates, value);
	    				   return letNode;
            		   }
            	   }
	           }
           }
           
           if (!letNode.getInRule().isEvaluated()) {
        	   clearLetResultChildNodes(letNode);
        	   UpdateMultiset updates = new UpdateMultiset();
               for (String v: variableMap.keySet()) {
            	   updates = storage.compose(updates, variableMap.get(v).getUpdates());
                   interpreter.addEnv(v,variableMap.get(v).getValue());
               }
               
               try {
            	   Set<Update> aggregatedUpdate = storage.performAggregation(updates);
            	   if (storage.isConsistent(aggregatedUpdate)) {
            		   storage.pushState();
            		   storage.apply(aggregatedUpdate);
            		   return letNode.getInRule();
            	   }
            	   else
            		   throw new EngineError();
               } catch (EngineError e) {
            	   capi.warning(PLUGIN_NAME, "TurboASM Plugin: Inconsistent updates computed in sequence. Leaving the sequence", letNode.getInRule(), interpreter);
            	   pos.setNode(null, updates, null);
               }
               
               return pos;
           }
           else {
        	   UpdateMultiset composed = new UpdateMultiset();
               for (String v: variableMap.keySet()) {
            	   composed = storage.compose(composed, variableMap.get(v).getUpdates());
                   interpreter.removeEnv(v);
               }
               
               composed = storage.compose(composed, letNode.getInRule().getUpdates());
               storage.popState();
               pos.setNode(null,composed,null);
               return pos;
           }
        }
        if (pos instanceof LetResultChildNode)
        	return pos.getParent();
        
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
			Parser<Node> funcRuleTermParser = kernel.getFunctionRuleTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			
			Parser<Object[]> letTermParser = pTools.csplus(pTools.seq(
					idParser,
					pTools.getOprParser("="),
					termParser
					));
			
			Parser<Object[]> letResultTermParser = pTools.csplus(pTools.seq(
					idParser,
					pTools.getOprParser(TurboASMPlugin.RETURN_RESULT_TOKEN),
					funcRuleTermParser
					));

			Parser<Node> letRuleParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("let", PLUGIN_NAME),
					Parsers.or(	pTools.seq(pTools.getOprParser("{"), Parsers.or(letTermParser, letResultTermParser), pTools.getOprParser("}")),
								pTools.seq(pTools.getOprParser("["), Parsers.or(letTermParser, letResultTermParser), pTools.getOprParser("]")),
								Parsers.or(letTermParser, letResultTermParser)),
					pTools.getKeywParser("in", PLUGIN_NAME),
					ruleParser
					}).map(
					new LetRuleParseMap());
			
			parsers.put("Rule",	
					new GrammarRule("LetRule", 
							"'let' ID ('=' | '<-') Term (',' ID '<-' Term )* 'in' Rule", 
							letRuleParser, PLUGIN_NAME));
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
        letResultChildNodes = new ThreadLocal<Map<Node, Map<Node, LetResultChildNode>>>() {
			@Override
			protected Map<Node, Map<Node, LetResultChildNode>> initialValue() {
				return new IdentityHashMap<Node, Map<Node, LetResultChildNode>>();
			}
		};
    }
    
    private LetResultChildNode getLetResultChildNodes(LetRuleNode letNode, Node node) {
    	Map<Node, Map<Node, LetResultChildNode>> allLetResultChildNodes = this.letResultChildNodes.get();
    	Map<Node, LetResultChildNode> letResultChildNodes = allLetResultChildNodes.get(letNode);
    	if (letResultChildNodes == null) {
    		letResultChildNodes = new IdentityHashMap<Node, LetResultChildNode>();
    		allLetResultChildNodes.put(letNode, letResultChildNodes);
    	}
    	LetResultChildNode letResultChildNode = letResultChildNodes.get(node);
    	if (letResultChildNode == null) {
    		letResultChildNode = new LetResultChildNode(letNode);
    		letResultChildNodes.put(node, letResultChildNode);
    	}
    	return letResultChildNode;
    }
    
    private void clearLetResultChildNodes(LetRuleNode letNode) {
    	Map<Node, LetResultChildNode> letResultChildNodes = this.letResultChildNodes.get().get(letNode);
    	if (letResultChildNodes != null) {
    		letResultChildNodes.clear();
    		this.letResultChildNodes.get().remove(letNode);
    	}
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
