/*	
 * SetPlugin.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Mashaal Memon
 * Copyright (C) 2006 Roozbeh Farahbod
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.engine.plugins.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.set.CompilerSetPlugin;
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
import org.coreasm.engine.plugin.Aggregator;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.plugins.collection.AbstractSetElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Plugin for finite set related rules, literals, 
 * operations, rules, aggregation and sequential composition.
 *   
 *  @author  Mashaal Memon, Roozbeh Farahbod
 *  
 */
public class SetPlugin extends Plugin 
		implements VocabularyExtender, OperatorProvider, Aggregator, 
				ParserPlugin, InterpreterPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 10, 2, "");

	protected static final Logger logger = LoggerFactory.getLogger(SetPlugin.class);
	
	public static final String PLUGIN_NAME = SetPlugin.class.getSimpleName();

	public static final String SETENUM_TOKEN = "setEnum";
	public static final String SETCOMP_TOKEN = "setComp";
	public static final String SETADVANCEDCOMP_TOKEN = "setAdvancedComp";
	public static final String SETADDTO_TOKEN = "setAddTo";
	public static final String SETREMOVEFROM_TOKEN = "setRemoveFrom";
	
	public static final String SETUNION_OP = "union";
	//public static final String SETUNION_OP_2 = "";
	public static final String SETINTERSECT_OP = "intersect";
	//public static final String SETINTERSECT_OP_2 = "";
	public static final String SETDIFF_OP = "diff";
	//public static final String SETDIFF_OP_2 = "diff";
	public static final String SETSUBSET_OP = "subset";
	//public static final String SETSUBSET_OP_2 = "⊂";
	
	// made public by Roozbeh ~ 29 July 2006
	public static final String SETADD_ACTION = "setAddAction";
	public static final String SETREMOVE_ACTION = "setRemoveAction";
	public static final String[] UPDATE_ACTIONS = {SETADD_ACTION, SETREMOVE_ACTION};
	
	private final String[] keywords = {"union", "intersect", "diff", "is", "in", "with", "subset"};
	private final String[] operators = {"{", "|", "}", ","};
	
	static final String NAME = PLUGIN_NAME;
	
	/* keeps track of to-be-considered values in a set comprehension */
	private ThreadLocal<Map<ASTNode, Collection<Map<String,Element>>>> tobeConsidered;
	
	/* keeps new sets created on a set comprehension node */
	private ThreadLocal<Map<ASTNode, Set<Element>>> newSet;
	
	private SetCardinalityFunctionElement setCardinalityFunction;
	private SetBackgroundElement setBackground;

	private Set<String> dependencyNames = null;
	private Map<String,FunctionElement> functions = null;
	private Map<String,BackgroundElement> backgrounds = null;
	private Map<String, GrammarRule> parsers = null;
	
	Parser.Reference<Node> refSetTermParser = Parser.newReference();
	
	private CompilerPlugin compilerPlugin = new CompilerSetPlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
	public SetPlugin() {
		super();
	}


	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	public void initialize() {
		tobeConsidered= new ThreadLocal<Map<ASTNode,Collection<Map<String,Element>>>>() {
			@Override
			protected Map<ASTNode, Collection<Map<String, Element>>> initialValue() {
				return new IdentityHashMap<ASTNode, Collection<Map<String,Element>>>();
			}
		};
		newSet= new ThreadLocal<Map<ASTNode,Set<Element>>>() {
			@Override
			protected Map<ASTNode, Set<Element>> initialValue() {
				return new IdentityHashMap<ASTNode, Set<Element>>();
			}
		};
		setCardinalityFunction = new SetCardinalityFunctionElement(capi);
		setBackground = new SetBackgroundElement();
	}

	/*
	 * Returns the instance of 'tobeConsidered' map for this thread.
	 */
	private Map<ASTNode, Collection<Map<String, Element>>> getToBeConsideredMap() {
		return tobeConsidered.get();
	}
	
	/*
	 * Returns the instance of 'newSet' map for this thread.
	 */
	private Map<ASTNode, Set<Element>> getNewSetMap() {
		return newSet.get();
	}
	
	@Override
	public Set<String> getDependencyNames() {
		if (dependencyNames == null) {
			dependencyNames = new HashSet<String>();
			dependencyNames.add("CollectionPlugin");
		}
		return dependencyNames;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.Plugin#interpret(org.coreasm.engine.interpreter.ASTNode)
	 */
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) {
		ASTNode nextPos = pos;
		String gClass = pos.getGrammarClass();
        
		Map<ASTNode, Collection<Map<String, Element>>> tobeConsidered = getToBeConsideredMap();
		Map<ASTNode, Set<Element>> newSet = getNewSetMap();
		
		// if set related expression
		if (gClass.equals(ASTNode.EXPRESSION_CLASS))
		{
//			if ((x != null) && (x.equals(SETENUM_TOKEN))) {
			if (pos instanceof SetEnumerateNode) {
				// set enumeration wrapper
				SetEnumerateNode seNode = (SetEnumerateNode)pos;
					
				nextPos = seNode.getUnevaluatedMember();
					
				// no unevaluated members
				if (nextPos == null)
				{
					// set next pos to current position
					nextPos = pos;
					
					Set<Element> elements = new HashSet<Element>();
					
					// for each member node
					for (ASTNode n : seNode.getAllMembers())
					{
						// get element value and add it to the set element
						elements.add(n.getValue());
					}
					
					// result of this node is the set element produced
					pos.setNode(null,null,new SetElement(elements));
				}		
	        }
			
			// The following code block is developed by Roozbeh Farahbod
			//
			// if the node is an advanced set comprehension (expression specifier) ...
			else if (pos instanceof SetCompNode) {
				SetCompNode node = (SetCompNode)pos;
				Map<String,ASTNode> bindings = null;
				
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
									pos.setNode(null, null, new SetElement());
									return pos;
								}
						}
						
						// else
					
						// TODO Incosistent with the spec
						// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						// !!!                                          !!!
						// !!! FROM THIS POINT, IT DEFERS FROM THE SPEC !!!
						// !!!                                          !!!
						// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						
						// create the resulting set
						newSet.put(pos, new HashSet<Element>());
						
						// Set of all possible bindings
						HashSet<Map<String,Element>> possibleBindings = new HashSet<Map<String,Element>>();
						
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
						tobeConsidered.put(pos, possibleBindings);
						
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
						Collection<Map<String,Element>> possibleBindings = tobeConsidered.get(pos);
						
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
							pos.setNode(null, null, new SetElement(newSet.get(pos)));
						}
							
					}
				} 
				
				// if everything is evaluated
				else {
					// remove previous bindings
					unbindVariables(interpreter, bindings.keySet());

					Set<Element> result = newSet.get(pos);
					result.add(expression.getValue());
					// get the remaining combinations
					Collection<Map<String,Element>> possibleBindings = tobeConsidered.get(pos);
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
						pos.setNode(null, null, new SetElement(newSet.get(pos)));
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
			HashSet<Map<String,Element>> possibleBindings, 
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
			return refSetTermParser.lazy();
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

			// SetEnumerate: '{' ( Term (',' Term)* )? '}'
			Parser<Node> setEnumerateParser = Parsers.array(
					new Parser[] {
							pTools.getOprParser("{"),
							pTools.csplus(termParser).optional(),
							pTools.getOprParser("}")
							}).map(
					new SetEnumerateParseMap());
			parsers.put("SetEnumerate", 
					new GrammarRule("SetEnumerate",
							"'{' Term (',' Term)* '}'", setEnumerateParser, PLUGIN_NAME));
			
			// SetComprehension: '{' (ID 'is')? Term '|' ID 'in' Term 
			//                    ( ',' ID 'in' Term )* ( 'with' Guard )? '}'
			Parser<Node> setComprehensionParser = Parsers.or(
				Parsers.array(new Parser[] {
					pTools.getOprParser("{"),
					termParser,
					pTools.getOprParser("|"),
					pTools.csplus(Parsers.array(
						idParser,
						pTools.getKeywParser("in", PLUGIN_NAME),
						termParser)),
					Parsers.array(
						pTools.getKeywParser("with", PLUGIN_NAME),
						guardParser).optional(),
					pTools.getOprParser("}")
				}),
				Parsers.array(new Parser[] {
					pTools.getOprParser("{"),
					Parsers.array(
						idParser,
						pTools.getKeywParser("is", PLUGIN_NAME)),
					termParser,
					pTools.getOprParser("|"),
					pTools.csplus(Parsers.array(
						idParser,
						pTools.getKeywParser("in", PLUGIN_NAME),
						termParser)),
					Parsers.array(
						pTools.getKeywParser("with", PLUGIN_NAME),
						guardParser).optional(),
					pTools.getOprParser("}")
				})
			).map(new SetComprehensionParseMap());
			parsers.put("SetComprehension", 
					new GrammarRule("SetComprehension",
							"'{' (ID 'is')? Term '|' ID 'in' Term ( ',' ID 'in' Term )* ( 'with' Guard )? '}'", 
							setComprehensionParser, PLUGIN_NAME));
			
			Parser<Node> setTermParser = Parsers.or(setEnumerateParser, setComprehensionParser);
			refSetTermParser.set(setTermParser);
			
			// BasicTerm : SetEnumerate | SetComprehension | ...
			parsers.put("BasicTerm",
					new GrammarRule("SetBasicTerm", 
							"SetEnumerate | SetComprehension", 
							refSetTermParser.lazy(), PLUGIN_NAME));
			
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
			functions.put(
					SetCardinalityFunctionElement.SET_CARINALITY_FUNCTION_NAME, 
					setCardinalityFunction);
			functions.put(ToSetFunctionElement.NAME, new ToSetFunctionElement());
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
			backgrounds.put(SetBackgroundElement.SET_BACKGROUND_NAME, setBackground);
		}
		return backgrounds;
	}
	
	//--------------------------------
	// Operator Implementor Interface
	//--------------------------------

	public Collection<OperatorRule> getOperatorRules() {
		
		ArrayList<OperatorRule> opRules = new ArrayList<OperatorRule>();
		
		opRules.add(new OperatorRule(SETINTERSECT_OP,
				OpType.INFIX_LEFT,
				675,
				NAME));
		
		opRules.add(new OperatorRule(SETDIFF_OP,
				OpType.INFIX_LEFT,
				650,
				NAME));
		
		opRules.add(new OperatorRule(SETUNION_OP,
				OpType.INFIX_LEFT,
				650,
				NAME));
		
		opRules.add(new OperatorRule(SETSUBSET_OP,
				OpType.INFIX_LEFT,
				700,
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
			
			// new set element holds result
			Set<Element> resultSet = new HashSet<Element>();
			
			// get operand nodes
			ASTNode alpha = opNode.getFirst();
			ASTNode beta = alpha.getNext();
			
			// get operand values
			Element l = alpha.getValue();
			Element r = beta.getValue();
			
			// set subset (is special and applies to all enumerables)
			if (x.equals(SETSUBSET_OP))	{
				// confirm that operands are enumerables or undef
				if ((l instanceof Enumerable || l.equals(Element.UNDEF))
						&& (r instanceof Enumerable || r.equals(Element.UNDEF))) {
					if (l instanceof Enumerable && r instanceof Enumerable) {
						// get enumerable interface to operands
						Enumerable eL = (Enumerable)l;
						Enumerable eR = (Enumerable)r;
						
						result = BooleanElement.valueOf(eR.enumerate().containsAll(eL.enumerate()));
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
				return result;
			} 

			//for other operators
			if ((l instanceof AbstractSetElement || l.equals(Element.UNDEF))
					&& (r instanceof AbstractSetElement || r.equals(Element.UNDEF))) {
				if (l instanceof AbstractSetElement && r instanceof AbstractSetElement) {
					// get enumerable interface to operands
					Enumerable eL = (Enumerable)l;
					Enumerable eR = (Enumerable)r;
					
					// set intersection
					if (x.equals(SETINTERSECT_OP))
					{
						// add elements which are in both
						for (Element m : eL.enumerate())
							if (eR.enumerate().contains(m))
								resultSet.add(m);
					}
					// set difference
					else if (x.equals(SETDIFF_OP))
					{
						// add elements which are not in the second
						for (Element m : eL.enumerate())
							if (!eR.enumerate().contains(m))
								resultSet.add(m);
							
					}
					// set union
					else if (x.equals(SETUNION_OP))
					{
						// add elements from both
						for (Element m : eL.enumerate())
							resultSet.add(m);
						for (Element m : eR.enumerate())
							resultSet.add(m);
					}
					
					if (result == null)
						result = new SetElement(resultSet);
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
		
		// all locations on which contain set incremental updates
		Set<Location> locsToAggregate = pluginAgg.getLocsWithAnyAction(SETADD_ACTION,SETREMOVE_ACTION);
		
		// for all locations to aggregate
		for (Location l : locsToAggregate)
		{
			// if regular update affects this location
			if (pluginAgg.regularUpdatesAffectsLoc(l))
			{
				// Case 1a
				// if regular updates are inconsitent, then aggregation inconsistency
				if (pluginAgg.inconsistentRegularUpdatesOnLoc(l))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// Case 1b
				// if regular update is not a set, then aggregation inconsistency
				else if (regularUpdateIsNotSet(l,pluginAgg))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// Case 1c
				// if add/remove are inconsistent with regular update, then aggregation inconsistency
				else if (addRemoveConflictWithRU(l,pluginAgg))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// otherwise set aggregation can be performed
				else
					// get regular update to add to resultant updates set
					pluginAgg.addResultantUpdate(getRegularUpdate(l,pluginAgg),this);
			}
			// else only partial updates affect this location
			else
			{	// Case 2a
				// if set add/remove failure, then aggregation inconsistency
				if (addRemoveConflict(l,pluginAgg))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// Case 2b
				// if set not currently at the location, then aggregation inconsistency
				else if (setNotInLocation(l))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// otherwise set aggregation can be performed
				else
					// get resultant update to add to resultant updates set
					pluginAgg.addResultantUpdate(buildResultantUpdate(l,pluginAgg),this);
			}	
		}	
	}


	/**
	 * Implemented by Roozbeh Farahbod.
	 * 
	 * @see org.coreasm.engine.plugin.Aggregator#compose(PluginCompositionAPI)
	 */
	public void compose(PluginCompositionAPI compAPI) {
		for (Location l: compAPI.getAffectedLocations()) {
			
			boolean isLocUpdatedWithAddRemove_Set1 = 
				compAPI.isLocUpdatedWithActions(1, l, SETADD_ACTION, SETREMOVE_ACTION);
			boolean isLocUpdatedWithAddRemove_Set2 = 
				compAPI.isLocUpdatedWithActions(2, l, SETADD_ACTION, SETREMOVE_ACTION);
			
			// Case 1a
			if (isLocUpdatedWithAddRemove_Set1 && !compAPI.isLocationUpdated(2, l)) {
				for (Update ui: compAPI.getLocUpdates(1, l))
					compAPI.addComposedUpdate(ui, this);
			} else 

				// Case 1b
				if (isLocUpdatedWithAddRemove_Set2 && !compAPI.isLocationUpdated(1, l)) {
					for (Update ui: compAPI.getLocUpdates(2, l))
						compAPI.addComposedUpdate(ui, this);
				} else
					
					// Case 2
					if (isLocUpdatedWithAddRemove_Set2 && compAPI.isLocUpdatedWithActions(2, l, Update.UPDATE_ACTION)) {
						for (Update ui: compAPI.getLocUpdates(2, l))
							compAPI.addComposedUpdate(ui, this);
					} else
						
						// Case 3a
						if (isLocUpdatedWithAddRemove_Set2 &&
								compAPI.isLocUpdatedWithActions(1, l, Update.UPDATE_ACTION)) {
							compAPI.addComposedUpdate(aggregateLocationForComposition(l, compAPI), this);
						} else 
							
							// Case 3b
							if (isLocUpdatedWithAddRemove_Set1 && isLocUpdatedWithAddRemove_Set2) {
								for (Update ui: eradicateConflictingIncrementalUpdates(l, compAPI))
									compAPI.addComposedUpdate(ui, this);
							}
		}
	}
	 

	private Update aggregateLocationForComposition(Location l, PluginCompositionAPI compAPI) {
		Element value = null;
		UpdateMultiset uMset1 = compAPI.getLocUpdates(1, l);
		UpdateMultiset uMset2 = compAPI.getLocUpdates(2, l);
		Set<Element> contributingAgents = new HashSet<Element>();
		Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();
		
		// get the value of the basic update on location 'l'
		// TODO what if there are more than two such updates?
		for (Update ui: uMset1)
			if (ui.action.equals(Update.UPDATE_ACTION)) {
				value = ui.value;
				contributingAgents.addAll(ui.agents);
				contributingNodes.addAll(ui.sources);
				break;
			}

		// value should be a set
		if (value instanceof SetElement) {
			Set<Element> resultSet = new HashSet<Element>(((SetElement)value).enumerate());
			
			for (Element e: resultSet) {
				Update removeUpdate = new Update(l, e, SETREMOVE_ACTION, (Element)null, null);
				
				if (!uMset2.contains(removeUpdate)) 
					resultSet.add(e);
			}
			
			for (Update u: uMset2) {
				if (u.action.equals(SETADD_ACTION)) 
					resultSet.add(u.value);
				contributingAgents.addAll(u.agents);
				contributingNodes.addAll(u.sources);
			}
			
			return new Update(l, new SetElement(resultSet), Update.UPDATE_ACTION, contributingAgents, contributingNodes);
		} else
			logger.error("Value is not a set (in SetPlugin Composition).");
		
		return null;
	}
	
	private UpdateMultiset eradicateConflictingIncrementalUpdates(Location l, PluginCompositionAPI compAPI) {
		UpdateMultiset remainingUpdates = new UpdateMultiset();
		Set<Element> locationValues = new HashSet<Element>();
		UpdateMultiset uMset1 = compAPI.getLocUpdates(1, l);
		UpdateMultiset uMset2 = compAPI.getLocUpdates(2, l);
		
		// Preparing the locationValues set
		for (Update u: uMset1)
			locationValues.add(u.value);
		for (Update u: uMset2)
			locationValues.add(u.value);
			
		for (Element e: locationValues) {
			Update updateAdd = new Update(l, e, SETADD_ACTION, (Element)null, null);
			Update updateRemove = new Update(l, e, SETREMOVE_ACTION, (Element)null, null);
			
			// Case 3(b)i
			if (uMset1.contains(updateAdd) && uMset2.contains(updateRemove))
				; // skip
			else
				
				// Case 3(b)ii
				if (uMset1.contains(updateRemove) && uMset2.contains(updateAdd)) 
					// deviating from the spec as there is no need for forall here
					remainingUpdates.add(updateAdd);
				else {
					
					// Case 3(b)iii
					for (Update ui: uMset1)
						if (ui.value.equals(e))
							remainingUpdates.add(ui);
					for (Update ui: uMset2)
						if (ui.value.equals(e))
							remainingUpdates.add(ui);
				}
		}

		return remainingUpdates;
	}
	
	// ---- Checks and Resultant Update creation when INCREMENTAL UPDATES AND REGULAR UPDATES
	
	/**
	 * Return true if a regular update on location is not a set
	 * 
	 * @param loc The location where we need to check if a set regular update was made.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>boolean</code> true value if not a set, and false otherwise.
	 */
	private boolean regularUpdateIsNotSet(Location loc, PluginAggregationAPI pluginAgg)
	{
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// for all updates
		for (Update u : locUpdates)
			// if this update is a regular update
			if (u.action.equals(Update.UPDATE_ACTION))
				if (!(u.value instanceof SetElement))
						return true;
		
		// otherwise return false
		return false;
		
	}
	
	/**
	 * Return true if there is an add or remove conflict with the set regular upate value
	 * 
	 * @param loc The location where we need to check if a set regular update and add/remove conflict was made.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>boolean</code> true value if add/remove conflict with regular update, and false otherwise.
	 */
	private boolean addRemoveConflictWithRU(Location loc, PluginAggregationAPI pluginAgg)
	{
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// the set regular update value
		SetElement ruValue = null; 
		
		// for all updates
		for (Update u : locUpdates)
			// if this update is a regular update
			if (u.action.equals(Update.UPDATE_ACTION))
			{
				// store it
				ruValue = (SetElement)u.value;
				break;
			}
		
		// enumerable view of the RU value 
		Collection<? extends Element> enumerableViewRUValue = ((Enumerable)ruValue).enumerate();
		
		// for all updates
		for (Update u : locUpdates)
			// if there is a set add of a value but RU set does not contain it, there is a conflict
			if (u.action.equals(SETADD_ACTION) && !enumerableViewRUValue.contains(u.value))
				return true;
			// else if there is a set remove of a value but RU set does contain it, there is a conflict
			else if (u.action.equals(SETREMOVE_ACTION) && enumerableViewRUValue.contains(u.value))
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
	 * Return true if a setAddAction and setRemoveAction operator on the location
	 * 
	 * @param loc The location where we need to check if a set currently resides.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>boolean</code> true value if there is a conflict, and false otherwise.
	 */
	private boolean addRemoveConflict(Location loc, PluginAggregationAPI pluginAgg)
	{
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// get all values of update instruction present in the multiset
		HashSet<Element> updateValues = new HashSet<Element>();
		for (Update u: locUpdates)
			updateValues.add(u.value);
		
		// for each value
		for (Element v : updateValues)
		{
			// if a setAddAction and setRemoveAction to same location and value occur, then conflict
			if (locUpdates.contains(new Update(loc,v,SETADD_ACTION, (Element)null, null)) 
					&& locUpdates.contains(new Update(loc,v,SETREMOVE_ACTION, (Element)null, null)))
				return true;
		}
		
		// if we reach this point, that means there's no conflict
		return false;
		
	}
	
	/**
	 * Return true if a SetElement (set) is not in the location
	 * 
	 * @param loc The location where we need to check if a set currently resides.
	 * 
	 * @return <code>boolean</code> true value if there is not a set at the location, and false otherwise.
	 */
	private boolean setNotInLocation(Location loc)
	{
		// get contents of location in question
		Element e;
		try 
		{
			e = capi.getStorage().getValue(loc);
		} catch (InvalidLocationException ex) 
		{
			// Should never happen
			throw new EngineError("Cannot perform  set-add/set-remove actions on a non-set location.");
		}
				
		// if location contains a set, return false
		if (e instanceof SetElement)
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
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// get set element at current location
		SetElement existingSet;
		try 
		{
			existingSet = (SetElement)capi.getStorage().getValue(loc);
		} catch (InvalidLocationException ex) 
		{
			// Should never happen
			throw new EngineError("Location to which set incremental update has been made is invalid!");
		}
		Enumerable enumerableViewExistingSet = (Enumerable)existingSet;
		
		// resultant set element 
		Set<Element> resultantSet = new HashSet<Element>();
		Set<Element> contributingAgents = new HashSet<Element>();
		Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();
		//SetElement resultantSet = (SetElement)setBackground.getNewValue();
		
		// add all existing elements less those removed with setRemoveAction
		for (Element e : enumerableViewExistingSet.enumerate()) {
			Update update = new Update(loc, e, SETREMOVE_ACTION, (Element)null, null);
			if (!locUpdates.contains(update)) 
				resultantSet.add(e);
		}
		
		// add all values resulting from setAddAction
		for (Update u : locUpdates)
			if (u.action.equals(SETADD_ACTION))
				resultantSet.add(u.value);
		
		// all updates added successfully, so flag them
		// and add their agents to the contributing agent set
		for (Update u : locUpdates) {
			pluginAgg.flagUpdate(u,Flag.SUCCESSFUL,this);
			contributingAgents.addAll(u.agents);
			contributingNodes.addAll(u.sources);
		}
	
		// return resultant set
		return new Update(loc, new SetElement(resultantSet), Update.UPDATE_ACTION, contributingAgents, contributingNodes);
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

	public static class SetEnumerateParseMap extends ParserTools.ArrayParseMap {

		public SetEnumerateParseMap() {
			super(PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] vals) {
			Node node = new SetEnumerateNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
	}

	public static class SetComprehensionParseMap extends ParserTools.ArrayParseMap {

		public SetComprehensionParseMap() {
			super(PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] vals) {
			Node node = null;
			// if there is an 'is' clause
			if (vals[1] != null && vals[1] instanceof Object[]) {
				Object[] newVals = new Object[vals.length - 1];
				newVals[0] = vals[0];
				for (int i = 1; i < newVals.length; i++)
					newVals[i] = vals[i + 1];
				vals = newVals;
			}
			node = new SetCompNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
	}
}
