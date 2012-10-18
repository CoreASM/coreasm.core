/*	
 * LocalRuleNode.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.turboasm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Wrapper for Local rule nodes.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class LocalRuleNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	private List<String> functionNames = null;
	
	/**
	 */
	public LocalRuleNode(ScannerInfo info) {
		super(
				TurboASMPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"LocalRule",
				null,
				info);
	}

	public LocalRuleNode(LocalRuleNode node) {
		super(node);
	}
	
	/**
	 * Returns the set local function names. This method
	 * assumes that the node structure does not change after 
	 * the first call to this method, so it caches the result.
	 * 
	 * @return a node
	 */
	public Collection<String> getFunctionNames() {
		if (functionNames == null) {
			functionNames = new ArrayList<String>();
			for (Node n: getAbstractChildNodes("lambda"))
				functionNames.add(n.getToken());
		}
		return functionNames;
	}
	
	/** 
	 * Returns the sub-rule part of this rule
	 * 
	 * @return a node
	 */
	public ASTNode getRuleNode() {
		return (ASTNode)getChildNode("alpha");
	}

}
