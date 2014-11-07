/*	
 * MapPlugin.java 	$Revision: 243 $
 * 
 * Copyright (C) 2007-2009 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.map.CompilerMapPlugin;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;

/** 
 * This is the Map Plug-in. It provides map structures and 
 * operations defined on maps.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class MapPlugin extends Plugin implements ParserPlugin, InterpreterPlugin, VocabularyExtender {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 4, 0, "beta");
	
	public static final String PLUGIN_NAME = MapPlugin.class.getSimpleName();
	
	/* keeps track of to-be-considered values in a map comprehension */
	private ThreadLocal<Map<ASTNode, Set<Map<String,Element>>>> tobeConsidered;
	
	/* keeps new lists created on a map comprehension node */
	private ThreadLocal<Map<ASTNode, Map<Element,Element>>> newMap;
	
	private Map<String, GrammarRule> parsers = null;
	private final String[] keywords = {};
	private final String[] operators = {"{", "}", "->", ","};
	Parser.Reference<Node> refMapTermParser = Parser.newReference();
	private Set<String> dependencies = null;
	private Map<String, BackgroundElement> bkgs = null;

	private HashMap<String, FunctionElement> functions;
	
	private CompilerPlugin compilerPlugin = new CompilerMapPlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
		tobeConsidered= new ThreadLocal<Map<ASTNode,Set<Map<String,Element>>>>() {
			@Override
			protected Map<ASTNode, Set<Map<String, Element>>> initialValue() {
				return new IdentityHashMap<ASTNode, Set<Map<String,Element>>>();
			}
		};
		newMap= new ThreadLocal<Map<ASTNode,Map<Element,Element>>>() {
			@Override
			protected Map<ASTNode, Map<Element,Element>> initialValue() {
				return new IdentityHashMap<ASTNode, Map<Element,Element>>();
			}
		};
	}
	
	/*
	 * Returns the instance of 'tobeConsidered' map for this thread.
	 */
	private Map<ASTNode, Set<Map<String, Element>>> getToBeConsideredMap() {
		return tobeConsidered.get();
	}
	
	/*
	 * Returns the instance of 'newMap' map for this thread.
	 */
	private Map<ASTNode, Map<Element,Element>> getNewMapMap() {
		return newMap.get();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getKeywords()
	 */
	@Override
	public String[] getKeywords() {
		return keywords;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getOperators()
	 */
	@Override
	public String[] getOperators() {
		return operators;
	}

	
	@Override
	public Set<String> getDependencyNames() {
		if (dependencies == null) {
			dependencies = new HashSet<String>();
			dependencies.add("CollectionPlugin");
			dependencies.add("ListPlugin");
			dependencies.add("SetPlugin");
		}
		return dependencies;
	}

	@Override
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
	 */
	@Override
	public Parser<Node> getParser(String nonterminal) {
		if (nonterminal.equals("MapTerm"))
			return refMapTermParser.lazy();
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParsers()
	 */
	@Override
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> termParser = kernel.getTermParser();
			Parser<Node> basicTermParser = kernel.getBasicTermParser();
			Parser<Node> guardParser = kernel.getGuardParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();

			Parser<Node> mapletParser = Parsers.array(
					new Parser[] {
						basicTermParser,
						pTools.getOprParser("->"),
						basicTermParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node map(Object[] vals) {
							ASTNode node = new MapletNode((Node)vals[0]);
							addChildren(node, vals);
							return node;
						}} 
			);
			parsers.put("Maplet", new GrammarRule("Maplet",
					"Term '->' Term", mapletParser, PLUGIN_NAME));
			
			Parser<Node> maptermParser = Parsers.array(
					new Parser[] {
						pTools.getOprParser("{"),
						Parsers.or(
								Parsers.array(pTools.getOprParser("->")),
								pTools.csplus(mapletParser)),
						pTools.getOprParser("}")
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node map(Object[] vals) {
							Node node = new MapTermNode((Node)vals[0]);
							addChildren(node, vals);
							return node;
						}
					}
			);
			
			// MapComprehension: '{' Maplet '|' ID 'in' Term 
			//                    ( ',' ID 'in' Term )* ( 'with' Guard )? ']'
			Parser<Node> mapComprehensionParser = Parsers.array(new Parser[] {
				pTools.getOprParser("{"),
				mapletParser,
				pTools.getOprParser("|"),
				pTools.csplus(Parsers.array(
					idParser,
					pTools.getKeywParser("in", PLUGIN_NAME),
					termParser)),
				Parsers.array(
					pTools.getKeywParser("with", PLUGIN_NAME),
					guardParser).optional(),
				pTools.getOprParser("}")
			}).map(new MapComprehensionParseMap());
			parsers.put("MapComprehension", 
					new GrammarRule("MapComprehension",
							"'{' Maplet '|' ID 'in' Term ( ',' ID 'in' Term )* ( 'with' Guard )? '}'", 
							mapComprehensionParser, PLUGIN_NAME));
						
			refMapTermParser.set(Parsers.or(maptermParser, mapComprehensionParser));
			parsers.put("MapTerm", new GrammarRule("MapTerm",
					"'{' '->' | ( Maplet (',' Maplet)* ) '}'", refMapTermParser.lazy(), PLUGIN_NAME));
			
			parsers.put("BasicTerm", new GrammarRule("MapBasicTerm", 
					"MapTerm | MapComprehension", refMapTermParser.lazy(), PLUGIN_NAME));
		} 
		
		return parsers;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	@Override
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	@Override
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		Map<ASTNode, Set<Map<String, Element>>> tobeConsidered = getToBeConsideredMap();
		Map<ASTNode, Map<Element,Element>> newMap = getNewMapMap();
		
		if (pos instanceof MapletNode) {
			if (!pos.getFirst().isEvaluated())
				return pos.getFirst();
			if (!pos.getFirst().getNext().isEvaluated())
				return pos.getFirst().getNext();
			pos.setNode(null, null, new MapletElement(pos.getFirst().getValue(), pos.getFirst().getNext().getValue()));
		} 
		else if (pos instanceof MapTermNode) {
			if (pos.getFirst() == null) {
				// it's an empty map
				pos.setNode(null, null, new MapElement());
			} else {
				// evaluate all child nodes (all the maplets)
				for (ASTNode maplet : pos.getAbstractChildNodes())
					if (!maplet.isEvaluated())
						return maplet;

				Map<Element, Element> map = new HashMap<Element, Element>();
				for (ASTNode maplet : pos.getAbstractChildNodes())
					map.put(maplet.getFirst().getValue(), maplet.getFirst().getNext().getValue());
				pos.setNode(null, null, new MapElement(map));
			}
		}
		else if (pos instanceof MapCompNode) {
			MapCompNode node = (MapCompNode)pos;
			Map<String,ASTNode> bindings = null;
			
			// get variable to domain bindings
			try {
				bindings = node.getVarBindings();
			} catch (EngineException e) {
				// "No two constrainer variables may have the same name."
				capi.error(e);
			}
			ASTNode guard = node.getGuard();
			ASTNode expression = node.getMapFunction();

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
								pos.setNode(null, null, new MapElement());
								return pos;
							}
					}
					
					// create the resulting map
					newMap.put(pos, new HashMap<Element,Element>());
					
					// Set of all possible bindings
					HashSet<Map<String,Element>> possibleBindings = new HashSet<Map<String,Element>>();
					
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
						pos.setNode(null, null, new MapElement(newMap.get(pos)));
					}
				}
			} 
			
			// if everything is evaluated
			else {
				// remove previous bindings
				unbindVariables(interpreter, bindings.keySet());

				Map<Element,Element> result = newMap.get(pos);
				MapletElement maplet = (MapletElement)expression.getValue();
				result.put(maplet.key, maplet.value);
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
					pos.setNode(null, null, new MapElement(newMap.get(pos)));
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
	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	}

	@Override
	public Map<String, BackgroundElement> getBackgrounds() {
		if (bkgs == null) {
			bkgs = new HashMap<String, BackgroundElement>();
			bkgs.put(MapBackgroundElement.NAME, new MapBackgroundElement());
		}
		return bkgs;
	}

	@Override
	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	@Override
	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();
			functions.put(ToMapFunctionElement.NAME, new ToMapFunctionElement());
			functions.put(MapToPairsFunctionElement.NAME, new MapToPairsFunctionElement());
		}
		return functions;
	}

	@Override
	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, RuleElement> getRules() {
		return Collections.emptyMap();
	}

	@Override
	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, UniverseElement> getUniverses() {
		return Collections.emptyMap();
	}

	public static class MapComprehensionParseMap extends ParserTools.ArrayParseMap {

		public MapComprehensionParseMap() {
			super(PLUGIN_NAME);
		}
		
		public Node map(Object[] vals) {
			Node node = new MapCompNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
	}
}
