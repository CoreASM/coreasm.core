/*	
 * MathPlugin.java 	1.0 	$Revision: 196 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-11-26 19:27:57 +0100 (Fr, 26 Nov 2010) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.math.CompilerMathPlugin;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParseMap;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.plugins.number.NumberElement;
import org.coreasm.engine.registry.PluginInfo;

/** 
 * This a standard Math plug-in for CoreASM.
 *   
 * @author Roozbeh Farahbod
 * @version 1.0, $Revision: 196 $, Last modified: $Date: 2010-11-26 19:27:57 +0100 (Fr, 26 Nov 2010) $
 */
public class MathPlugin extends Plugin implements VocabularyExtender, ParserPlugin, InterpreterPlugin {

	/** version information */
	public static final VersionInfo vInfo = new VersionInfo(0, 2, 3, "beta");
	
	/** plug-in name */
	public static final String PLUGIN_NAME = MathPlugin.class.getSimpleName();

	public static final String KW_RANDOM_VALUE = "randomvalue";
	
	private Map<String, FunctionElement> functions = null;
	
	private Set<String> dependencyNames = null;

	private String[] keywords = {KW_RANDOM_VALUE};
	private String[] operators = {};

	private Map<String, GrammarRule> parsers;

	private CompilerPlugin compilerPlugin = new CompilerMathPlugin();
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
	public MathPlugin() {
		super();
		dependencyNames = new HashSet<String>();
		dependencyNames.add("NumberPlugin");
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgroundNames()
	 */
	public Set<String> getBackgroundNames() {
		return Collections.emptySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgrounds()
	 */
	public Map<String, BackgroundElement> getBackgrounds() {
		return Collections.emptyMap();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctionNames()
	 */
	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctions()
	 */
	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) 
			functions = MathFunction.createFunctions(capi);
		return functions;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverseNames()
	 */
	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverses()
	 */
	public Map<String, UniverseElement> getUniverses() {
		return Collections.emptyMap();
	}

	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return vInfo;
	}

	@Override
	public Set<String> getDependencyNames() {
		return dependencyNames;
	}
	
	@Override
	public PluginInfo getInfo() {
		StringBuffer descr = new StringBuffer(
				"This plug-in provides some basic mathematical functions and constants such as: ");
		for (String fname: getFunctionNames()) {
			descr.append(fname + ", ");
		}
		PluginInfo info = new PluginInfo("Roozbeh Farahbod", 
				getVersionInfo().toString(), 
				descr.substring(0, descr.length() - 2).toString());
		return info;
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
	public Parser<Node> getParser(String arg0) {
		return null;
	}

	@Override
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			
			ParserTools pTools = ParserTools.getInstance(capi);
			
			Parser<Node> randomValueParser = 
					pTools.getKeywParser(KW_RANDOM_VALUE, PLUGIN_NAME).map(
					new ParseMap<Node, Node>(PLUGIN_NAME) {

						public Node map(Node v) {
							Node node = new ASTNode(
									PLUGIN_NAME, 
									ASTNode.EXPRESSION_CLASS, 
									"RandomValue", 
									v.getToken(), 
									v.getScannerInfo(), 
									Node.KEYWORD_NODE);
							return node;
						}
				
					});
			parsers.put("RandomValue",
					new GrammarRule("RandomValue", 
							"'randomvalue'", randomValueParser, PLUGIN_NAME));

			parsers.put("BasicTerm", 
					new GrammarRule("RandomValue", "RandomValue",
							randomValueParser, PLUGIN_NAME));
		}
		
		return parsers;
	}

	@Override
	public ASTNode interpret(Interpreter interpreter, ASTNode pos)
			throws InterpreterException {
		final String token = pos.getToken();
		if (token == null) 
			return pos;
		else if (token.equals(KW_RANDOM_VALUE)) {
			pos.setNode(null, null, NumberElement.getInstance(Math.random()));
		}
		return pos;
	}

}
