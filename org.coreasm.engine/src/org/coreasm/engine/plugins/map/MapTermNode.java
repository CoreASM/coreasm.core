/*	
 * MapTermNode.java 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.map;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;

/** 
 * Node for Map terms.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class MapTermNode extends ASTNode {
	private static final long serialVersionUID = 2295836328143514054L;

	public MapTermNode(Node firstNode) {
		super(MapPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"MapTerm",
				null,
				firstNode.getScannerInfo());
	}
	
	public MapTermNode(MapTermNode node) {
		super(node);
	}
}
