/*	
 * MakeTreeRuleNode.java
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
 *	AST node rule elements: 'make TERM into tree TERM'.
 *   
 *  @author  Franco Alberto Cardillo
 */
public class MakeTreeRuleNode extends ASTNode  {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MakeTreeRuleNode() {
		super(
				TreePlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"MakeTreeRule",
				null,
				null);
	}

	/**
	 * @param node
	 */
	public MakeTreeRuleNode(MakeTreeRuleNode node) {
		super(node);
	}

	
	/**
	 * @return returns the Tree parameter of this rule
	 */
	public ASTNode getSecond() {
		return getFirst().getNext();
	} // getParent
} // MakeTreeRuleNode.java
