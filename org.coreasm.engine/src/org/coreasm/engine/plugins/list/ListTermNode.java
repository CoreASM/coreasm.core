/*	
 * ListTermNode.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.list;

import java.util.List;

import org.coreasm.engine.interpreter.ASTNode;

/** 
 * Observer for list terms.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ListTermNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public ListTermNode() {
		super(
				ListPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"ListTerm",
				null,
				null);
	}
	
	public ListTermNode(ListTermNode node) {
		super(node);
	}

	/**
	 * @return the list of elements (nodes) in this list
	 */
	public List<ASTNode> getElements() {
		return this.getAbstractChildNodes();
	}
}
