/*	
 * ListPlugin.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.list.CompilerListPlugin;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;

/** 
 * A plugin to provide lists of elements.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ListPlugin extends Plugin implements ParserPlugin, 
				InterpreterPlugin, OperatorProvider, VocabularyExtender {

	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 1, 1, "beta");
	
	public static final String PLUGIN_NAME = ListPlugin.class.getSimpleName();
	
	public static final String LIST_OPEN_SYMBOL_1 = "[";
	public static final String LIST_CLOSE_SYMBOL_1 = "]";
//	public static final String LIST_OPEN_SYMBOL_2 = "<<";
//	public static final String LIST_CLOSE_SYMBOL_2 = ">>";
	public static final String LIST_INSERT_KEYWORD = "insert";
	public static final String LIST_DELETE_KEYWORD = "delete";
	
//	public static final String LIST_ADD_ACTION = "ListAddAction";
//	public static final String LIST_REMOVE_ACTION = "ListRemoveAction";
//	public static final String[] UPDATE_ACTIONS = {
//			LIST_ADD_ACTION, 
//			LIST_REMOVE_ACTION};
	
	public static final String LIST_CONCAT_OP = "+";
	
	/* keeps track of to-be-considered values in a list comprehension */
	private ThreadLocal<Map<ASTNode, List<Map<String,Element>>>> tobeConsidered;
	
	/* keeps new lists created on a list comprehension node */
	private ThreadLocal<Map<ASTNode, List<Element>>> newList;
	
	private Map<String, GrammarRule> parsers = null;
	private List<OperatorRule> operatorRules = null;
	private HashSet<String> depencyList = new HashSet<String>();
	
	Parser.Reference<Node> refListTermParser = Parser.newReference();
	
	private Map<String, BackgroundElement> backgrounds = null;
	private Map<String, FunctionElement> functions = null;

	private final String[] keywords = {"shift", "left", "right", "into", "in", "with"};
	private final String[] operators = {"<<", ">>", "[", "]", ",", "+"};
	
	private final CompilerPlugin compilerPlugin = new CompilerListPlugin();
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
	public ListPlugin() {
		super();
		depencyList.add("CollectionPlugin");
		depencyList.add("NumberPlugin");
	}
	

	@Override
	public String[] getKeywords() {
		return keywords;
	}

	@Override
	public String[] getOperators() {
		return operators;
	}

	@Override
	public Set<String> getDependencyNames() {
		return depencyList;
	}

	@Override
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}
	
	@Override
	public Parser<Node> getParser(String nonterminal) {
		if (nonterminal.equals("ListTerm"))
			return refListTermParser.lazy();
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
		tobeConsidered= new ThreadLocal<Map<ASTNode,List<Map<String,Element>>>>() {
			@Override
			protected Map<ASTNode, List<Map<String, Element>>> initialValue() {
				return new IdentityHashMap<ASTNode, List<Map<String,Element>>>();
			}
		};
		newList= new ThreadLocal<Map<ASTNode,List<Element>>>() {
			@Override
			protected Map<ASTNode, List<Element>> initialValue() {
				return new IdentityHashMap<ASTNode, List<Element>>();
			}
		};
	}
	
	/*
	 * Returns the instance of 'tobeConsidered' map for this thread.
	 */
	private Map<ASTNode, List<Map<String, Element>>> getToBeConsideredMap() {
		return tobeConsidered.get();
	}
	
	/*
	 * Returns the instance of 'newList' map for this thread.
	 */
	private Map<ASTNode, List<Element>> getNewListMap() {
		return newList.get();
	}

	@Override
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> termParser = kernel.getTermParser();
			Parser<Node> guardParser = kernel.getGuardParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			
			Parser<Object[]> csTerms = pTools.csplus(termParser);
			
			// ListComprehension: '[' Term '|' ID 'in' Term 
			//                    ( ',' ID 'in' Term )* ( 'with' Guard )? ']'
			
			
			Parser<Node> listComprehensionParser = Parsers.array(new Parser[] {
				pTools.getOprParser("["),
				termParser,
				pTools.getOprParser("|"),
				pTools.csplus(Parsers.array(
					idParser,
					pTools.getKeywParser("in", PLUGIN_NAME),
					termParser)),
				Parsers.array(
					pTools.getKeywParser("with", PLUGIN_NAME),
					guardParser).optional(),
				pTools.getOprParser("]")
			}).map(new ListComprehensionParseMap());
			parsers.put("ListComprehension", 
					new GrammarRule("ListComprehension",
							"'[' Term '|' ID 'in' Term ( ',' ID 'in' Term )* ( 'with' Guard )? ']'", 
							listComprehensionParser, PLUGIN_NAME));
			
			// [ Term, ..., Term ]
			Parser<Object[]> listTermParser1 = Parsers.array(
					pTools.getOprParser(LIST_OPEN_SYMBOL_1),
					csTerms.optional(),
					pTools.getOprParser(LIST_CLOSE_SYMBOL_1));
			
			// << Term, ..., Term >>
			/*
			Parser<Object[]> listTermParser2 = pTools.seq(
					pTools.getOprParser(LIST_OPEN_SYMBOL_2),
					optionalDelim,
					csTerms.optional(),
					optionalDelim,
					pTools.getOprParser(LIST_CLOSE_SYMBOL_2));
			*/ 
			
			// ListTerm : '[' Term,...,Term ']' | '<<' Term, ..., Term '>>'
			//listTermParserArray[0] = Parsers.mapn("ListTerm",
//					new Parser[] {Parsers.alt(listTermParser1, listTermParser2)},
					
			Parser<Node> listtermParser = Parsers.array(
					new Parser[] {listTermParser1}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node map(Object[] vals) {
							Node node = new ListTermNode();
							addChildren(node, vals);
							node.setScannerInfo(node.getFirstCSTNode());
							return node;
						}

						@Override
						public void addChild(Node parent, Node child) {
							if (child instanceof ASTNode) 
								parent.addChild("alpha",child);
							else
								super.addChild(parent, child);
						}
						
					});
			refListTermParser.set(Parsers.or(listtermParser, listComprehensionParser));
			parsers.put("ListTerm", new GrammarRule("ListTerm",
					"'[' ( Term ( ',' Term )* )? ']'", refListTermParser.lazy(), PLUGIN_NAME));
			
			parsers.put("BasicTerm", new GrammarRule("ListBasicTerm", 
					"ListTerm | ListComprehension", refListTermParser.lazy(), PLUGIN_NAME));

			// ShiftRule: 'shift' ('left'|'right') Term 'into' Term
			Parser<Node> shiftParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("shift", PLUGIN_NAME),
					Parsers.or(
							pTools.getKeywParser("left", PLUGIN_NAME),
							pTools.getKeywParser("right", PLUGIN_NAME)),
					termParser,
					pTools.getKeywParser("into", PLUGIN_NAME),
					termParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node map(Object[] vals) {
							boolean isLeft = ((Node)vals[1]).getToken().equals("left");
							Node node = new ShiftRuleNode(((Node)vals[0]).getScannerInfo(), isLeft);
							addChildren(node, vals);
							return node;
						}} );
			
			parsers.put("ShiftRule", 
					new GrammarRule("ShiftRule",
							"'shift' ('left' | 'right') Term 'into' Term", shiftParser, PLUGIN_NAME));
			
			parsers.put("Rule",	
					new GrammarRule("Rule", "ShiftRule", shiftParser, PLUGIN_NAME));
		}
		return parsers;
	}
	
	@Override
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
//		AbstractStorage storage = capi.getStorage();
		Map<ASTNode, List<Map<String, Element>>> tobeConsidered = getToBeConsideredMap();
		Map<ASTNode, List<Element>> newList = getNewListMap();
		
		if (pos instanceof ListTermNode) {
			ListTermNode node = (ListTermNode)pos;
			List<ASTNode> children = node.getElements();
			
			// evaluate elements
			for (ASTNode child: children)
				if (!child.isEvaluated())
					return child;
			
			List<Element> values = new ArrayList<Element>();
			for (ASTNode child: children)
				if (child.getValue() != null) 
					values.add(child.getValue());
				else {
					capi.error("Cannot add a non-value element to a list.", node, interpreter);
					return pos;
				}
			
			pos.setNode(null, null, new ListElement(values));
		}
		else if (pos instanceof ShiftRuleNode) {
			ShiftRuleNode node = (ShiftRuleNode)pos;
			
			if (!pos.getFirst().isEvaluated())
				return pos.getFirst();
			if (!pos.getFirst().getNext().isEvaluated())
				return pos.getFirst().getNext();
			Element e = node.getListNode().getValue();
			Location loc = node.getLocationNode().getLocation();
			if (e != null && e instanceof ListElement) {
				ListElement list = (ListElement)e; 
				if (node.getListNode().getLocation() != null) {
					if (loc != null)  {
						if (list.intSize() > 0) {
							UpdateMultiset updates = new UpdateMultiset();
							ArrayList<Element> listData = new ArrayList<Element>(list.enumerate());
							Element shifted = Element.UNDEF;
							
							if (node.isLeft) {
								shifted = listData.remove(0);
							} else {
								shifted = listData.remove(((ListElement)list).intSize()-1);
							}
							
							Update u1 = new Update(loc, 
									shifted, 
									Update.UPDATE_ACTION, 
									interpreter.getSelf(),
									pos.getScannerInfo());
							Update u2 = new Update(node.getListNode().getLocation(), 
									new ListElement(listData), 
									Update.UPDATE_ACTION, 
									interpreter.getSelf(),
									pos.getScannerInfo());
							
							updates.add(u1);
							updates.add(u2);

							pos.setNode(null, updates, null);
						} else
							capi.error("Cannot shift an empty list.", node.getListNode(), interpreter);
							
					} else
						capi.error("Cannot shift to a non-location.", node.getLocationNode(), interpreter);
				} else
					capi.error("Cannon shift a non-location.", node.getListNode(), interpreter);
			} else
				capi.error("Cannont shift a non-list element.", node.getListNode(), interpreter);
		}
		else if (pos instanceof ListCompNode) {
			ListCompNode node = (ListCompNode)pos;
			Map<String,ASTNode> bindings = null;
			
			// get variable to domain bindings
			try {
				bindings = node.getVarBindings();
			} catch (EngineException e) {
				// "No two constrainer variables may have the same name."
				capi.error(e);
			}
			ASTNode guard = node.getGuard();
			ASTNode expression = node.getListFunction();

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
								pos.setNode(null, null, new ListElement());
								return pos;
							}
					}
					
					// create the resulting list
					newList.put(pos, new ArrayList<Element>());
					
					// List of all possible bindings
					ArrayList<Map<String,Element>> possibleBindings = new ArrayList<Map<String,Element>>();
					
					// List of all variables
					ArrayList<String> allVariables = new ArrayList<String>(bindings.keySet());
					
					// Map of all possible values for variables 
					Map<String,ArrayList<Element>> possibleValues = new HashMap<String,ArrayList<Element>>();
					for (String var: bindings.keySet()) {
						Enumerable set = (Enumerable)(bindings.get(var).getValue());
						possibleValues.put(var, new ArrayList<Element>(set.enumerate()));
					}

					// create all possible combination of values for variables
					createAllPossibleBindings(
							allVariables, possibleValues, allVariables.size() - 1, possibleBindings, new HashMap<String,Element>());

					// set the superlist of values
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
						pos.setNode(null, null, new ListElement(newList.get(pos)));
					}
				}
			} 
			
			// if everything is evaluated
			else {
				// remove previous bindings
				unbindVariables(interpreter, bindings.keySet());

				List<Element> result = newList.get(pos);
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
					pos.setNode(null, null, new ListElement(newList.get(pos)));
					return pos;
				}
			}
			
			return pos;
		}
		else if (pos instanceof TrueGuardNode) {
			pos.setNode(null, null, BooleanElement.TRUE);
			return pos;
		}
		
		return pos;
	}

	/*
	 * This recursive method creates all the possible combinations of values
	 * for variables. 
	 */
	private void createAllPossibleBindings(
			ArrayList<String> allVariables, 
			Map<String,ArrayList<Element>> possibleValues, 
			int index, 
			ArrayList<Map<String,Element>> possibleBindings, 
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
			if (index > 0) {
				// get all the possible values for the remaining variables
				createAllPossibleBindings(
						allVariables, possibleValues, index-1, possibleBindings, currentBinding);
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

	@Override
	public Collection<OperatorRule> getOperatorRules() {
		if (operatorRules == null) {
			operatorRules = new ArrayList<OperatorRule>();
			
			operatorRules.add(new OperatorRule(LIST_CONCAT_OP,
					OpType.INFIX_LEFT,
					750,
					PLUGIN_NAME));
		}
		
		return operatorRules;
	}


	@Override
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
			
			if ((l instanceof ListElement || l.equals(Element.UNDEF)) 
					&& (r instanceof ListElement || r.equals(Element.UNDEF))) {
				
				if (l instanceof ListElement && r instanceof ListElement) {
					ListElement lList = (ListElement)l;
					ListElement rList = (ListElement)r;
					// list concatenation 
					if (x.equals(LIST_CONCAT_OP))
					{
						List<Element> list = new ArrayList<Element>(lList.getList());
						list.addAll(rList.getList());
						result = new ListElement(list);
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

	@Override
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}


//	public String[] getUpdateActions() {
//		return UPDATE_ACTIONS;
//	}
//
	@Override
	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	}

	@Override
	public Map<String, BackgroundElement> getBackgrounds() {
		if (backgrounds == null) {
			backgrounds = new HashMap<String, BackgroundElement>();
			
			backgrounds.put(ListBackgroundElement.LIST_BACKGROUND_NAME, new ListBackgroundElement());
		}
		return backgrounds;
	}

	@Override
	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	@Override
	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();
			functions.put(HeadLastFunctionElement.HEAD_FUNC_NAME, 
					new HeadLastFunctionElement(capi, true));
			functions.put(HeadLastFunctionElement.LAST_FUNC_NAME, 
					new HeadLastFunctionElement(capi, false));
			functions.put(TailFunctionElement.NAME,
					new TailFunctionElement(capi));
			functions.put(ConsFunctionElement.NAME,	new ConsFunctionElement());
			functions.put(ToListFunctionElement.NAME, new ToListFunctionElement());
			functions.put(FlattenListFunctionElement.NAME, new FlattenListFunctionElement());

			functions.put(NthFunctionElement.NAME, new NthFunctionElement());
			functions.put(TakeFunctionElement.NAME, new TakeFunctionElement(capi));
			functions.put(DropFunctionElement.NAME, new DropFunctionElement(capi));
			functions.put(ReverseFunctionElement.NAME, new ReverseFunctionElement(capi));
			
			FunctionElement indexesFunc = new IndexesFunctionElement(capi);
			functions.put(IndexesFunctionElement.NAME, indexesFunc);
			functions.put(IndexesFunctionElement.NAME_ALTERNATIVE, indexesFunc);
			
			functions.put(SetNthFunctionElement.NAME, new SetNthFunctionElement(capi));
			functions.put(ZipFunctionElement.NAME, new ZipFunctionElement(capi));
			functions.put(ZipWithFunctionElement.NAME, new ZipWithFunctionElement(capi));
			functions.put(ReplicateFunctionElement.NAME, new ReplicateFunctionElement(capi));
		}
		return functions;
	}

	@Override
	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, RuleElement> getRules() {
		return null;
	}

	@Override
	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, UniverseElement> getUniverses() {
		return null;
	}
	
	public static class ListComprehensionParseMap extends ParserTools.ArrayParseMap {

		public ListComprehensionParseMap() {
			super(PLUGIN_NAME);
		}
		
		public Node map(Object[] vals) {
			Node node = new ListCompNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
	}

	/*
	 * The following code is incomplete and is just kept for future references
	 *
	public void aggregateUpdates(PluginAggregationAPI pluginAgg) {
		// all locations on which contain list incremental updates
		Set<Location> locsToAggregate = pluginAgg.getLocsWithAnyAction(LIST_ADD_ACTION,LIST_REMOVE_ACTION);
		
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
				// if regular update is not a list, then aggregation inconsistency
				else if (regularUpdateIsNotList(l,pluginAgg))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// Case 1c
				// if add/remove are inconsistent with regular update, then aggregation inconsistency
				else if (addRemoveConflictWithRU(l,pluginAgg))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// otherwise list aggregation can be performed
				else
					// get regular update to add to resultant updates set
					pluginAgg.addResultantUpdate(getRegularUpdate(l,pluginAgg),this);
			}
			// else only partial updates affect this location
			else
			{	// Case 2a
				// if list add/remove failure, then aggregation inconsistency
				// We don't consider this a problem with lists, 
				// i.e., remove will remove all previous elements
				if (false) //(addRemoveConflict(l,pluginAgg))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// Case 2b
				// if list not currently at the location, then aggregation inconsistency
				else if (listNotInLocation(l))
					pluginAgg.handleInconsistentAggregationOnLocation(l,this);
				// otherwise list aggregation can be performed
				else
					// get resultant update to add to resultant updates set
					pluginAgg.addResultantUpdate(buildResultantUpdate(l,pluginAgg),this);
			}	
		}	
	}

	public void compose(PluginCompositionAPI compAPI) {
		for (Location l: compAPI.getAffectedLocations()) {
			
			boolean isLocUpdatedWithAddRemove_Set1 = 
				compAPI.isLocUpdatedWithActions(1, l, LIST_ADD_ACTION, LIST_REMOVE_ACTION);
			boolean isLocUpdatedWithAddRemove_Set2 = 
				compAPI.isLocUpdatedWithActions(2, l, LIST_ADD_ACTION, LIST_REMOVE_ACTION);
			
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

	/**
	 * Return true if a regular update on location is not a list element
	 * 
	 * @param loc The location where we need to check if a list regular update was made.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>boolean</code> true value if not a list, and false otherwise.
	 *
	private boolean regularUpdateIsNotList(Location loc, PluginAggregationAPI pluginAgg)
	{
		// see also the counterpart of this method in the SetPlugin
		
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// for all updates
		for (Update u : locUpdates)
			// if this update is a regular update
			if (u.action.equals(Update.UPDATE_ACTION))
				if (!(u.value instanceof ListElement))
						return true;
		
		// otherwise return false
		return false;
		
	}
	
	/**
	 * Return true if there is an add or remove conflict with the list regular upate value
	 * 
	 * @param loc The location where we need to check if a list regular update and add/remove conflict was made.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>boolean</code> true value if add/remove conflict with regular update, and false otherwise.
	 *
	private boolean addRemoveConflictWithRU(Location loc, PluginAggregationAPI pluginAgg)
	{
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// the list regular update value
		ListElement ruValue = null; 
		
		// for all updates
		for (Update u : locUpdates)
			// if this update is a regular update
			if (u.action.equals(Update.UPDATE_ACTION))
			{
				// store it
				ruValue = (ListElement)u.value;
				break;
			}

		// Build the list from incremental updates
		Element list = buildResultantUpdateFromIncrementalUpdatesOnly(loc, pluginAgg).value;
		
		return !list.equals(ruValue);
		
	}
	
	/**
	 * Get any one of regular updates from the multiset, and mark all updates as successfully aggregated.
	 * 
	 * @param loc The location being updated.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>Update</code> and update representing the regular update
	 *
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
		
		// return resultant list
		return regularUpdate;
	}
	
	
	/**
	 * Return true if a listAddAction and listRemoveAction operator on the location
	 * 
	 * @param loc The location where we need to check if a list currently resides.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>boolean</code> true value if there is a conflict, and false otherwise.
	 *
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
			// if a listAddAction and listRemoveAction to same location and value occur, then conflict
			if (locUpdates.contains(new Update(loc,v,LIST_ADD_ACTION)) && locUpdates.contains(new Update(loc,v,LIST_REMOVE_ACTION)))
				return true;
		}
		
		// if we reach this point, that means there's no conflict
		return false;
		
	}
	
	/**
	 * Return true if a ListElement is not in the location
	 * 
	 * @param loc The location where we need to check if a list currently resides.
	 * 
	 * @return <code>boolean</code> true value if there is not a list at the location, and false otherwise.
	 *
	private boolean listNotInLocation(Location loc)
	{
		// get contents of location in question
		Element e;
		try 
		{
			e = capi.getStorage().getValue(loc);
		} catch (InvalidLocationException ex) 
		{
			// Should never happen
			throw new EngineError("Location to which list incremental update has been made is invalid!");
		}
				
		// if location contains a list, return false
		if (e instanceof ListElement)
			return false;
		// else return true
		else
			return true;	
		
	}
	
	/**
	 * Updates are only of the incremental variety, so build resultant list from the
	 * updates and put it in resultant update to be returned. Mark all updates as
	 * successfully aggregated
	 * 
	 * @param loc The location being updated.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>Update</code> and update representing the resultant update
	 *
	private Update buildResultantUpdate(Location loc, PluginAggregationAPI pluginAgg)
	{
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);

		Update result = buildResultantUpdateFromIncrementalUpdatesOnly(loc, pluginAgg);
		
		// all updates added successfully, so flag them
		for (Update u : locUpdates)
			pluginAgg.flagUpdate(u,Flag.SUCCESSFUL,this);
		
		// return resultant list
		return result;
	}

	/**
	 * Only looking at incremental updates, build resultant list from the
	 * updates and put it in resultant update to be returned. 
	 * 
	 * @param loc The location being updated.
	 * @param pluginAgg plugin aggregation API object.
	 * 
	 * @return <code>Update</code> and update representing the resultant update
	 *
	private Update buildResultantUpdateFromIncrementalUpdatesOnly(Location loc, PluginAggregationAPI pluginAgg)
	{
		/*
		 * The idea here is that a removing and adding the same element from and 
		 * to a list, should remove all previous instances of that element, and
		 * add the new ones.
		 *
		
		// updates for this location
		UpdateMultiset locUpdates = pluginAgg.getLocUpdates(loc);
		
		// get list element at curent location
		ListElement existingList;
		try 
		{
			existingList = (ListElement)capi.getStorage().getValue(loc);
		} catch (InvalidLocationException ex) 
		{
			// Should never happen
			throw new EngineError("Location to which list incremental update has been made is invalid!");
		}
		
		// resultant list element 
		ListElement resultantList = new ListElement();
		
		// add all existing elements less those removed with listRemoveAction
		for (Element e : existingList.enumerate()) {
			Update update = new Update(loc, e, LIST_REMOVE_ACTION);
			if (!locUpdates.contains(update)) {
				resultantList.add(e);
			}
		}
		
		// add all values resulting from listAddAction
		for (Update u : locUpdates)
			if (u.action.equals(LIST_ADD_ACTION))
				resultantList.add(u.value);
		
		// return resultant list
		return new Update(loc,resultantList,Update.UPDATE_ACTION);
	}


	/*
	 * When a location has a total update in the first step and
	 * has incremental updates in the second step.
	 *
	private Update aggregateLocationForComposition(Location l, PluginCompositionAPI compAPI) {
		ListElement newList = new ListElement();
		Element value = null;
		UpdateMultiset uMset1 = compAPI.getLocUpdates(1, l);
		UpdateMultiset uMset2 = compAPI.getLocUpdates(2, l);
		
		// get the value of the basic update on location 'l'
		// TODO what if there are more than two such updates?
		for (Update ui: uMset1)
			if (ui.action.equals(Update.UPDATE_ACTION)) {
				value = ui.value;
				break;
			}

		// value should be a list
		if (value instanceof ListElement) {
			ListElement list = (ListElement)value;
			
			for (Element e: list.enumerate()) {
				Update removeUpdate = new Update(l, e, LIST_REMOVE_ACTION);
				
				if (!uMset2.contains(removeUpdate)) 
					newList.add(e);
			}
			
			for (Update u: uMset2) {
				if (u.action.equals(LIST_ADD_ACTION)) 
					newList.add(u.value);
			}
			
			return new Update(l, newList, Update.UPDATE_ACTION);
		} else
			Logger.log(Logger.ERROR, Logger.storage, "Value is not a list (in ListPlugin Composition).");
		
		return null;
	}
	
	/*
	 * If we only have incremental updates in both steps 
	 *
	private UpdateMultiset eradicateConflictingIncrementalUpdates(Location l, PluginCompositionAPI compAPI) {
		UpdateMultiset remainingUpdates = new UpdateMultiset();
		UpdateMultiset uMset1 = compAPI.getLocUpdates(1, l);
		UpdateMultiset uMset2 = compAPI.getLocUpdates(2, l);
		
		for (Update u: uMset1) {
			Update updateRemove = new Update(u.loc, u.value, LIST_REMOVE_ACTION);
			
			if (u.action.equals(LIST_ADD_ACTION)) {
				if (!uMset2.contains(updateRemove))
					remainingUpdates.add(u);
			} else
				remainingUpdates.add(u);
		}
		
		for (Update u: uMset2) {
			remainingUpdates.add(u);
		}

		return remainingUpdates;
	}
	*/
}
