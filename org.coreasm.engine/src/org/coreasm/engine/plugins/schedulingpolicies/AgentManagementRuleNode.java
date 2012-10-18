/*	
 * AgentManagementRuleNode.java  	$Revision: 95 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-08-04 12:40:53 +0200 (Di, 04 Aug 2009) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.schedulingpolicies;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * A node for <code>'suspend' Term<code> nodes.
 *   
 * @author  Roozbeh Farahbod
 */
public class AgentManagementRuleNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public AgentManagementRuleNode(ScannerInfo info, String ruleName) {
		super(
				SchedulingPoliciesPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				ruleName,
				null,
				info);
	}
	
	public AgentManagementRuleNode(AgentManagementRuleNode node) {
		super(node);
	}

	/**
	 * @return the keyword part of this node
	 */
	public String getKeyword() {
		return this.getFirstCSTNode().getToken();
	}
	
	/**
	 * @return the term part of this node that refers to the agent
	 */
	public ASTNode getAgent() {
		return getFirst();
	}
}
