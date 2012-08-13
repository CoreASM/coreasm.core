/*	
 * ExtendRuleNode.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.extendrule;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Node for extend rules
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ExtendRuleNode extends ASTNode {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
	 */
	public ExtendRuleNode(ScannerInfo info) {
		super(ExtendRulePlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"ExtendRule",
				null,
				info);
	}

	public ExtendRuleNode(ExtendRuleNode node) {
		super(node);
	}
	
	/**
	 * Returns the universe part of this rule
	 * @return a node
	 */
	public ASTNode getUniverseNode() {
		return getFirst();
	}
	
	/**
	 * Returns the id part of this rule
	 * @return a node
	 */
	public ASTNode getIdNode() {
		return getFirst().getNext();
	}
	
	/**
	 * Returns the name of the universe to be extended
	 * @return name of the universe
	 */
	public String getUniverseName() {
		return getFirst().getFirst().getToken();
	}
	
	/** 
	 * Returns the sub-rule part of this rule
	 * @return a node
	 */
	public ASTNode getRuleNode() {
		return getFirst().getNext().getNext();
	}
}
