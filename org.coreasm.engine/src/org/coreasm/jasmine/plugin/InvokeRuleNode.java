/*
 * InvokeRuleNode.java 		$Revision: 9 $
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
 * Node type of JASMine invoke rule.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class InvokeRuleNode extends ASTNode {

	/**
	 * @see ASTNode#ASTNode(ASTNode)
	 */
	public InvokeRuleNode(InvokeRuleNode node) {
		super(node);
	}

	/**
	 * Creates a new node with the given scanner information
	 */
	public InvokeRuleNode(ScannerInfo info) {
		super(JasminePlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"JasmineInvokeRule",
				null,
				info
				);
	}
	
	/**
	 * Returns true if this invoke rule
	 * does not have a 'result into' clause.
	 */
	public boolean isVoidInvocation() {
		return (this.getChildNode("gamma") == null);
	}
}
