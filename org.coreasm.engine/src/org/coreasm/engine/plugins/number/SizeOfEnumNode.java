/*	
 * SizeOfEnumNode.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006-2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.number;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

//TODO this can go to EnumerablePlugin
/** 
 * Node of size-of function pattern.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class SizeOfEnumNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public SizeOfEnumNode(ScannerInfo info) {
		super(
				NumberPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"SizeOfEnumTerm",
				null,
				info);
	}

	public SizeOfEnumNode(SizeOfEnumNode node) {
		super(node);
	}
	
	/**
	 * @return the node containing the enumberable element
	 */
	public ASTNode getEnumerableNode() {
		return this.getFirst();
	}
	
}
