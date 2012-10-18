/*	
 * ReturnResultNode.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.turboasm;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Wrapper for ReturnResult nodes.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ReturnResultNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	/**
	 */
	public ReturnResultNode(ScannerInfo info) {
		super(
				TurboASMPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"ReturnResultRule",
				null,
				info);
	}

	public ReturnResultNode(ReturnResultNode node) {
		super(node);
	}
	
	/**
	 * Returns the location part of this rule
	 * 
	 * @return a node
	 */
	public ASTNode getLocationNode() {
		return this.getFirst();
	}
	
	/** 
	 * Returns the rule part of this rule
	 * 
	 * @return a node
	 */
	public ASTNode getRuleNode() {
		return this.getFirst().getNext();
	}

}
