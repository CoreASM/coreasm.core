/*	
 * AddChildToRuleNode.java
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
 *	AST node for rules: add child NODE to NODE (at POSITION)?
 *   
 *  @author  Franco Alberto Cardillo
 */
public class AddChildToRuleNode extends ASTNode  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddChildToRuleNode() {
		super(
				TreePlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"AddChildToRule",
				null,
				null);
	}
	
	/**
	 * @param node
	 */
	public AddChildToRuleNode(AddChildToRuleNode node) {
		super(node);
	}

	
	/**
	 * @return the second parameter of this rule
	 */
	public ASTNode getSecond() {
		return getFirst().getNext();
	} // getParent
} // AddChildToRuleNode.java
