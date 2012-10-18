/*	
 * AddVertexRuleNode.java 	$Revision: 80 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.network.plugins.graph;


import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	AST node for add-vertex rules.
 *   
 *  @author  Roozbeh Farahbod
 */
public class AddVertexRuleNode extends ASTNode {
	
	private static final long serialVersionUID = 1L;

	public AddVertexRuleNode(ScannerInfo info) {
		super(
				GraphPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				GraphPlugin.ADD_VERTEX_GR_NAME,
				null,
				info);
	}
	
	/**
	 * @param node
	 */
	public AddVertexRuleNode(AddVertexRuleNode node) {
		super(node);
	}
	
	/**
	 * @return the node representing the vertex being added
	 */
	public ASTNode getVertex() {
		return getFirst();
	}
	
	/**
	 * @return the node representing the graph
	 */
	public ASTNode getGraph() {
		return getFirst().getNext();
	}

}
