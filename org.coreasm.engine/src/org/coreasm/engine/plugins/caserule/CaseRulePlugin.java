/*	
 * CaseRulePlugin.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.caserule;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.caserule.CompilerCaseRulePlugin;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;

/** 
 *	Plugin for case rule.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class CaseRulePlugin extends Plugin 
    implements ParserPlugin, InterpreterPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 1, 1, "beta");
	
	public static final String PLUGIN_NAME = CaseRulePlugin.class.getSimpleName();
	
	public static final String CASE_ITEM_RULE_DELIMITER = ":";

	private final String[] keywords = {"case", "of", "endcase"};
	private final String[] operators = {CASE_ITEM_RULE_DELIMITER};
	
    private Map<String, GrammarRule> parsers = null;
    private ThreadLocal<Map<Node,Set<ASTNode>>> matchingRules;

    private final CompilerPlugin compilerPlugin = new CompilerCaseRulePlugin();
    
    @Override
    public void initialize() {
        matchingRules = new ThreadLocal<Map<Node, Set<ASTNode>>>() {
			@Override
			protected Map<Node, Set<ASTNode>> initialValue() {
				return new IdentityHashMap<Node, Set<ASTNode>>();
			}
        };
    }

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
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
			KernelServices kernel = (KernelServices)capi.getPlugin("Kernel").getPluginInterface();
			
			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> termParser = kernel.getTermParser();
			
			ParserTools pTools = ParserTools.getInstance(capi);
			
			Parser<Node> caseRuleParser = Parsers.array(
				new Parser[] {
					pTools.getKeywParser("case", PLUGIN_NAME),
					termParser,
					pTools.getKeywParser("of", PLUGIN_NAME),
					pTools.plus(
					pTools.seq(
						termParser,
						pTools.getOprParser(CASE_ITEM_RULE_DELIMITER),
						ruleParser)
					),
					pTools.getKeywParser("endcase", PLUGIN_NAME)
				}).map(
				new CaseParseMap());
			
				parsers.put("Rule",
					new GrammarRule("CaseRule",
							"'case' Term 'of' (Term '" + CASE_ITEM_RULE_DELIMITER + "' Rule)+ 'endcase'", 
							caseRuleParser, PLUGIN_NAME));
		}
		return parsers;
	}

	/* (non-Javadoc)
     * @see org.coreasm.engine.Plugin#interpret(org.coreasm.engine.interpreter.Node)
     */
    public ASTNode interpret(Interpreter interpreter, ASTNode pos) {
       
        if (pos instanceof CaseRuleNode) {
            CaseRuleNode caseNode = (CaseRuleNode) pos;
            
            if (!caseNode.getCaseTerm().isEvaluated()) {
            	// clear the cache of the rules whose guard 
            	// will match the value of the case term
            	matchingRules.get().put(caseNode, new HashSet<ASTNode>());
            	// return the case term for evaluation
            	return caseNode.getCaseTerm();
            } else {
            	Map<ASTNode, ASTNode> caseMap = new IdentityHashMap<ASTNode, ASTNode>();
            	caseMap = caseNode.getCaseMap();
            	
            	// evaluate all case guards
            	for (ASTNode guard: caseMap.keySet()) {
            		if (!guard.isEvaluated())
            			return guard;
            	}
            	
            	// At this point, all guards are evaluated
            	// It's time to evaluate rules with a matching guard
            	for (Entry<ASTNode, ASTNode> pair: caseMap.entrySet()) {
            		Element value = pair.getKey().getValue();
            		if (value == null) {
            			capi.error("Case guard does not have a value.", pair.getKey(), interpreter);
            			return pos;
            		}
        			if (!pair.getValue().isEvaluated()) 
        				if (value.equals(caseNode.getCaseTerm().getValue())) {
        					// add this rule to the cache
        					matchingRules.get().get(caseNode).add(pair.getValue());
        					return pair.getValue(); 
        				}
            	}
            	
            	// At this point all matching rules are evaluated
            	// Time to put all the updates together
            	UpdateMultiset result = new UpdateMultiset();
            	for (ASTNode rule: matchingRules.get().get(caseNode)) {
            		result.addAll(rule.getUpdates());
            	}
            	
            	pos.setNode(null, result, null);
            	return pos;
            	
            }
        }
        else {
            return null;
        }
    }

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}
	
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public static class CaseParseMap //extends ParseMapN<Node> {
	extends ParserTools.ArrayParseMap {

		private static final long serialVersionUID = 1L;
		String nextChildName;
		
		public CaseParseMap() {
			super(PLUGIN_NAME);
		}

		public Node map(Object... vals) {
			nextChildName = "alpha";
            Node node = new CaseRuleNode(((Node)vals[0]).getScannerInfo());
            addChildren(node, vals);
			return node;
		}

		@Override
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode) {
				parent.addChild(nextChildName, child);
				if (nextChildName.equals("gamma"))
					nextChildName = "beta";
			} else {
				parent.addChild(child);
				if (child.getToken().equals("of")) 				// case item 
					nextChildName = "beta";
				else
					if (child.getToken().equals(CASE_ITEM_RULE_DELIMITER))		// case rule
						nextChildName = "gamma";
			}
		}
	}
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
}
