/*	
 * TrueGuardNode.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
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
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * An always TRUE guard
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class TrueGuardNode extends ASTNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TrueGuardNode(Node parent) {
		super(
				ListPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"",
				null,
				ScannerInfo.NO_INFO);
    	parent.addChild(this);
    	this.setParent(parent);
    }

}
