/*	
 * RemoveChildFromRuleNode.java
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
 *	AST node rule elements: 'remove child NODE from NODE'.
 *   
 *  @author  Franco Alberto Cardillo (facardillo@gmail.com)
 */
public class RemoveChildFromRuleNode extends ASTNode  {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RemoveChildFromRuleNode() {
		super(
				TreePlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"RemoveChildFromRule",
				null,
				null);
	}

	/**
	 * @param node
	 */
	public RemoveChildFromRuleNode(RemoveChildFromRuleNode node) {
		super(node);
	}

	
	/**
	 * @return returns the second parameter of this rule
	 */
	public ASTNode getSecond() {
		return getFirst().getNext();
	} // getParent
} // RemoveChildFromRuleNode.java
