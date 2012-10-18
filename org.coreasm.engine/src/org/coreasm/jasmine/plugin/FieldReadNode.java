/*
 * FieldReadNode.java 		$Revision: 9 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.jasmine.plugin;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/**
 * The node representing a JASMine field read of the 
 * form:
 * <p>
 * <code><i>v</i> -> <i>x</i></code>
 *   
 * @author Roozbeh Farahbod
 *
 */

public class FieldReadNode extends ASTNode {

	/**
	 * @see ASTNode#ASTNode(ASTNode)
	 */
	public FieldReadNode(FieldReadNode node) {
		super(node);
	}

	/**
	 * Creates a new node with the given scanner information
	 */
	public FieldReadNode(ScannerInfo info) {
		super(JasminePlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"JasmineFieldReadExp",
				null,
				info
				);
	}

	/**
	 * Returns the term component.
	 */
	public ASTNode getTerm() {
		return getFirst();
	}

	/**
	 * Returns the field component.
	 */
	public String getField() {
		return getFirst().getNext().getToken();
	}
	

}
