/*
 * ModularityPlugin.java 		$Revision: 91 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-07-31 17:41:23 +0200 (Fr, 31 Jul 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.engine.plugins.modularity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Token;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.modularity.CompilerModularityPlugin;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.SpecLine;
import org.coreasm.engine.Specification;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides some basic modularity features to CoreASM.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class ModularityPlugin extends Plugin implements ParserPlugin,
		ExtensionPointPlugin {

	protected static final Logger logger = LoggerFactory.getLogger(ModularityPlugin.class);

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 1, 0, "alpha");
	
	public static final String PLUGIN_NAME = ModularityPlugin.class.getSimpleName();

	private final String[] keywords = {"CoreModule", "include"};
	private final String[] operators = {};
	
    private Map<String, GrammarRule> parsers = null;

	private Map<EngineMode, Integer> targetModes;
	
	private Set<String> loadedModules = null;
	
	private final CompilerPlugin compilerPlugin = new CompilerModularityPlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() throws InitializationFailedException {

	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ExtensionPointPlugin#fireOnModeTransition(org.coreasm.engine.CoreASMEngine.EngineMode, org.coreasm.engine.CoreASMEngine.EngineMode)
	 */
	public void fireOnModeTransition(EngineMode source, EngineMode target) {
    	if (target == EngineMode.emParsingSpec) {
    		loadedModules = new HashSet<String>();
    		final ArrayList<SpecLine> newSpec = injectModules(capi.getSpec().getLines(), capi.getSpec().getFileDir());
    		capi.getSpec().updateLines(newSpec);
    		/*
    		System.out.println("** ModularityPlugin :  Specification is modified as follows:");
    		System.out.println("** ModularityPlugin :  -------------------------------------");
    		int i = 1;
    		for (SpecLine line: newSpec) {
        		System.out.println("** ModularityPlugin : " + Tools.lFormat(i, 3) + "  " + line.text);
        		i++;
    		}
    		System.out.println("** ModularityPlugin :  -------------------------------------");
    		/**/
    	}
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ExtensionPointPlugin#getSourceModes()
	 */
	public Map<EngineMode, Integer> getSourceModes() {
		return Collections.emptyMap();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ExtensionPointPlugin#getTargetModes()
	 */
	public Map<EngineMode, Integer> getTargetModes() {
    	if (targetModes == null) {
    		targetModes = new HashMap<EngineMode, Integer>();
    		targetModes.put(EngineMode.emParsingSpec, 90);
    	}
    	return targetModes;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	public String[] getOperators() {
		return operators;
	}

	/**
	 * This plugin requires "String" because of the new "include" parser.
	 * 
	 * @author Markus
	 */
	public Set<String> getDependencyNames() {
		Set<String> names = new HashSet<String>(super.getDependencyNames());
		names.add("StringPlugin");
		return names;
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
			
			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			Parser<Node> stringParser = ((ParserPlugin)capi.getPlugin("StringPlugin")).getParser("StringTerm");
			
			// rule for regognizing include statements
			// (for usage of the parser out of the engine)
			// 'include' StringTerm
			//
			// IMPORTANT: This parser currently restricts the usage of the include statement
			// as header statements. The parser will still produce an error for an include
			// which is found inside a rule definition.
			// Additionally the filename is required to be quoted as a StringTerm.
			// A specification can still be run if these requirements aren't met,
			// because the include's are replaced before parsing.
			Parser<Node> includeParser = Parsers.array( new Parser [] {
					pTools.getKeywParser("include", PLUGIN_NAME).token(),
					stringParser
			}).map( new ParserTools.ArrayParseMap(PLUGIN_NAME) {
				@Override
				public Node map(Object[] from) {
					int index = -1;
					if (from[0]!=null && from[0] instanceof Token)
						index = ((Token)from[0]).index();

					IncludeNode iNode = new IncludeNode(new ScannerInfo(index));
					Node kwNode = new Node(
							PLUGIN_NAME,
							"include",
							new ScannerInfo(index),
							Node.KEYWORD_NODE
						);
					iNode.addChild(kwNode);
					if (from[1]!=null && from[1] instanceof Node)
						iNode.addChild("alpha", (Node)from[1]);
					return iNode;
				}
			});
			

	    	// CoreModule : 'CoreModule' ID ( UseClause )* ( Header )* 'init' ID
	    	Parser<Node> coreModuleParser = Parsers.array(
	    			new Parser[] {
	    			pTools.getKeywParser("CoreModule", PLUGIN_NAME),
	    			idParser,
	    			}).map(
	    			new CoreModuleParseMap()
	    			);
	    	
	    	Parser<Node> modularityHeaderParser = Parsers.or(includeParser, coreModuleParser);
	    	
	    	parsers.put("Header", 
	    			new GrammarRule("CoreModule", 
	    					"'CoreModule' ID", modularityHeaderParser, this.getName()));
			
		}
		return parsers;
	}

	private ArrayList<SpecLine> injectModules(List<SpecLine> lines, String relativePath) {
		// CHANGE: Includes inside included specifications will be looked up relative to the including specification now
		ArrayList<SpecLine> newSpec = new ArrayList<SpecLine>();
		String useRegex;
		Pattern usePattern;
		Matcher useMatcher;
		// compile pattern to find "include " directives using regular expression
		useRegex = "^[\\s]*[i][n][c][l][u][d][e][\\s]+"; 
		usePattern = Pattern.compile(useRegex);

		for (SpecLine line: lines) {
			// get an "include" directive matcher object for the line
			useMatcher = usePattern.matcher(line.text);

			// if match found
			if (useMatcher.find()) {
				// are there inner include files?
				
				// get the include file name and load the file
				String fileName = useMatcher.replaceFirst("").trim();
				// remove potential quotation marks
				while (fileName.startsWith("\"") && fileName.endsWith("\"")) 
					fileName = fileName.substring(1, fileName.length()-1);
				// CHANGE: make sure that filenames have the same format to ensure that equal paths match, not only equal include statements
				try {
					fileName = (new File(relativePath + File.separator + fileName)).getCanonicalPath();
				} catch (IOException e) {
				}
				if (loadedModules.contains(fileName))
					logger.info(
							"ModularityPlugin: Skipping module '" + fileName + "' since it's already loaded.");
				else 
					try {
						loadedModules.add(fileName);
						logger.info( 
								"ModularityPlugin: Loading module '" + fileName + "'.");
						List<SpecLine> newLines = injectModules(Specification.loadSpec(fileName), fileName.substring(0, fileName.lastIndexOf(File.separator)));
						for (SpecLine newLine: newLines)
							newSpec.add(newLine);
					} catch (IOException e) {
						capi.error("Modularity plugin cannot load module file '" 
								+ fileName + "'. The error is:" + Tools.getEOL()
								+ e.getMessage() + Tools.getEOL()
								+ "Check " + line.fileName + ":" + line.line + ".");
					}
			} else
				newSpec.add(line);
		}
		return newSpec;
	}
}
