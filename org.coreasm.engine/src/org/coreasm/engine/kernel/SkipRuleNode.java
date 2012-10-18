/*	
 * SkipRuleNode.java  	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.kernel;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * This is the node type representing the 'skip' node. 
 * Other plugins can use this node to create a skip rule node.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class SkipRuleNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a 'skip' rule node with the 
	 * given scanner information.
	 * 
	 * @param info scanner information
	 */
	public SkipRuleNode(ScannerInfo info) {
		super(
				Kernel.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				Kernel.GR_SKIP,
				Kernel.KW_SKIP,
				info,
				Node.KEYWORD_NODE
				);
	}
	
	/**
	 * @see ASTNode#ASTNode(ASTNode)
	 */
	public SkipRuleNode(SkipRuleNode node) {
		super(node);
	}
	
}
