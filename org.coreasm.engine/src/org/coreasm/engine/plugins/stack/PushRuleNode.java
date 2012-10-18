/*	
 * PushRuleNode.java 	$Revision: 243 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.stack;


import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	AST node for push rules.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class PushRuleNode extends ASTNode {
	
	private static final long serialVersionUID = 1L;

	public PushRuleNode(ScannerInfo info) {
		super(
				StackPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"StackPushRule",
				null,
				info);
	}
	
	/**
	 * @param node
	 */
	public PushRuleNode(PushRuleNode node) {
		super(node);
	}
	
	/**
	 * @return the node representing the element being pushed into stack
	 */
	public ASTNode getElementNode() {
		return getFirst();
	}
	
	/**
	 * @return the node representing the stack
	 */
	public ASTNode getStackNode() {
		return getFirst().getNext();
	}

}
