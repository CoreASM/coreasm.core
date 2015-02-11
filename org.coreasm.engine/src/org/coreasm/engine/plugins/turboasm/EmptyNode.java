/*	
 * EmptyNode.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.turboasm;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * A NO-OP rule node like skip.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class EmptyNode extends ASTNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8936982237213357521L;

	/**
	 * Creates an empty node with the given scanner information.
	 */
	public EmptyNode(ScannerInfo scannerInfo) {
		super(TurboASMPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"EmptyRule",
				null,
				scannerInfo);
	}

	public EmptyNode(EmptyNode node) {
		super(node);
	}

}
