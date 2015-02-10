/*	
 * ParserPlugin.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2005-2007 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugin;

import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.GrammarRule;

/** 
 *	Interface for plugins that extend the Parser module.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public interface ParserPlugin {

	/**
	 * Returns the pieces of Lexer provided by this plug-in.
	 * The returned value should not be null.
	 * 
	 * This method should only be called by the Kernel and 
	 * other plug-ins should NOT call this method.
	 *  
	 * @return a set of lexers
	 * @see Parser
	 * @see Lexers
	 */
	public Set<Parser<? extends Object>> getLexers();
	
	/**
	 * Returns the grammar rules provided by 
	 * this plugin as a map of nonterminals to grammar rules.
	 * This method should only be called by the Kernel and 
	 * other plug-ins should NOT call this method and should 
	 * use {@link #getParser(String)} if they are looking for a specific 
	 * parser provided by this plugin.
	 *  
	 * @return a map of nonterminals to grammar rules
	 */
	public Map<String, GrammarRule> getParsers();
	
	/**
	 * Provides a hook to a parser that this plug-in provides 
	 * for the given nonterminal. This is basically used by other 
	 * plug-ins in creating their own parser which is composed of 
	 * other plugin's parsers.
	 * 
	 * @param nonterminal name of the nonterminal
	 * @return a JParsec parser object
	 * 
	 * @see Parser
	 */
	public Parser<Node> getParser(String nonterminal);
	
	/**
	 * Returns the list of keywords this plugin provides.
	 * The returned value should not be null.
	 *  
	 * @return a String array of keywords.
	 */
	public String[] getKeywords();

	/**
	 * Returns the list of operators this plugin provides.
	 * The returned value should not be null.
	 * 
	 * @return a String array of operators.
	 */
	public String[] getOperators();
}
