/*	
 * KernelExtensionsPlugin.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author$ on $Date$.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.kernelextensions;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.EngineTools;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.Node.NameNodeTuple;
import org.coreasm.engine.kernel.FunctionRuleTermParseMap;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds functionality in handling function and rule elements
 * 
 * @author Roozbeh Farahbod
 *
 */
public class KernelExtensionsPlugin extends Plugin implements ParserPlugin, InterpreterPlugin {

	protected static final Logger logger = LoggerFactory.getLogger(KernelExtensionsPlugin.class);

	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 0, "alpha");

	public static final String PLUGIN_NAME = KernelExtensionsPlugin.class.getSimpleName();

	public static final String EXTENDED_FUNC_RULE_TERM_NAME = "ExtendedFunctionRuleTermNode";

	public static final String EXTENDED_RULE_CALL_NAME = "ExtendedRullCall";

	private HashMap<String, GrammarRule> parsers = null;
	
	private final String[] keywords = {"call"};
	private final String[] operators = {"(", ")"};

    private ThreadLocal<Map<Node,Node>> terms;

	public KernelExtensionsPlugin() {
		terms = new ThreadLocal<Map<Node,Node>>() {
			protected Map<Node, Node> initialValue() {
				return new HashMap<Node, Node>();
			}
		};
	}
	
	protected Map<Node, Node> getTerms() {
		return terms.get();
	}
	
	@Override
	public void initialize() throws InitializationFailedException {
	}

	@Override
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	@Override
	public String[] getKeywords() {
		return keywords;
	}

	@Override
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	@Override
	public String[] getOperators() {
		return operators;
	}

	@Override
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}

	@Override
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			
			KernelServices kernel = (KernelServices)capi.getPlugin("Kernel").getPluginInterface();
			
			Parser<Node> termParser = kernel.getTermParser();
			Parser<Node> tupleTermParser = kernel.getTupleTermParser();
			
			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			
			// ExtendedFunctionRuleTerm1: ID TupleTerm TupleTerm
			Parser<Node> extFuncRuleTermParser1 = Parsers.array(
	       			new Parser[] {
       				idParser,
       				tupleTermParser,
       				tupleTermParser
       				}).map( new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object... vals) {
							Node node = new ExtendedFunctionRuleTermNode(((Node)vals[0]).getScannerInfo());
							addChild(node, (new FunctionRuleTermParseMap()).map((Node)vals[0], (Node)vals[1]));
							for (Node n: ((Node)vals[2]).getChildNodes())
								if (n instanceof ASTNode) 
									node.addChild("lambda", n);
								else 
									node.addChild(n);
							return node;
						}
				
					});

			// ExtendedFunctionRuleTerm2: '(' Term ')' TupleTerm
			Parser<Node> extFuncRuleTermParser2 = Parsers.array(
	       			new Parser[] {
       				pTools.getOprParser("("),
       				termParser,
       				pTools.getOprParser(")"),
       				tupleTermParser
       				}).map( new ParserTools.ArrayParseMap(PLUGIN_NAME) {
						public Node map(Object... vals) {
							Node node = new ExtendedFunctionRuleTermNode(((Node)vals[0]).getScannerInfo());
							for (int i = 0; i < 3; i++)
								if (vals[i] != null)
									addChild(node, (Node)vals[i]);
							for (Node n: ((Node)vals[3]).getChildNodes())
								if (n instanceof ASTNode) 
									node.addChild("lambda", n);
								else 
									node.addChild(n);
							return node;
						}
				
					});

			Parser<Node> extendedFuncRuleTermParser = Parsers.longest(
					extFuncRuleTermParser1, extFuncRuleTermParser2);
			
			// ExtendedRuleCall: 'call' ExtendedFunctionRuleTerm
			Parser<Node> extRuleCallParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("call", PLUGIN_NAME),
					extendedFuncRuleTermParser
					}).map( 
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object... vals) {
							Node node = new ExtendedRuleCallNode(((Node)vals[0]).getScannerInfo());
							addChild(node, (Node)vals[0]);
							for (NameNodeTuple nt: ((Node)vals[1]).getChildNodesWithNames())
								node.addChild(nt.name, nt.node);
							return node;
						}
				
					});
						
					
			parsers.put(extFuncRuleTermParser1.toString(),
					new GrammarRule(extFuncRuleTermParser1.toString(), 
							"ID TupleTerm TupleTerm", extFuncRuleTermParser1, PLUGIN_NAME));

			parsers.put(extFuncRuleTermParser2.toString(),
					new GrammarRule(extFuncRuleTermParser2.toString(), 
							"'(' Term ')' TupleTerm", extFuncRuleTermParser2, PLUGIN_NAME));

			parsers.put("FunctionRuleTerm",
					new GrammarRule(EXTENDED_FUNC_RULE_TERM_NAME,
							EXTENDED_FUNC_RULE_TERM_NAME + "1 | " + EXTENDED_FUNC_RULE_TERM_NAME + "2",
							extendedFuncRuleTermParser, 
							PLUGIN_NAME));
			
			parsers.put("Rule", 
					new GrammarRule(EXTENDED_RULE_CALL_NAME,
							"'call' " + EXTENDED_FUNC_RULE_TERM_NAME, 
							extRuleCallParser,
							PLUGIN_NAME));
		}
		return parsers;
	}

	@Override
	public ASTNode interpret(Interpreter interpreter, ASTNode pos)
			throws InterpreterException {
		if (pos instanceof ExtendedFunctionRuleTermNode) {
			ExtendedFunctionRuleTermNode pnode = (ExtendedFunctionRuleTermNode)pos;
			
			// 1. evaluate the term part
			if (!pnode.getTerm().isEvaluated()) {
				return pnode.getTerm();
			} else {
				Element func = pnode.getTerm().getValue();
				
				if (func instanceof FunctionElement) {
					FunctionElement fe = (FunctionElement)func;
					final List<ASTNode> args = pnode.getArguments();
					final ASTNode toBeEvaluated = getUnevaluatedNode(args);
					if (toBeEvaluated == null) {
						// if all nodes are evaluated...
						ElementList vList = EngineTools.getValueList(args);
						
						//look for the function in the state
						Element value = null;
						String fname = capi.getStorage().getFunctionName(fe);
						Location loc = null;
						if (fname != null) {
							try {
								loc = new Location(fname, vList);
								value = capi.getStorage().getValue(loc);
							} catch (InvalidLocationException e) {
								// should not happen
								capi.error(e, pos, interpreter);
								return pos;
							}
						} else
							value = fe.getValue(vList);
						pos.setNode(loc, null, value);
					} else
						pos = toBeEvaluated;
				} else { 
					String msg = "Cannot apply arguments to a non-function value.";
					capi.error(msg, pos, interpreter);
					logger.error(msg);
				}
			} 
		} else
			if (pos instanceof ExtendedRuleCallNode) {
				ExtendedRuleCallNode pnode = (ExtendedRuleCallNode)pos;
				if (!pnode.getTerm().isEvaluated())
					return pnode.getTerm();
				else {
					Element func = pnode.getTerm().getValue();
					
					if (func instanceof RuleElement) {
						RuleElement re = (RuleElement)func;
						final List<ASTNode> args = pnode.getArguments();
						final List<String> params = re.getParam();
						if (args.size() == params.size()) {
							if (args.size()==0)
								pos = capi.getInterpreter().ruleCall(re, re.getParam(), null, pos);
							else
								pos = capi.getInterpreter().ruleCall(re, re.getParam(), args, pos);
						} else {
							capi.error("The number of arguments passed to '" + re.getName()  + 
									"' does not match its signature.", pos, interpreter);
						}
					}
				}
			} else { 
				String msg = "Cannot call a non-rule value.";
				capi.error(msg, pos, interpreter);
				logger.error(msg);
			}

		return pos;
	}

	/**
	 * The goal is to ensure that the given nodes are all evaluated. If there is 
	 * an unevaluated node, returns that node. If all the given nodes are evaluated
	 * returns <code>null</code>. 
	 * 
	 * @param nodes list of nodes
	 */
	private ASTNode getUnevaluatedNode(List<ASTNode> nodes) {
		for (ASTNode n: nodes) 
			if (!n.isEvaluated()) {
				return n;
			}
		return null;
	}
	

}
