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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.letrule.CompilerLetRulePlugin;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.parser.ParseMapN;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;

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
	private final String[] operators = {"=", ","};
	
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
            
           try {
               variableMap = letNode.getVariableMap();
           } 
           catch (Exception e) {
               capi.error(e.getMessage(), pos, interpreter);
               return pos;
           }
                           
           // evaluate all the terms that will be aliased
           for (ASTNode n :variableMap.values()) {
               if (!n.isEvaluated()) {
                   return n;
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
               // remove the aliased variables from the environment
               for (String v: variableMap.keySet()) {
                   interpreter.removeEnv(v);
               }
               
               // get the updates
               pos.setNode(null,letNode.getInRule().getUpdates(),null);
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

			Parser<Node> letRuleParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("let", PLUGIN_NAME),
					pTools.csplus(pTools.seq(
							idParser,
							pTools.getOprParser("="),
							termParser
							)),
					pTools.getKeywParser("in", PLUGIN_NAME),
					ruleParser
					}).map(
					new LetRuleParseMap());
			parsers.put("Rule",	
					new GrammarRule("LetRule", 
							"'let' ID '=' Term (',' ID '=' Term )* 'in' Rule", 
							letRuleParser, PLUGIN_NAME));
    	}
    	
    	return parsers;
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
		
		public Node map(Object... vals) {
			nextChildName = "alpha";
			Node node = new LetRuleNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}

		@Override
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode) {
				parent.addChild(nextChildName, child);
			} else {
				parent.addChild(child);
				if (child.getToken().equals("="))				// Term
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
