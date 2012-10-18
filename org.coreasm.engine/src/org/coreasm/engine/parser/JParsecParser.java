/*	
 * JParsecParser.java 	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.parser;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jparsec.Parsers;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.SpecLine;
import org.coreasm.engine.Specification;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.util.Logger;
import org.coreasm.util.Tools;

/** 
 * This is an implementation of the {@link Parser} component 
 * using the JParsec libraries.
 *   
 * @author Roozbeh Farahbod, Mashaal Memon
 * 
 */
public class JParsecParser implements Parser {

	/** Control API of engine which this parser belongs to */
	private ControlAPI capi;
		
	/** names of all plugins used by specification*/
	private HashSet<String> pluginNames;

	/* CoreASM specification */
	private Specification specification = null;
	
	private PositionMap positionMap = null;
	
	private boolean headerParsed = false;
	
	/** the actual parser -- a JParsec parser */ 
	private org.codehaus.jparsec.Parser<Node> parser;
	
	/** the root grammar rule */
	private GrammarRule rootGrammarRule;
	
	/** the root node of the specification (after parsing) */
	private ASTNode rootNode = null;
	
	//private final ParserTools parserTools;
	private final ParserTools parserTools;
	
	
	/**
	 * Implementation of the parser interface using the JParsec library.
	 * The
	 * control API for the engine which this parser belongs to is passed in.
	 *
	 * @param capi control api for engine which this parser belongs to.
	 */
	public JParsecParser(ControlAPI capi) {
		super();
		this.capi = capi;
		parserTools = ParserTools.getInstance(capi);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.parser.Parser#getRequiredPlugins()
	 */
	public Set<String> getRequiredPlugins() {
		return pluginNames;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.parser.Parser#getRootNode()
	 */
	public ASTNode getRootNode() {
		return rootNode;
	}

	/**
	 * Plugins to be used for the specification are specified with "use"
	 * directive on their own lines. Fine these lines and take note of
	 * plugin names found beside the "use" directives.
	 * 
	 * @see org.coreasm.engine.parser.Parser#parseHeader()
	 */
	public void parseHeader() throws ParserException
	{
            
		String useRegex;
		Pattern usePattern;
		Matcher useMatcher;
                
		// instantiate new plugin names set.
		pluginNames = new HashSet<String>();
		
		try
		{
			// compile pattern to find "use" directive using regular expression
			useRegex = "^[\\s]*[uU][sS][eE][\\s]+"; // regex to fine "use" directive followed by whitespace at beginning of line
			// compile and get a reference to a Pattern object.
			usePattern = Pattern.compile(useRegex);
			
			// error if specification is not set
			if (specification==null)
			{
				Logger.parser.log(Logger.FATAL, "Specification file must first be set before its header can be parsed.");
				throw new ParserException("Specification file must first be set before its header can be parsed.");
			}
		
			// for each line of specification file
			for (SpecLine line: specification.getLines()) {

				// get a "use" directive matcher object for the line
				useMatcher = usePattern.matcher(line.text);

				// if match found
				if (useMatcher.find())
				{
					// get plugin name and add to the list
					String pluginName = useMatcher.replaceFirst("").trim();
					pluginNames.add(pluginName);
				}
			}
			
			headerParsed = true;
			
		}
		catch (NullPointerException e)
		{
			Logger.parser.log(Logger.ERROR,"CoreASM specification cannot be read from.");	
		} 
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.parser.Parser#parseSpecification()
	 */
	public void parseSpecification() throws ParserException {
		if (headerParsed) {
			Plugin kernel = capi.getPlugin("Kernel");
			if (kernel != null) {
				this.rootGrammarRule = ((ParserPlugin)kernel).getParsers().get("CoreASM");
				this.parser = rootGrammarRule.parser;
				try {
					org.codehaus.jparsec.Parser<Node> _parser =  parser.from(parserTools.getTokenizer(), parserTools.getIgnored());
					rootNode = (ASTNode) _parser.parse(specification.getText());
				} catch (Throwable e) {
					if (e instanceof org.codehaus.jparsec.error.ParserException) {
						org.codehaus.jparsec.error.ParserException pe = (org.codehaus.jparsec.error.ParserException) e;
						Throwable cause = pe.getCause();
						String msg = pe.getMessage();
						msg = msg.substring(msg.indexOf("\n")+1);
						msg = "Error parsing " + msg + (cause==null?"":"\n" + cause.getMessage());
						
						String errorLogMsg = "Error in parsing.";
						if (cause != null) {
							StringWriter strWriter = new StringWriter();
							cause.printStackTrace(new PrintWriter(strWriter));
							errorLogMsg = errorLogMsg + Tools.getEOL() + strWriter.toString();
						}
						Logger.parser.log(Logger.ERROR, errorLogMsg);
						
						throw new ParserException(msg, 
								new CharacterPosition(pe.getLocation().line, pe.getLocation().column));
					}
					throw new ParserException(e);
				}
			} else {
				Logger.parser.log(Logger.FATAL, "Parser cannot find the Kernel plugin.");
				throw new EngineError("Parser cannot find the Kernel plugin.");
			}
		} else {
			Logger.parser.log(Logger.FATAL, "Header must be parsed before the entire specification can be parsed.");
			throw new ParserException("Header must be parsed before the entire specification can be parsed.");
		}
	}

	public void setSpecification(Specification spec) {
		positionMap = null;
		pluginNames = null;
		parser = null;
		headerParsed = false;
		this.specification = spec;
	}

	/*
	 * @see org.coreasm.engine.parser.Parser#getPositionMap()
	 */
	public PositionMap getPositionMap() {
		if (positionMap == null) {
			positionMap = new PositionMap(specification.getText(), 1, 1);
		}
		return positionMap;
	}

	public ParserTools getParserTools() {
		return parserTools;
	}

}
