/*	
 * TurboASMPlugin.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.turboasm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.MapFunction;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParseMap;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.parser.ParserTools.ArrayParseMap;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * This plugin provides the following TurboASM rules:
 * <ol>
 * <li>R1 <b>seq</b> R2</li>
 * <li><b>iterate</b> R</li>
 * <li><b>while</b> (exp) R</li>
 * </ol> 
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class TurboASMPlugin extends Plugin implements ParserPlugin, InterpreterPlugin, 
														VocabularyExtender {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 9, 1, "beta");

	protected static final Logger logger = LoggerFactory.getLogger(TurboASMPlugin.class);

	public static final String PLUGIN_NAME = TurboASMPlugin.class.getSimpleName();

	/* work copies of a tree */
	private ThreadLocal<Map<ASTNode, ASTNode>> workCopies;
	
	/* composed updates cache */
	private ThreadLocal<Map<ASTNode,UpdateMultiset>> composedUpdatesMap;

	private Map<String, GrammarRule> parsers = null;
	
	public static final String RETURN_RESULT_TOKEN = "<-";
	public static final String RESULT_KEYWORD = "result";
	public static final String WHILE_KEYWORD = "while";
	public static final String ITERATE_KEYWORD = "iterate";
	public static final String SEQ_KEYWORD = "seq";
	public static final String RETURN_KEYWORD = "return";
	public static final String LOCAL_KEYWORD = "local";

	private Map<String, FunctionElement> functions;
	private FunctionElement resultFunction;

	private final String[] keywords = {"seq", "next", "endseq", "seqblock", "endseqblock", "iterate", "while", 
			"local", "in", "return", "result"};
	private final String[] operators = {",", "<-", "[", "]"};
	
	@Override
	public void initialize() {
		workCopies = new ThreadLocal<Map<ASTNode,ASTNode>>() {
			@Override
			protected Map<ASTNode, ASTNode> initialValue() {
				return new IdentityHashMap<ASTNode, ASTNode>();
			}
		};
		composedUpdatesMap = new ThreadLocal<Map<ASTNode,UpdateMultiset>>() {
			@Override
			protected Map<ASTNode, UpdateMultiset> initialValue() {
				return new IdentityHashMap<ASTNode, UpdateMultiset>();
			}
		};
		logger.debug("TurboASM is loaded!");
	}

	/*
	 * Returns the composed updates cache for this thread
	 */
	private Map<ASTNode, UpdateMultiset> getThreadComposedUpdates() {
		return composedUpdatesMap.get();
	}
	
	/*
	 * Returns the work copy cache for this thread
	 */
	private Map<ASTNode, ASTNode> getThreadWorkCopy() {
		return workCopies.get();
	}
	
	/**
	 * @return <code>null</code>
	 */
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}


	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
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
	
			// SeqRule : 'seq' Rule ('next' Rule)+ | 'seq' (Rule)+ 'endseq'
			Parser<Node> seqRuleParser = Parsers.array(new Parser[] {
					Parsers.or(
							Parsers.array(pTools.getKeywParser("seq", PLUGIN_NAME),
									ruleParser,
									pTools.plus(Parsers.array(pTools.getKeywParser("next", PLUGIN_NAME), ruleParser))),
							Parsers.array(Parsers.or(pTools.getKeywParser("seq", PLUGIN_NAME), pTools.getKeywParser("seqblock", PLUGIN_NAME), pTools.getOprParser("[")),
									pTools.plus(ruleParser),
									Parsers.or(pTools.getKeywParser("endseq", PLUGIN_NAME), pTools.getKeywParser("endseqblock", PLUGIN_NAME), pTools.getOprParser("]"))))
				}).map(
					new SeqRuleParseMap());
			parsers.put("SeqRule",
					new GrammarRule("SeqRule", "'seq' Rule ('next' Rule)+ | 'seq' (Rule)+ 'endseq'", 
							seqRuleParser, PLUGIN_NAME));

			// IterateRule : 'iterate' Rule
			Parser<Node> iterateRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("iterate", PLUGIN_NAME),
						ruleParser,
					}).map(
					new ArrayParseMap(PLUGIN_NAME) {
						public Node map(Object... vals) {
							Node node = new IterateRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			});
			parsers.put("IterateRule",
					new GrammarRule("IterateRule", "'iterate' Rule", 
							iterateRuleParser, PLUGIN_NAME));

			// WhileRule : 'while' '(' Term ')' Rule
			Parser<Node> whileRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("while", PLUGIN_NAME),
						pTools.getOprParser("("),
						termParser,
						pTools.getOprParser(")"),
						pTools.getKeywParser("do", PLUGIN_NAME).optional(),
						ruleParser
					}).map(
					new WhileParseMap());
			parsers.put("WhileRule",
					new GrammarRule("WhileRule", "'while' '(' Term ')' Rule", 
							whileRuleParser, PLUGIN_NAME));

			// ReturnResultRule: FunctionRuleTerm '<-' FunctionRuleTerm
			Parser<Node> retResRuleParser = Parsers.array(
					new Parser[] {
						funcRuleTermParser,
						pTools.getOprParser("<-"),
						funcRuleTermParser
					}).map(
					new ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object... vals) {
							Node node = new ReturnResultNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
					}
			 );
			parsers.put("ReturnResultRule",
					new GrammarRule("ReturnResultRule", "FunctionRuleTerm '<-' FunctionRuleTerm", 
							retResRuleParser, PLUGIN_NAME));
			
			// ReturnRule : 'return' Term 'in' Rule
			Parser<Node> returnRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("return", PLUGIN_NAME),
						termParser,
						pTools.getKeywParser("in", PLUGIN_NAME),
						ruleParser
					}).map(
					new ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object... vals) {
							Node node = new ReturnRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			});
			parsers.put("ReturnRule",
					new GrammarRule("ReturnRule", "'return' Term 'in' Rule", 
							returnRuleParser, PLUGIN_NAME));

			// LocalRule : 'local' ID (',' ID)* 'in' Rule
			Parser<Node> localRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("local", PLUGIN_NAME),
						pTools.csplus(idParser),
						pTools.getKeywParser("in", PLUGIN_NAME),
						ruleParser
					}).map(
					new LocalRuleParseMap());
			parsers.put("LocalRule",
					new GrammarRule("LocalRule", "'local' ID (',' ID)* 'in' Rule", 
							localRuleParser, PLUGIN_NAME));
			
			// TurboASMRules : SeqRule | IterateRule | WhileRule | ReturnResultRule | LocalRule
			parsers.put("Rule", new GrammarRule("TurboASMRules", 
					"SeqRule | IterateRule | WhileRule | ReturnResultRule | LocalRule",
					Parsers.or(seqRuleParser, iterateRuleParser, whileRuleParser, 
							retResRuleParser, 
							localRuleParser), PLUGIN_NAME));
			
			parsers.put("BasicTerm", 
					new GrammarRule("TurboASMRules", "ReturnRule",
							returnRuleParser, PLUGIN_NAME));
			
			// ResultLocation : 'result'
			Parser<Node> resultLocationParser = //Parsers.map("ResultLocation",
						pTools.getKeywParser(RESULT_KEYWORD, PLUGIN_NAME).map(
					new ParseMap<Node, Node>(PLUGIN_NAME) {
						public Node map(Node v) {
							/*
							 *  Here we do a little bit of cheating! :-)
							 *  We basically make 'result' act as an identifier.
							 *  
							 *  see ParserTools.getIdentifierParser()
							 */
							
							Node node = new FunctionRuleTermNode(v.getScannerInfo());
							node.addChild("alpha", new ASTNode(
									"Kernel", 
									ASTNode.ID_CLASS, 
									"ID", 
									RESULT_KEYWORD,
									v.getScannerInfo(),
									Node.GENERAL_ID_NODE
									)); 
							return node;
						}
					}
			);
			parsers.put("ResultLocation",
					new GrammarRule("ResultLocation", 
							"'result'", 
							resultLocationParser, PLUGIN_NAME));
			
			// FunctionRuleTerm : 'result'
			/*
			 * !! Notice that to be on the safe side, any
			 *    grammar rule that extends the FunctionRuleTerm 
			 *    rule has to return a node of the class FunctionRuleTermNode.
			 */
			parsers.put(Kernel.GR_FUNCTION_RULE_TERM, 
					new GrammarRule(resultLocationParser.toString(),
					"ResultLocation", 
					resultLocationParser, PLUGIN_NAME));
    	}
    	
    	return parsers;
    }
	
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		AbstractStorage storage = capi.getStorage();
		
		if (pos instanceof SeqRuleNode) {
			SeqRuleNode node = (SeqRuleNode)pos;
			ASTNode firstRule = node.getFirstRule();
			ASTNode secondRule = node.getSecondRule();
			
			// Evaluate the first rule
			if (!firstRule.isEvaluated()) 
				return firstRule;
			
			if (!secondRule.isEvaluated()) {
				// Aggregate updates of the first rule
				Set<Update> aggregatedUpdate = null;
				try {
					aggregatedUpdate = 
						storage.performAggregation(firstRule.getUpdates());
					if (storage.isConsistent(aggregatedUpdate)) {
						storage.pushState();
						storage.apply(aggregatedUpdate);
						return secondRule; 
					} else
						// this will be catched in the 'catch' phrase 
						throw new EngineError();
				} catch (EngineError e) {
					// inconsistent aggregation or inconsistent updateset
					capi.warning(PLUGIN_NAME, "TurboASM Plugin: Inconsistent updates computed in sequence. Leaving the sequence", 
							secondRule, interpreter);
					//TODO better logging (tell where was it)
					pos.setNode(null, firstRule.getUpdates(), null);
				}
			} else {
				// second rule is evaluated...
				
				UpdateMultiset composed = storage.compose(firstRule.getUpdates(), secondRule.getUpdates());
				storage.popState();
				pos.setNode(null, composed, null);
			}
			
			
		} else
			if (pos instanceof IterateRuleNode) {
				IterateRuleNode node = (IterateRuleNode)pos;
				ASTNode childRule = node.getChildRule();
				
				Map<ASTNode, UpdateMultiset> composedUpdates = getThreadComposedUpdates();

				if (!childRule.isEvaluated()) {
					storage.pushState();
					composedUpdates.put(pos, new UpdateMultiset()); 
					return childRule;
				} else {
					UpdateMultiset u = childRule.getUpdates();
					if (!u.isEmpty()) {
						Set<Update> uSet = null;
						try {
							uSet = storage.performAggregation(u);
							composedUpdates.put(pos, storage.compose(composedUpdates.get(pos), u));
							if (storage.isConsistent(uSet)) {
								storage.apply(uSet);
								interpreter.clearTree(childRule);
								return childRule;
							} else
								// this will be catched by the 'catch' clause below
								throw new EngineError();
						} catch (EngineError e) {
							storage.popState();
							// inconsistent aggregation or updateset
							pos.setNode(null, composedUpdates.get(pos), null);
							composedUpdates.remove(pos);
						}
					} else {
						storage.popState();
						pos.setNode(null, composedUpdates.get(pos), null);
						composedUpdates.remove(pos);
					}
				}
			} else
				if (pos instanceof WhileRuleNode) {
					WhileRuleNode node = (WhileRuleNode)pos;
					ASTNode childRule = node.getChildRule();
					ASTNode whileCond = node.getCondition();
	
					Map<ASTNode, UpdateMultiset> composedUpdates = getThreadComposedUpdates();
					
					// if the guard is not evaluated, evaluate it
					if (!whileCond.isEvaluated()) {
						storage.pushState();
						composedUpdates.put(pos, new UpdateMultiset()); 
						return whileCond;
					}
					
					// if condition is TRUE
					if (whileCond.getValue().equals(BooleanElement.TRUE)) {
						if (!childRule.isEvaluated()) 
							return childRule;
						else {
							UpdateMultiset u = childRule.getUpdates();
							if (!u.isEmpty()) {
								Set<Update> uSet = null;
								try {
									uSet = storage.performAggregation(u);
									composedUpdates.put(pos, storage.compose(composedUpdates.get(pos), u));
									if (storage.isConsistent(uSet)) {
										storage.apply(uSet);
										interpreter.clearTree(childRule);
										interpreter.clearTree(whileCond);
										return whileCond;
									} else
										// this will be catched by the 'catch' clause below
										throw new EngineError();
								} catch (EngineError e) {
									storage.popState();
									// inconsistent aggregation or updateset
									pos.setNode(null, composedUpdates.get(pos), null);
									composedUpdates.remove(pos);
								}
							} else {
								storage.popState();
								pos.setNode(null, composedUpdates.get(pos), null);
								composedUpdates.remove(pos);
							}
						}
					} else {
						storage.popState();
						pos.setNode(null, composedUpdates.get(pos), null);
						composedUpdates.remove(pos);
					}
				} else
					if (pos instanceof ReturnResultNode) {
						ReturnResultNode node = (ReturnResultNode)pos;
						ASTNode loc = node.getLocationNode();
						FunctionRuleTermNode rule = (FunctionRuleTermNode)node.getRuleNode();
						
						// If the rule part is of the form 'x' or 'x(...)'
						if (rule.hasName()) {
	
							String x = rule.getName();
							
							// If the rule part is of the form 'x' with no arguments
							if (!rule.hasArguments()) {
								
								if (storage.isRuleName(x)) {
									pos = ruleCallWithResult(interpreter, storage.getRule(x), null, loc, pos);
								}
							
							} else { // if the rule part 'x(...)' (with arguments)
								
								if (storage.isRuleName(x)) {
									pos = ruleCallWithResult(interpreter, storage.getRule(x), rule.getArguments(), loc, pos);
								}
								
							}
						}
	
						
					} else 
						if (pos instanceof ReturnRuleNode) {
							ReturnRuleNode node = (ReturnRuleNode)pos;
							ASTNode exp = node.getExpressionNode();
							ASTNode rule = node.getRuleNode();
							
							// Evaluate the rule
							if (!rule.isEvaluated()) 
								return rule;
							
							if (!exp.isEvaluated()) {
								// Aggregate updates of the rule
								Set<Update> aggregatedUpdate = null;
								try {
									aggregatedUpdate = 
										storage.performAggregation(rule.getUpdates());
									if (storage.isConsistent(aggregatedUpdate)) {
										storage.pushState();
										storage.apply(aggregatedUpdate);
										return exp; 
									} else
										throw new EngineError("You should not see this error (TurboASMPlugin:return rule)");
								} catch (EngineError e) {
									pos.setNode(null, new UpdateMultiset(), Element.UNDEF);
								}
							} else {
								// expression is evaluated...
								storage.popState();
								pos.setNode(null, new UpdateMultiset(), exp.getValue());
							}
						} else 
							if (pos instanceof LocalRuleNode) {
								LocalRuleNode node = (LocalRuleNode)pos;
								ASTNode rule = node.getRuleNode();
								
								// Evaluate the rule
								if (!rule.isEvaluated()) 
									return rule;
								else {
									// Remove updates of local functions
									UpdateMultiset updates = rule.getUpdates();
									UpdateMultiset newUpdates = new UpdateMultiset();
									Collection<String> fNames = node.getFunctionNames();
									for (Update u: updates) {
										if (!fNames.contains(u.loc.name))
											newUpdates.add(u);
									}
									pos.setNode(null, newUpdates, rule.getValue());
								}
								
							} else 
								if (pos instanceof EmptyNode)
									pos.setNode(null, new UpdateMultiset(), null);
								else
									throw new InterpreterException(this.getName() + " cannot interpret the given node.");
		
		return pos;
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
		exParams.add(RESULT_KEYWORD);
		
		return interpreter.ruleCall(rule, exParams, exArgs, pos);
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public static class WhileParseMap extends ArrayParseMap {
		
		String nextChildName = "cond";
		
		public WhileParseMap() {
			super(PLUGIN_NAME);
		}

		public Node map(Object... vals) {
			nextChildName = "cond";
			Node node = new WhileRuleNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode) {
				parent.addChild(nextChildName, child);
				nextChildName = "rule";
			} else
				parent.addChild(child);
		}
		
	}

	public static class LocalRuleParseMap extends ArrayParseMap {
		
		String nextChildName = "lambda";
		
		public LocalRuleParseMap() {
			super(PLUGIN_NAME);
		}

		public Node map(Object... vals) {
			nextChildName = "lambda";
			Node node = new LocalRuleNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode) {
				parent.addChild(nextChildName, child);
			} else {
				if (child.getToken().equals("in"))
					nextChildName = "alpha";
				parent.addChild(child);
			}
		}
		
	}
	
	public static class SeqRuleParseMap extends ArrayParseMap {

		public SeqRuleParseMap() {
			super(PLUGIN_NAME);
		}
		
		public Node map(Object... vals) {
			vals = (Object[])vals[0];
			SeqRuleNode node = new SeqRuleNode(((Node)vals[0]).getScannerInfo());
			ArrayList<Node> nodes = new ArrayList<Node>();
			int i = unpackChildren(nodes, vals);
			addSeqChildren(node, nodes, i);
			return node;
		}

		private int unpackChildren(List<Node> nodes, Object[] vals) {
			// 'astCount' is the number of ASTNodes in the list
			int astCount = 0;
			for (Object child: vals) {
				if (child != null) {
					if (child instanceof ASTNode)
						astCount++;
					if (child instanceof Object[])
						astCount += unpackChildren(nodes, (Object[])child);
					else
						if (child instanceof Node)
							nodes.add((Node)child);
				}
			}
			return astCount;
		}
		
		private void addSeqChildren(SeqRuleNode root, List<Node> children, int astCount) {
			int i = 1;
			for (Node child: children) {
				if (child instanceof ASTNode) {
					if (astCount == 1) {
						root.addChild(child);
						root.addChild(new EmptyNode(child.getScannerInfo()));
					} else {
						if (root.getFirst() == null)
							root.addChild(child);
						else {
							if (i == astCount) 
								root.addChild(child);
							else {
								SeqRuleNode newRoot = new SeqRuleNode(child.getScannerInfo());
								newRoot.addChild(child);
								root.addChild(newRoot);
								root = newRoot;
							}
						}
					}
					i++;
				} else
					root.addChild(child);
			}
		}
		
		private SeqRuleNode addSeqChild(SeqRuleNode root, Node child) {
			SeqRuleNode newRoot = root;
			if (child instanceof ASTNode) {
				if (root.getFirst() == null)
					root.addChild(child);
				else {
					newRoot = new SeqRuleNode(child.getScannerInfo());
					newRoot.addChild(child);
					root.addChild(newRoot);
				}
			} else
				root.addChild(child);
			return newRoot;
		}
		
	}

	public Set<String> getBackgroundNames() {
		return Collections.emptySet();
	}

	public Map<String, BackgroundElement> getBackgrounds() {
		return Collections.emptyMap();
	}

	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();
			functions.put(RESULT_KEYWORD, getResultFunction());
		}
		return functions;
	}

	/*
	 * Creates the 'result' function element.
	 */
	private FunctionElement getResultFunction() {
		if (resultFunction == null) {
			resultFunction = new MapFunction();
			resultFunction.setFClass(FunctionElement.FunctionClass.fcOut);
			resultFunction.setSignature(new Signature());
		}
		return resultFunction;
	}
	
	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		return Collections.emptyMap();
	}

	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	public Map<String, UniverseElement> getUniverses() {
		return Collections.emptyMap();
	}
}
