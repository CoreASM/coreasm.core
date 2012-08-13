/*	
 * ShiftRuleNode.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.list;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Represents shift rules.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ShiftRuleNode extends ASTNode {

	private static final long serialVersionUID = 1L;
	
	public final boolean isLeft;
	
	public ShiftRuleNode(ShiftRuleNode node) {
		super(node);
		this.isLeft = node.isLeft;
	}
	
	public ShiftRuleNode(ScannerInfo info, boolean isLeft) {
		super(ListPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"ShiftRule",
				null,
				info);
		this.isLeft = isLeft;
	}
	
	public ASTNode getListNode() {
		return getFirst();
	}
	
	public ASTNode getLocationNode() {
		return getFirst().getNext();
	}
}
