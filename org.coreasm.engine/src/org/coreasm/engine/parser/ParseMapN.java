/*	
 * ParseMapN.java 	$Revision: 243 $
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

/** 
 * Specialized version of {@link Mapn} that gets a plug-in name as well. 
 *   
 * @author Roozbeh Farahbod
 * 
 */

@SuppressWarnings("serial")
public abstract class ParseMapN<R> {

	public final String pluginName;
	
	public ParseMapN(String pluginName) {
		this.pluginName = pluginName;
	}

	public ParseMapN(Plugin plugin) {
		this.pluginName = plugin.getName();
	}
	
	/**
	 * Assumes all the children are instances of {@link Node} and
	 * adds all of them as children of parent.
	 *  
	 * @param parent parent node
	 * @param children array of child nodes
	 */
	public void addChildren(Node parent, Object[] children) {
		
		for (Object child: children) {
			if (child != null) {
				if (child instanceof Object[])
					addChildren(parent, (Object[])child);
				else
				// otherwise child should be a Node!
					addChild(parent, (Node)child);
			}
		}
	}
	
	/**
	 * Simply adds <code>child</code> to the children of
	 * <code>parent</code>. This method is used by 
	 * {@link #addChildren(Node, Object[])} and can 
	 * be overriden to customize the tree construction.
	 * 
	 *  @param parent
	 *  @param child 
	 */
	public void addChild(Node parent, Node child) {
		parent.addChild(child);
	}
	
}

