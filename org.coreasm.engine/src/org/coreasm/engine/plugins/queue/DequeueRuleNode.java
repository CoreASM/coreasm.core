/*	
 * DequeueRuleNode.java 	$Revision: 243 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.queue;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	AST node for 'dequeue' rules.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class DequeueRuleNode extends ASTNode {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DequeueRuleNode(ScannerInfo info) {
		super(
				QueuePlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"DequeueRule",
				null,
				info);
	}
	
	/**
	 * @param node
	 */
	public DequeueRuleNode(DequeueRuleNode node) {
		super(node);
	}

	/**
	 * @return the node representing the location where the popped value should be stored
	 */
	public ASTNode getLocationNode() {
		return getFirst();
	}
	
	/**
	 * @return the node representing the stack
	 */
	public ASTNode getQueueNode() {
		return getFirst().getNext();
	}
}
