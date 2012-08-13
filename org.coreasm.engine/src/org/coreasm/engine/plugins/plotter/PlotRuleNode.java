/*	
 * PlotRuleNode.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.plotter;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * A node-wrapper for Plot rule nodes.
 *   
 * @author  Roozbeh Farahbod
 * 
 */

public class PlotRuleNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public PlotRuleNode(ScannerInfo info) {
		super(
				PlotterPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"PlotRule",
				null,
				info);
	}
	
	public PlotRuleNode(PlotRuleNode node) {
		super(node);
	}

	/**
	 * Returns the node that contains the function to be plotted.
	 * 
	 * @return a node
	 */
	public ASTNode getFunctionNode() {
		return this.getFirst();
	}
	
	/**
	 * Returns the id of the plotter window
	 * 
	 * @return a node
	 */
	public ASTNode getWindowId() {
		return this.getFirst().getNext();
	}
	
}
