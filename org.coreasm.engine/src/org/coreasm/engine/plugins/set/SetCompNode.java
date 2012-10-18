/*	
 * SetCompNode.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006-2007  Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.set;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Set comprehension node.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class SetCompNode extends ASTNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private ASTNode dummyGuard = null;
	
	public SetCompNode(ScannerInfo info) {
		super(
				SetPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"SetComprehension",
				null,
				info);
	}

	public SetCompNode(SetCompNode node) {
		super(node);
	}

	/**
	 * @return the first occurrence of the specifier variable
	 */
	public String getSpecifierVar() {
		// as the variable node is a TERM, we need to go two step down
		return this.getFirst().getFirst().getToken();
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
