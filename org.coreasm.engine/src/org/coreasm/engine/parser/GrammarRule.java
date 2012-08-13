/*	
 * GrammarRule.java 	$Revision: 243 $
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

import org.coreasm.engine.interpreter.Node;

import org.codehaus.jparsec.Parser;

/** 
 * A structure to hold a grammar rule, and a {@link jfun.parsec.Parser} instance
 * for that grammar rule.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class GrammarRule {

	/** Grammar Rule Type: 
	 * 
	 * START = start of grammar
	 * OP_CONNECT = highest level of precdedence to connect to this rule, 
	 * NORMAL = all others 
	 */
	public enum GRType {START, OP_TOP_LEVEL, OP_BOTTOM_LEVEL, NORMAL};
	
	public final String name; // nonterminal
	public String body;
	public final Parser<Node> parser;
	public final String pluginName;
	public final GRType type;
	
	/**
	 * Creates a new grammar rule instance.
	 * 
	 * @param name the name of the nonterminal
	 * @param body body of this grammar rule
	 * @param parser parser of this grammar rule
	 * @param pluginName the plug-in that contributed this grammar rule
	 * @param type type of the grammar rule
	 */
	public GrammarRule(String name, String body, Parser<Node> parser, String pluginName, GRType type) {
		this.name = name;
		this.parser = parser;
		this.body = body;
		this.pluginName = pluginName;
		this.type = type;
	}

	/**
	 * Creates a new grammar rule instance with a normal type.
	 * 
	 * @param name the name of the nonterminal
	 * @param body body of this grammar rule
	 * @param parser parser of this grammar rule
	 * @param pluginName the plug-in that contributed this grammar rule
s	 */
	public GrammarRule(String name, String body, Parser<Node> parser, String pluginName) {
		this(name, body, parser, pluginName, GRType.NORMAL);
	}

	public String toString() {
		return name + ": " + body + " // " + pluginName;
	}
}
