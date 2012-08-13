/*	
 * MapletNode.java 	$Revision: 243 $
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
 * Nodes of the form:
 * <p>
 * Term '->' Term
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class MapletNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public MapletNode(Node firstChild) {
		super(MapPlugin.PLUGIN_NAME, 
				ASTNode.OTHER_NODE, 
				"Maplet", 
				null, 
				firstChild.getScannerInfo());
	}

	/**
	 * @param node
	 */
	public MapletNode(MapletNode node) {
		super(node);
	}

	/**
	 * @return the first child of this node (which is the 'key' part)
	 */
	public ASTNode getKeyNode() {
		return getFirst();
	}
	
	/**
	 * @return the second child of this node (which is the 'value' part)
	 */
	public ASTNode getValueNode() {
		return getFirst().getNext();
	}
}
