/*	
 * DebugInfoNode.java  	$Revision: 86 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-07-30 12:35:45 +0200 (Do, 30 Jul 2009) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.debuginfo;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * A node for debuginfo nodes.
 *   
 * @author  Roozbeh Farahbod
 */
public class DebugInfoNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public DebugInfoNode(ScannerInfo info) {
		super(
				DebugInfoPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"DebugInfoRule",
				null,
				info);
	}
	
	public DebugInfoNode(DebugInfoNode node) {
		super(node);
	}

	/**
	 * @return the channel id node
	 */
	public ASTNode getId() {
		return this.getFirst();
	}
	
	/**
	 * @return the message part of this node
	 */
	public ASTNode getMessage() {
		return getId().getNext();
	}
}
