/*	
 * BagPlugin.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.bag;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.PluginAggregationAPI;
import org.coreasm.engine.absstorage.PluginAggregationAPI.Flag;
import org.coreasm.engine.absstorage.PluginCompositionAPI;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.parser.ParserTools.ArrayParseMap;
import org.coreasm.engine.plugin.Aggregator;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Plugin providing the Bag background.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class BagPlugin extends Plugin 
		implements VocabularyExtender, OperatorProvider, Aggregator, 
				ParserPlugin, InterpreterPlugin {

	private static final Logger logger = LoggerFactory.getLogger(BagPlugin.class);

	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 2, "beta");
	
	public static final String PLUGIN_NAME = BagPlugin.class.getSimpleName();

	public static final String BAG_UNION_OP = "union";
	public static final String BAG_UNION_OP_2 = "∪";
	public static final String BAG_INTERSECT_OP = "intersect";
	public static final String BAG_INTERSECT_OP_2 = "∩";
	public static final String BAG_DIFF_OP = "diff";
	public static final String BAG_JOIN_OP = "+";

	public static final String BAG_OPEN_SYMBOL = "<<";
	public static final String BAG_CLOSE_SYMBOL = ">>";
	
	public static final String BAG_UPDATE_ACTION = "bagUpdateAction";
	public static final String[] UPDATE_ACTIONS = {BAG_UPDATE_ACTION};
	
	private final String[] keywords = {"union", "intersect", "diff", "is", "in", "with", "subset"};
	private final String[] operators = {BAG_OPEN_SYMBOL, BAG_CLOSE_SYMBOL, BAG_JOIN_OP, "|", ","};
	
	static final String NAME = PLUGIN_NAME;
	
	/* keeps track of to-be-considered values in a bag comprehension */
	private ThreadLocal<Map<ASTNode, Collection<Element>>> tobeConsidered;
	
	/* keeps track of to-be-considered values in an advanced bag comprehension */
	private ThreadLocal<Map<ASTNode, Collection<Map<String, Element>>>> tobeConsideredAdv;
	
	/* keeps new bags created on a bag comprehension node */
	private ThreadLocal<Map<ASTNode, Collection<Element>>> newBags;
	
//	protected Interpreter interpreter;
	
	private BagBackgroundElement bagBackground;

	private Set<String> dependencyNames = null;
	private Map<String,FunctionElement> functions = null;
	private Map<String,BackgroundElement> backgrounds = null;
	private Map<String, GrammarRule> parsers = null;
	
	private final Parser.Reference<Node> refBagTermParser = Parser.newReference();
	
	public BagPlugin() {
		super();
	}

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	public void initialize() {
		tobeConsidered = new ThreadLocal<Map<ASTNode,Collection<Element>>>() {
			@Override
			protected Map<ASTNode, Collection<Element>> initialValue() {
				return new HashMap<ASTNode, Collection<Element>>();
			}
		};
		tobeConsideredAdv = new ThreadLocal<Map<ASTNode,Collection<Map<String,Element>>>>() {
			@Override
			protected Map<ASTNode, Collection<Map<String, Element>>> initialValue() {
				return new HashMap<ASTNode, Collection<Map<String,Element>>>();
			}
		};
		newBags = new ThreadLocal<Map<ASTNode, Collection<Element>>>() {
			@Override
			protected Map<ASTNode, Collection<Element>> initialValue() {
				return new HashMap<ASTNode, Collection<Element>>();
			}
		};
		bagBackground = new BagBackgroundElement();
	}
	
	/*
	 * Returns the instance of 'tobeConsidered' map for this thread.
	 */
	private Map<ASTNode, Collection<Element>> getToBeConsideredMap() {
		return tobeConsidered.get();
	}
	
	/*
	 * Returns the instance of 'tobeConsideredAdv' map for this thread.
	 */
	private Map<ASTNode, Collection<Map<String, Element>>> getToBeConsideredAdvMap() {
		return tobeConsideredAdv.get();
	}
	
	/*
	 * Returns the instance of 'newBag' map for this thread.
	 */
	private Map<ASTNode, Collection<Element>> getNewBagMap() {
		return newBags.get();
	}
	
	@Override
	public Set<String> getDependencyNames() {
		if (dependencyNames == null) {
			dependencyNames = new HashSet<String>();
			dependencyNames.add("CollectionPlugin");
			dependencyNames.add("NumberPlugin");
		}
		return dependencyNames;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.Plugin#interpret(org.coreasm.engine.interpreter.ASTNode)
	 */
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) {
		ASTNode nextPos = pos;
		String gClass = pos.getGrammarClass();
        
		Map<ASTNode, Collection<Element>> tobeConsidered = getToBeConsideredMap();
		Map<ASTNode, Collection<Map<String, Element>>> tobeConsideredAdv = getToBeConsideredAdvMap();
		Map<ASTNode, Collection<Element>> newBag = getNewBagMap();
		
		// if bag related expression
		if (gClass.equals(ASTNode.EXPRESSION_CLASS))
		{
			if (pos instanceof BagEnumerateNode) {
				// bag enumeration wrapper
				BagEnumerateNode seNode = (BagEnumerateNode)pos;
					
				nextPos = seNode.getUnevaluatedMember();
					
				// no unevaluated members
				if (nextPos == null)
				{
					// set next pos to current position
					nextPos = pos;
					
					List<Element> elements = null;
					if (seNode.getAllMembers().size() == 0)
						elements = Collections.emptyList();
					else {
						elements = new ArrayList<Element>();
						// for each member node
						for (ASTNode n : seNode.getAllMembers())
						{
							// get element value and add it to the collection
							elements.add(n.getValue());
						}
					}
					
					// result of this node is the bag element produced
					pos.setNode(null,null,new BagElement(elements));
				}		
	        }
			
			// if the node is a simple set comprehension (single variable specifier) ...
			else if (pos instanceof BagCompNode) {
				BagCompNode node = (BagCompNode)pos;
				String variable = node.getSpecifierVar();
				
				// if nothing is evaluated yet
				if (!node.getDomain().isEvaluated()) {
					// if x = x_1 
					if (node.getConstrainerVar().equals(variable)) {
						tobeConsidered.remove(pos); // to make its to-be-considered value null
						newBag.put(pos, new ArrayList<Element>());
						return node.getDomain();
					} else
						capi.error("Constrainer variable must have same name as specifier variable (" 
								+ variable + " vs. " + node.getConstrainerVar() + ").", node, interpreter);
				}
				
				// if the domain is evaluated 
				else if (!node.getGuard().isEvaluated()) {
					if (!(node.getDomain().getValue() instanceof Enumerable)) 
						capi.error("Free variables may only be bound to enumerable elements.", node.getDomain(), interpreter);
					else {
						if (tobeConsidered.get(pos) == null) {
							Enumerable domain = (Enumerable)node.getDomain().getValue();
							tobeConsidered.put(pos, new ArrayList<Element>(domain.enumerate()));
						}
						Collection<Element> domain = tobeConsidered.get(pos);
						
						if (domain.isEmpty()) {
							pos.setNode(null, null, new BagElement(newBag.get(pos)));
							return pos;
						} else {
							Element e = domain.iterator().next();
							interpreter.addEnv(variable, e);
							domain.remove(e);
							return node.getGuard();
						}
					}
				}
				
				// if everything is evaluated
				else {
					if (node.getGuard().getValue().equals(BooleanElement.TRUE)) 
						newBag.get(pos).add(interpreter.getEnv(variable));
					interpreter.removeEnv(variable);
					interpreter.clearTree(node.getGuard());
					return pos;
				}
			}

			// if the node is an advanced set comprehension (expression specifier) ...
			else if (pos instanceof BagAdvancedCompNode) {
				BagAdvancedCompNode node = (BagAdvancedCompNode)pos;
				Map<String, ASTNode> bindings = null;
				
				// get variable to domain bindings
				try {
					bindings = node.getVarBindings();
				} catch (EngineException e) {
					// "No two constrainer variables may have the same name."
					capi.error(e);
				}
				ASTNode guard = node.getGuard();
				ASTNode expression = node.getSetFunction();

				if (!guard.isEvaluated()) {
	 				if (bindings.size() >= 1) {
						if (bindings.containsKey(node.getSpecifierVar())) 
							capi.error("Constrainer variable cannot have same name as specifier.", node, interpreter);
						
						// evaluate all the domains
						for (ASTNode domain: bindings.values())
							if (!domain.isEvaluated()) 
								return domain;
						
						// if all domains are evaluated
						for (ASTNode domain: bindings.values()) {
							if (!(domain.getValue() instanceof Enumerable)) {
								capi.error("Constrainer variables may only be bound to enumerable elements.", domain, interpreter);
								return pos;
							} else 
								// if any domain is empty, the whole result is also empty
								if (((Enumerable)domain.getValue()).enumerate().size() == 0) { 
									pos.setNode(null, null, new BagElement());
									return pos;
								}
						}
						
						// else
					
						// TODO Inconsistent with the spec
						// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						// !!!                                          !!!
						// !!! FROM THIS POINT, IT DEFERS FROM THE SPEC !!!
						// !!!                                          !!!
						// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						
						// create the resulting bag
						newBag.put(pos, new ArrayList<Element>());
						
						// collection of all possible bindings
						Collection<Map<String,Element>> possibleBindings = new ArrayList<Map<String,Element>>();
						
						// set of all variables
						ArrayList<String> allVariables = new ArrayList<String>(bindings.keySet());
						
						// set of all possible values for variables 
						Map<String,ArrayList<Element>> possibleValues = new HashMap<String,ArrayList<Element>>();
						for (String var: bindings.keySet()) {
							Enumerable set = (Enumerable)(bindings.get(var).getValue());
							possibleValues.put(var, new ArrayList<Element>(set.enumerate()));
						}
	
						// create all possible combination of values for variables
						createAllPossibleBindings(
								allVariables, possibleValues, 0, possibleBindings, new HashMap<String,Element>());
	
						// set the superset of values
						tobeConsideredAdv.put(pos, possibleBindings);
						
						// pick the first combination
						Map<String,Element> firstBinding = possibleBindings.iterator().next();
						
						// bind the combination to the variables
						bindVariables(interpreter, firstBinding);
						
						// remove the already chosen combination
						possibleBindings.remove(firstBinding);
						
						return guard;
						
					} else
						capi.error("At least one constrainer variable must be present.", node, interpreter);
				} 
				
				// if guard is evaluated but the expression is not
				else if (!expression.isEvaluated()) {
					if (guard.getValue().equals(BooleanElement.TRUE)) 
						return expression;
					else {
						// remove previous bindings
						unbindVariables(interpreter, bindings.keySet());

						// get the remaining combinations
						Collection<Map<String,Element>> possibleBindings = tobeConsideredAdv.get(pos);
						
						// if there is more combination to be tried...
						if (possibleBindings.size() > 0) {

							// pick the next combination
							Map<String,Element> nextBinding = possibleBindings.iterator().next();
							
							// bind the combination to the variables
							bindVariables(interpreter, nextBinding);
							
							// remove the already chosen combination
							possibleBindings.remove(nextBinding);
							
							// clear the guard
							interpreter.clearTree(guard);
							
							return guard;
						} else {
							pos.setNode(null, null, new BagElement(newBag.get(pos)));
						}
							
					}
				} 
				
				// if everything is evaluated
				else {
					// remove previous bindings
					unbindVariables(interpreter, bindings.keySet());

					Collection<Element> result = newBag.get(pos);
					result.add(expression.getValue());
					// get the remaining combinations
					Collection<Map<String,Element>> possibleBindings = tobeConsideredAdv.get(pos);
					if (possibleBindings.size() > 0) {

						// pick the first combination
						Map<String,Element> nextBinding = possibleBindings.iterator().next();
						
						// bind the combination to the variables
						bindVariables(interpreter, nextBinding);
						
						// remove the already chosen combination
						possibleBindings.remove(nextBinding);

						// clear the guard and the expression
						interpreter.clearTree(guard);
						interpreter.clearTree(expression);
						
						return guard;
					} else {
						pos.setNode(null, null, new BagElement(newBag.get(pos)));
						return pos;
					}
				}
				
				return pos;
			}
			
			else if (pos instanceof TrueGuardNode) {
				pos.setNode(null, null, BooleanElement.TRUE);
				return pos;
			}
		}
		
        return nextPos;
	}

	/*
	 * This recursive method creates all the possible combinations of values
	 * for variables. 
	 */
	private void createAllPossibleBindings(
			ArrayList<String> allVariables, 
			Map<String,ArrayList<Element>> possibleValues, 
			int index, 
			Collection<Map<String,Element>> possibleBindings, 
			Map<String,Element> currentBinding) {

		// get possible values for this particular variable
		String var = allVariables.get(index);
		ArrayList<Element> values = new ArrayList<Element>(possibleValues.get(var));
		
		while (values.size() > 0) {
			// get the first element of those values
			Element value = values.get(0);
			
			// put it as a possible binding
			currentBinding.put(var, value);
			
			// if this is not the last variable in the list
			if (index < (allVariables.size() - 1)) {
				// get all the possible values for the remaining variables
				createAllPossibleBindings(
						allVariables, possibleValues, index+1, possibleBindings, currentBinding);
			} 
			
			// if this is the last variable
			else {
				// currentBinding is a draft copy that keeps changing, so you want
				// to add a new map to the set
				possibleBindings.add(new HashMap<String,Element>(currentBinding));
			}
			values.remove(0);
		}
	}
	
	/*
	 * Binds values to variables.
	 */
	private void bindVariables(Interpreter interpreter, Map<String,Element> binding) {
		for (String var: binding.keySet()) {
			interpreter.addEnv(var, binding.get(var));
		}
	}
	
	/*
	 * Unbinds environment variables.
	 */
	private void unbindVariables(Interpreter interpreter, Set<String> variables) {
		for (String var: variables){
			interpreter.removeEnv(var);
		}
	}
	
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	/*
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
	 */
	public Parser<Node> getParser(String nonterminal) {
		if (nonterminal.equals("SetTerm"))
			return refBagTermParser.lazy();
		else
			return null;
	}


	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> termParser = kernel.getTermParser();
			Parser<Node> guardParser = kernel.getGuardParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();

			// BagEnumerate: '<<' ( Term (',' Term)* )? '>>'
			Parser<Node> bagEnumerateParser = Parsers.array(
					new Parser[] {
							pTools.getOprParser(BAG_OPEN_SYMBOL),
							pTools.csplus(termParser).optional(),
							pTools.getOprParser(BAG_CLOSE_SYMBOL)
							}).map(
					new BagEnumerateParseMap());
			parsers.put("BagEnumerate", 
					new GrammarRule("BagEnumerate",
							"'" + BAG_OPEN_SYMBOL + "' Term (',' Term)* '" + BAG_CLOSE_SYMBOL + "'", bagEnumerateParser, PLUGIN_NAME));
			
			// BagComprehension: '<<' Term ( 'is' Term )? '|' ID 'in' Term 
			//                    ( ',' ID 'in' Term )* ( 'with' Guard )? '>>'
			Parser<Node> bagComprehensionParser = Parsers.array(
					new Parser[] {
						pTools.getOprParser(BAG_OPEN_SYMBOL),
						termParser,
						pTools.seq(
								pTools.getKeywParser("is", PLUGIN_NAME),
								termParser).optional(),
						pTools.getOprParser("|"),
						pTools.csplus(pTools.seq(
								idParser,
								pTools.getKeywParser("in", PLUGIN_NAME),
								termParser)),
						pTools.seq(
								pTools.getKeywParser("with", PLUGIN_NAME),
								guardParser).optional(),
						pTools.getOprParser(BAG_CLOSE_SYMBOL)
					}).map(
					new BagComprehensionParseMap());
			parsers.put("BagComprehension", 
					new GrammarRule("BagComprehension",
							"'" + BAG_OPEN_SYMBOL + "' Term ( 'is' Term )? '|' ID 'in' Term ( ',' ID 'in' Term )* ( 'with' Guard )? '" + BAG_CLOSE_SYMBOL + "'", 
							bagComprehensionParser, PLUGIN_NAME));
			
			Parser<Node> bagtermParser = Parsers.or(bagEnumerateParser, bagComprehensionParser);
			refBagTermParser.set(bagtermParser);
			
			// BasicTerm : BagEnumerate | BagComprehension | ...
			parsers.put("BasicTerm",
					new GrammarRule("BagBasicTerm", 
							"BagEnumerate | BagComprehension", 
							refBagTermParser.lazy(), PLUGIN_NAME));
			
		}
		
		return parsers;
	}
	//--------------------------------
	// Vocabulary Extender Interface
	//--------------------------------
	
	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctions()
	 */
	public Map<String,FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String,FunctionElement>();
		}
		return functions;
	}

	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		return null;
	}

	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverses()
	 */
	public Map<String,UniverseElement> getUniverses() {
		// no universes
		return Collections.emptyMap();
	}

	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgrounds()
	 */
	public Map<String,BackgroundElement> getBackgrounds() {
		if (backgrounds == null) {
			backgrounds = new HashMap<String,BackgroundElement>();
			backgrounds.put(BagBackgroundElement.BAG_BACKGROUND_NAME, bagBackground);
		}
		return backgrounds;
	}
	
	//--------------------------------
	// Operator Implementor Interface
	//--------------------------------

	public Collection<OperatorRule> getOperatorRules() {
		
		ArrayList<OperatorRule> opRules = new ArrayList<OperatorRule>();
		
		opRules.add(new OperatorRule(BAG_INTERSECT_OP,
				OpType.INFIX_LEFT,
				675,
				NAME));
		
		opRules.add(new OperatorRule(BAG_DIFF_OP,
				OpType.INFIX_LEFT,
				650,
				NAME));
		
		opRules.add(new OperatorRule(BAG_UNION_OP,
				OpType.INFIX_LEFT,
				650,
				NAME));
		
		opRules.add(new OperatorRule(BAG_JOIN_OP,
				OpType.INFIX_LEFT,
				750,
				NAME));
		
		return opRules;
	}

	public Element interpretOperatorNode(Interpreter interpreter, ASTNode opNode) throws InterpreterException {
		Element result = null;
		String x = opNode.getToken();
		String gClass = opNode.getGrammarClass();
		
		// if class of operator is binary
		if (gClass.equals(ASTNode.BINARY_OPERATOR_CLASS))
		{
			
			// get operand nodes
			ASTNode alpha = opNode.getFirst();
			ASTNode beta = alpha.getNext();
			
			// get operand values
			Element l = alpha.getValue();
			Element r = beta.getValue();
			
			// new bag element holds result
			Map<Element, Integer> resultBag = new HashMap<Element, Integer>();
	
			// confirm that operands are enumerable, otherwise throw an error
			if ((l instanceof BagElement || l.equals(Element.UNDEF))
					&& (r instanceof BagElement || r.equals(Element.UNDEF))) {
				if (l instanceof BagElement && r instanceof BagElement) {
					// get enumerable interface to operands
					BagElement eL = (BagElement)l;
					BagElement eR = (BagElement)r;
					
					// bag intersection
					if (x.equals(BAG_INTERSECT_OP))
					{
						// add elements which are in both bags
						for (Entry<? extends Element,Integer> e: eL.members.entrySet()) {
							Integer c = eR.members.get(e.getKey());
							if (c != null) {
								if (c < e.getValue())
									resultBag.put(e.getKey(), c);
								else
									resultBag.put(e.getKey(), e.getValue());
							}
						}
						result = new BagElement(resultBag);
					}
					// bag difference
					else if (x.equals(BAG_DIFF_OP))
					{
						// add elements which are not in the second bag
						for (Entry<? extends Element,Integer> e: eL.members.entrySet()) {
							Integer c = eR.members.get(e.getKey());
							if (c == null) 
								c = 0;
							if (c < e.getValue())
								resultBag.put(e.getKey(), e.getValue() - c);
						}
						result = new BagElement(resultBag);
					}
					// bag union
					else if (x.equals(BAG_UNION_OP))
					{
						// add elements from both bags with maximum of multiplicity
						resultBag = new HashMap<Element, Integer>(eL.members);
						for (Entry<? extends Element,Integer> e: eR.members.entrySet()) {
							Integer c = resultBag.get(e.getKey());
							if (c == null) 
								c = 0;
							if (e.getValue() > c)
								resultBag.put(e.getKey(), e.getValue());
						}
						result = new BagElement(resultBag);
					}
					// bag join
					else if (x.equals(BAG_JOIN_OP))
					{
						// add elements from both bags
						resultBag = new HashMap<Element, Integer>(eL.members);
						for (Entry<? extends Element,Integer> e: eR.members.entrySet()) {
							Integer c = resultBag.get(e.getKey());
							if (c == null) 
								c = 0;
							resultBag.put(e.getKey(), e.getValue() + c);
						}
						result = new BagElement(resultBag);
					}
				
				} else {
					result = Element.UNDEF;
					if (l.equals(Element.UNDEF) && r.equals(Element.UNDEF))
						capi.warning(PLUGIN_NAME, "Both operands of the '" + x + "' operator were undef.", opNode, interpreter);
					else
						if (l.equals(Element.UNDEF))
							capi.warning(PLUGIN_NAME, "The left operand of the '" + x + "' operator was undef.", opNode, interpreter);
						else
							if (r.equals(Element.UNDEF))
								capi.warning(PLUGIN_NAME, "The right operand of the '" + x + "' operator was undef.", opNode, interpreter);
				}
			}
		}
		
		return result;
	}
	
	//----------------------------------
	// Aggregator Interface AND Helpers
	//----------------------------------
	

	public String[] getUpdateActions() {
		return UPDATE_ACTIONS;
	}

	/**
	 * Basic Update Aggregator.
	 * 
	 * @param pluginAgg plugin aggregation API object.
	 */
	public void aggregateUpdates(PluginAggregationAPI pluginAgg) {
		
		// all locations on which contain bag incremental updates
		Set<Location> locsToAggregate = pluginAgg.getLocsWithAnyAction(BAG_UPDATE_ACTION);
		
		// for all locations to aggregate
		for (Location l : locsToAggregate)
		{
			// if regular update affects this location
			if (pluginAgg.regularUpdatesAffectsLoc(l))
			{
				// Case 1a
				// if regular updates are inconsistent, then aggregation inconsistency
				if (pluginAgg.inconsistentRegularUpdatesOnLoc(l))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// Case 1b
				// if regular update is not a bag, then aggregation inconsistency
				else if (regularUpdateIsNotBag(l,pluginAgg))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// Case 1c
				// if add/remove are inconsistent with regular update, then aggregation inconsistency
				else if (addRemoveConflictWithRU(l,pluginAgg))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// otherwise bag aggregation can be performed
				else
					// get regular update to add to resultant updates set
					pluginAgg.addResultantUpdate(getRegularUpdate(l,pluginAgg),this);
			}
			// else only partial updates affect this location
			else
			{	// Case 2a
				// if bag not currently at the location, then aggregation inconsistency
				if (bagNotInLocation(l))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// otherwise bag aggregation can be performed
				else
					// get resultant update to add to resultant updates set
					pluginAgg.addResultantUpdate(buildResultantUpdate(l,pluginAgg),this);
			}	
		}	
	}


	public void compose(PluginCompositionAPI compAPI) {
		
		for (Location l: compAPI.getAffectedLocations()) {
			boolean isLocUpdatedWithAddRemove_Set1 = 
				compAPI.isLocUpdatedWithActions(1, l, BAG_UPDATE_ACTION);
			boolean isLocUpdatedWithAddRemove_Set2 = 
				compAPI.isLocUpdatedWithActions(2, l, BAG_UPDATE_ACTION);
			
			// Case 1a
			if (isLocUpdatedWithAddRemove_Set1 && !compAPI.isLocationUpdated(2, l)) {
				UpdateMultiset updates = filterUpdates(compAPI.getLocUpdates(1, l));
				for (Update ui: updates)
					compAPI.addComposedUpdate(ui, this);
			} else 

			// Case 1b
			if (isLocUpdatedWithAddRemove_Set2 && !compAPI.isLocationUpdated(1, l)) {
				UpdateMultiset updates = filterUpdates(compAPI.getLocUpdates(2, l));
				for (Update ui: updates)
					compAPI.addComposedUpdate(ui, this);
			} else
				
			// Case 2
			if (isLocUpdatedWithAddRemove_Set2 && compAPI.isLocUpdatedWithActions(2, l, Update.UPDATE_ACTION)) {
				UpdateMultiset updates = filterUpdates(compAPI.getLocUpdates(2, l));
				for (Update ui: updates)
					compAPI.addComposedUpdate(ui, this);
			} else
				
			// Case 3a
			if (isLocUpdatedWithAddRemove_Set2 &&
					compAPI.isLocUpdatedWithActions(1, l, Update.UPDATE_ACTION)) {
				compAPI.addComposedUpdate(aggregateLocationForComposition(l, compAPI), this);
			} else 
				
			// Case 3b
			if (isLocUpdatedWithAddRemove_Set1 && isLocUpdatedWithAddRemove_Set2) {
				compAPI.addComposedUpdate(composeBagIncrementalUpdates(l, compAPI), this);
			}
		}
	}
	 
	/*
	 * Filters the given updates, such that the resulting update set has only one 
	 * bag update action for every location. 
	 */
	public UpdateMultiset filterUpdates(UpdateMultiset updates) {
		Map<Location, List<BagAbstractUpdateElement>> map = new HashMap<Location, List<BagAbstractUpdateElement>>();
		Map<Location, Set<Element>> contributingAgents = new HashMap<Location, Set<Element>>();
		Map<Location, Set<ScannerInfo>> contributingNodes = new HashMap<Location, Set<ScannerInfo>>();
		UpdateMultiset result = new UpdateMultiset();
		
		for (Update u: updates) {
			if (u.action.equals(BAG_UPDATE_ACTION)) {
				List<BagAbstractUpdateElement> list = map.get(u.loc);
				Set<Element> agents = contributingAgents.get(u.loc);
				Set<ScannerInfo> nodes = contributingNodes.get(u.loc);
				if (list == null) {
					list = new ArrayList<BagAbstractUpdateElement>();
					agents = new HashSet<Element>();
					nodes = new HashSet<ScannerInfo>();
					map.put(u.loc, list);
					contributingAgents.put(u.loc, agents);
					contributingNodes.put(u.loc, nodes);
				}
				list.add((BagAbstractUpdateElement)u.value);
				agents.addAll(u.agents);
				nodes.addAll(u.sources);
			} else
				result.add(u);
		}
		
		for (Location l: map.keySet()) 
			result.add(new Update(l, new BagUpdateContainer(map.get(l)), BAG_UPDATE_ACTION, 
					contributingAgents.get(l), contributingNodes.get(l)));
		
		return result;
	}
	

	/*
	 * Aggregates updates on location l, knowing that the first set of updates
	 * has an absolute (regular) update on l. The result will be a regular update
	 * on l.
	 */
	private Update aggregateLocationForComposition(Location l, PluginCompositionAPI compAPI) {
		Element value = null;
		UpdateMultiset uMset1 = compAPI.getLocUpdates(1, l);
		UpdateMultiset uMset2 = compAPI.getLocUpdates(2, l);
		Update result = null;
		Set<Element> contributingAgents = new HashSet<Element>();
		Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();
		
		// get the value of a basic update on location 'l'
		// at this point, there should only be one value or 
		// there will be an inconsistent update error issued somewhere else
		for (Update ui: uMset1)
			if (ui.action.equals(Update.UPDATE_ACTION)) {
				value = ui.value;
				contributingAgents.addAll(ui.agents);
				contributingNodes.addAll(ui.sources);
				break;
			}
		
		// value should be a bag
		if (value instanceof BagElement) {
			Collection<BagAbstractUpdateElement> temp = new HashSet<BagAbstractUpdateElement>();
			
			for (Update u: uMset2) 
				if (u.action.equals(BAG_UPDATE_ACTION)) {
					temp.add((BagAbstractUpdateElement)u.value);
					contributingAgents.addAll(u.agents);
					contributingNodes.addAll(u.sources);
				}
			
			BagUpdateContainer bagUpdates = new BagUpdateContainer(temp);

			BagElement newBag = bagUpdates.aggregateUpdates((BagElement)value);

			//TODO This needs to be tested
			result = new Update(l, newBag, Update.UPDATE_ACTION, contributingAgents, contributingNodes);
			
		} else
			logger.error("Value is not a bag in BagPlugin composition.");
		
		return result;
	}

	/*
	 * Composes two bag updates on a location.
	 */
	private Update composeBagIncrementalUpdates(Location l, PluginCompositionAPI compAPI) {
		UpdateMultiset uMset1 = filterUpdates(compAPI.getLocUpdates(1, l));
		UpdateMultiset uMset2 = filterUpdates(compAPI.getLocUpdates(2, l));
		Set<Element> contributingAgents = new HashSet<Element>();
		Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();
		
		BagAbstractUpdateElement update1 = null;
		BagAbstractUpdateElement update2 = null;

		for (Update u1: uMset1) 
			if (u1.action.equals(BAG_UPDATE_ACTION)) {
				update1 = (BagAbstractUpdateElement)u1.value;
				contributingAgents.addAll(u1.agents);
				contributingNodes.addAll(u1.sources);
			}
		for (Update u2: uMset2) 
			if (u2.action.equals(BAG_UPDATE_ACTION)) {
				update2 = (BagAbstractUpdateElement)u2.value;
				contributingAgents.addAll(u2.agents);
				contributingNodes.addAll(u2.sources);
			}
		
		return new Update(l, BagUpdateContainer.compose(update1, update2), BAG_UPDATE_ACTION, contributingAgents, contributingNodes);
	}
	
	// ---- Checks and Resultant Update creation when INCREMENTAL UPDATES AND REGULAR UPDATES
	
	/**
	 * Return true if a regular update on location is not a bag
	 * 
	 * @param loc The location where we need to check if a bag regular update was made.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>boolean</code> true value if not a bag, and false otherwise.
	 */
	private boolean regularUpdateIsNotBag(Location loc, PluginAggregationAPI pluginAgg)
	{
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// for all updates
		for (Update u : locUpdates)
			// if this update is a regular update
			if (u.action.equals(Update.UPDATE_ACTION))
				if (!(u.value instanceof BagElement))
						return true;
		
		// otherwise return false
		return false;
	}
	
	/**
	 * Return true if there is an add or remove conflict with the bag regular update value
	 * 
	 * @param loc The location where we need to check if a bag regular update and add/remove conflict was made.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>boolean</code> true value if add/remove conflict with regular update, and false otherwise.
	 */
	private boolean addRemoveConflictWithRU(Location loc, PluginAggregationAPI pluginAgg)
	{
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// for all updates
		for (Update u : locUpdates)
			// if there is a bag add or remove action on this location, it is an inconsistency
			if (u.action.equals(BAG_UPDATE_ACTION))
				return true;

		// otherwise return false
		return false;
	}
	
	/**
	 * Get any one of regular updates from the multiset, and mark all updates as successfully aggregated.
	 * 
	 * @param loc The location being updated.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>Update</code> and update representing the regular update
	 */
	private Update getRegularUpdate(Location loc, PluginAggregationAPI pluginAgg)
	{
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// regular update to be returned
		Update regularUpdate = null;
		
		
		// all updates added successfully, so flag them
		for (Update u : locUpdates)
		{
			
			// if this update is a regular update and no regular update found to return yet
			if (regularUpdate == null && u.action.equals(Update.UPDATE_ACTION))
				// store it for return to the plugin
				regularUpdate = u;
		
			// flag update aggregation as successful for this update
			pluginAgg.flagUpdate(u,Flag.SUCCESSFUL,this);
		}
		
		// return resultant set
		return regularUpdate;
	}
	
	
	// ---- Checks and Resultant Update creation when ONLY INCREMENTAL UPDATES
	
	/**
	 * Return true if a bag is not in the location
	 * 
	 * @param loc The location where we need to check if a bag currently resides.
	 * 
	 * @return <code>boolean</code> true value if there is not a bag at the location, and false otherwise.
	 */
	private boolean bagNotInLocation(Location loc)
	{
		// get contents of location in question
		Element e;
		try 
		{
			e = capi.getStorage().getValue(loc);
		} catch (InvalidLocationException ex) 
		{
			// Should never happen
			throw new EngineError("Cannot perform  bag-add/bag-remove actions on a non-bag location.");
		}
				
		// if location contains a bag, return false
		if (e instanceof BagElement)
			return false;
		// else return true
		else
			return true;	
		
	}
	
	/**
	 * Updates are only of the incremental variety, so build resultant set from the
	 * updates and put it in resultant update to be returned. Mark all updates as
	 * successfully aggregated
	 * 
	 * @param loc The location being updated.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>Update</code> and update representing the resultant update
	 */
	private Update buildResultantUpdate(Location loc, PluginAggregationAPI pluginAgg)
	{
		Set<Element> contributingAgents = null;
		Set<ScannerInfo> contributingNodes = null;

		// updates for this location
		UpdateMultiset locUpdates = filterUpdates(pluginAgg.getLocUpdates(loc));
		
		// get bag element at current location
		BagElement existingBag;
		try {
			existingBag = (BagElement)capi.getStorage().getValue(loc);
		} catch (InvalidLocationException ex) {
			// Should never happen
			throw new EngineError("Cannot perform bag-add/bag-remove actions on a non-bag location.");
		}

		BagUpdateContainer bagUpdates = null;
		
		for (Update u: locUpdates) 
			if (u.action.equals(BAG_UPDATE_ACTION)) {
				bagUpdates = (BagUpdateContainer)u.value; // as we filtered them, it will be container
				contributingAgents = u.agents;
				contributingNodes = u.sources;
			}
		
		BagElement newBag = bagUpdates.aggregateUpdates(existingBag);

		// all updates added successfully, so flag them
		for (Update u : pluginAgg.getLocUpdates(loc))
			pluginAgg.flagUpdate(u,Flag.SUCCESSFUL,this);
		
		// return resultant set
		return new Update(loc, newBag, Update.UPDATE_ACTION, contributingAgents, contributingNodes);

	}

	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	}

	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	public Set<String> getUniverseNames() {
		return getUniverses().keySet();
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public static class BagEnumerateParseMap extends ParserTools.ArrayParseMap {

		public BagEnumerateParseMap() {
			super(PLUGIN_NAME);
		}
		
		public Node map(Object... vals) {
			Node node = new BagEnumerateNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
	}

	public static class BagComprehensionParseMap extends ArrayParseMap {

		public BagComprehensionParseMap() {
			super(PLUGIN_NAME);
		}
		
		public Node map(Object... vals) {
			Node node = null;
			// if there is an 'is' clause
			if (vals[2] != null && vals[2] instanceof Object[])  
				node = new BagAdvancedCompNode(((Node)vals[0]).getScannerInfo());
			else
				node = new BagCompNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
	}
}
