/*
 * StoreRuleNode.java 		$Revision: 9 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.jasmine.plugin;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/**
 * Node type of JASMine store rule.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class StoreRuleNode extends ASTNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1617318616162102928L;

	/**
	 * @see ASTNode#ASTNode(ASTNode)
	 */
	public StoreRuleNode(StoreRuleNode node) {
		super(node);
	}

	/**
	 * Creates a new node with the given scanner information
	 */
	public StoreRuleNode(ScannerInfo info) {
		super(JasminePlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"JasmineStoreRule",
				null,
				info
				);
	}
	
}
