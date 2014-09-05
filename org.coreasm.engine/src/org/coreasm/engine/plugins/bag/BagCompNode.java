/*	
 * BagCompNode.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.bag;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Bag comprehension node.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class BagCompNode extends ASTNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private ASTNode dummyGuard = null;
	
	public BagCompNode(ScannerInfo info) {
		super(
				BagPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"BagComprehension",
				null,
				info);
	}

	public BagCompNode(BagCompNode node) {
		super(node);
	}

	/**
	 * @return the first occurrence of the specifier variable
	 */
	public String getSpecifierVar() {
		return this.getFirst().getToken();
	}
	
	/**
	 * @return the constrainer variable
	 */
	public String getConstrainerVar() {
		return this.getFirst().getNext().getToken();
	}
	
	/**
	 * @return the node referring to the domain of the set comprehension
	 */
	public ASTNode getDomain() {
		return this.getFirst().getNext().getNext();
	}
	
	/**
	 * @return the guard node
	 */
	public ASTNode getGuard() {
		ASTNode guard = this.getDomain().getNext();
		if (guard != null)
			return guard;
		else {
			if (dummyGuard == null)
				dummyGuard = new TrueGuardNode(this);
	    	return dummyGuard;
		}
	}
}

