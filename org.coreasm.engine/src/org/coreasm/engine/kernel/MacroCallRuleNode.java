/*	
 * MacroCallRuleNode.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006-2007 Roozbeh Farahbod
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
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Macro Call rule node.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class MacroCallRuleNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public MacroCallRuleNode(MacroCallRuleNode node) {
		super(node);
	}

	public MacroCallRuleNode(ScannerInfo info) {
		super(
				Kernel.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"MacroCallRule",
				null,
				info);
	}

	/**
	 * @return the rule name 
	 */
	public String getRuleName() {
		return getFunctionRuleElement().getToken();
	}
	
	/**
	 * @return the function rule element
	 */
	public ASTNode getFunctionRuleElement() {
		return this.getFirst();
	}
}
