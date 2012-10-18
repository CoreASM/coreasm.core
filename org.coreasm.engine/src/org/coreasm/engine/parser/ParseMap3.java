/*	
 * ParseMap3.java 	$Revision: 243 $
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
import org.coreasm.engine.plugin.Plugin;

import org.codehaus.jparsec.functors.Map3;

/** 
 * Specialized version of {@link Map3} that gets a plug-in name as well. 
 *   
 * @author Roozbeh Farahbod
 * 
 */

public abstract class ParseMap3 implements Map3<Node, Node, Node, Node> {

	public final String pluginName;
	
	public ParseMap3(String pluginName) {
		this.pluginName = pluginName;
	}

	public ParseMap3(Plugin plugin) {
		this.pluginName = plugin.getName();
	}
}

