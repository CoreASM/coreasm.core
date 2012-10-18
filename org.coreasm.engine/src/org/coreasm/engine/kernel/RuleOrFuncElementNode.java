/*	
 * RuleOrFuncElementNode.java 	1.0 	$Revision: 243 $
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
 * Node of rule/function element terms.
 * 
 * @author Roozbeh Farahbod
 * 
 */
public class RuleOrFuncElementNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public RuleOrFuncElementNode(RuleOrFuncElementNode node) {
		super(node);
	}
	
	public RuleOrFuncElementNode(ScannerInfo info) {
		super(
				null,
				ASTNode.EXPRESSION_CLASS,
				Kernel.GR_RULE_OR_FUNCTION_ELEMENT_TERM,
				null,
				info
				);
	}

	/**
	 * @return the name of the rule or function
	 */
	public String getElementName() {
		return this.getFirst().getToken();
	}
	
		
}
