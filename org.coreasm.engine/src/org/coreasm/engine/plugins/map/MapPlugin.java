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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
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
import org.coreasm.engine.parser.ParseMapN;
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
	
	private Map<String, GrammarRule> parsers = null;
	private final String[] keywords = {};
	private final String[] operators = {"{", "}", "->", ","};
	Parser.Reference<Node> refMapTermParser = Parser.newReference();
	private Set<String> dependencies = null;
	private Map<String, BackgroundElement> bkgs = null;

	private HashMap<String, FunctionElement> functions;
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getKeywords()
	 */
	public String[] getKeywords() {
		return keywords;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getOperators()
	 */
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

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
	 */
	public Parser<Node> getParser(String nonterminal) {
		if (nonterminal.equals("MapTerm"))
			return refMapTermParser.lazy();
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParsers()
	 */
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> termParser = kernel.getTermParser();
			Parser<Node> basicTermParser = kernel.getBasicTermParser();
			Parser<Node> guardParser = kernel.getGuardParser();

			ParserTools pTools = ParserTools.getInstance(capi);

			Parser<Node> mapletParser = Parsers.array(
					new Parser[] {
						basicTermParser,
						pTools.getOprParser("->"),
						basicTermParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object... vals) {
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

						public Node map(Object... vals) {
							Node node = new MapTermNode((Node)vals[0]);
							addChildren(node, vals);
							return node;
						}
					}
			);
			refMapTermParser.set(maptermParser);
			parsers.put("MapTerm", new GrammarRule("MapTerm",
					"'{' '->' | ( Maplet (',' Maplet)* ) '}'", refMapTermParser.lazy(), PLUGIN_NAME));
			
			parsers.put("BasicTerm", new GrammarRule("MapBasicTerm", 
					"MapTerm", refMapTermParser.lazy(), PLUGIN_NAME));
		} 
		
		return parsers;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
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
		return pos;
	}

	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	}

	public Map<String, BackgroundElement> getBackgrounds() {
		if (bkgs == null) {
			bkgs = new HashMap<String, BackgroundElement>();
			bkgs.put(MapBackgroundElement.NAME, new MapBackgroundElement());
		}
		return bkgs;
	}

	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();
			functions.put(ToMapFunctionElement.NAME, new ToMapFunctionElement());
			functions.put(MapToPairsFunctionElement.NAME, new MapToPairsFunctionElement());
		}
		return functions;
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
