/*	
 * RemoveChildFromAtNode.java
 * 
 * Copyright (C) 2010 Dipartimento di Informatica, Universita` di Pisa, Italy.
 *
 * Author: Franco Alberto Cardillo 		(facardillo@gmail.com)
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.engine.plugins.tree;

import org.coreasm.engine.interpreter.ASTNode;


/** 
 *	AST node rule elements: 'remove child at IDX from NODE'.
 *   
 *  @author  Franco Alberto Cardillo
 */
public class RemoveChildAtRuleNode extends ASTNode  {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RemoveChildAtRuleNode() {
		super(
				TreePlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"RemoveChildAtRule",
				null,
				null);
	}

	/**
	 * @param node
	 */
	public RemoveChildAtRuleNode(RemoveChildAtRuleNode node) {
		super(node);
	}

	
	/**
	 * @return the second parameter 
	 */
	public ASTNode getSecond() {
		return getFirst().getNext();
	} // getParent
} // RemoveChildAtRuleNode.java
