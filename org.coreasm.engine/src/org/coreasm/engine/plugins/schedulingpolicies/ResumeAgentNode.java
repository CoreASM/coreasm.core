/*	
 * ResumeAgentNode.java  	$Revision: 95 $
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
 * A node for <code>'resume' Term<code> nodes.
 *   
 * @author  Roozbeh Farahbod
 */
public class ResumeAgentNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public ResumeAgentNode(ScannerInfo info) {
		super(
				SchedulingPoliciesPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"ResumeAgentRule",
				null,
				info);
	}
	
	public ResumeAgentNode(ResumeAgentNode node) {
		super(node);
	}

	/**
	 * @return the term part of this node that refers to the agent
	 */
	public ASTNode getAgent() {
		return getFirst();
	}
}
