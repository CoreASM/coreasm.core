/*	
 * ParserFragments.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-04-29 03:11:19 +0200 (Do, 29 Apr 2010) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.parser;

import java.util.Collection;
import java.util.HashMap;

/**
 * Implements an easy to use collection of parser fragments as a map of grammar rule names to 
 * instances of {@link GrammarRule}.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class ParserFragments extends HashMap<String, GrammarRule> {

	private static final long serialVersionUID = 1L;

	/**
	 * Adds a new grammar rule to this collection.
	 * 
	 * @param rule a grammar rule
	 */
	public void add(GrammarRule rule) {
		this.put(rule.name, rule);
	}
	
	/**
	 * @return the collection of grammar rules in this object
	 */
	public Collection<GrammarRule> getGrammarRules() {
		return this.values();
	}
}
