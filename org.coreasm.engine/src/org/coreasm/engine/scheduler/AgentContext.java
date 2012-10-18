/*
 * AgentContext.java 		$Revision: 80 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 */

package org.coreasm.engine.scheduler;

import java.util.HashMap;
import java.util.Map;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;

/**
 * Keeps a context and caching of various info for every agent.
 *
 */
public class AgentContext {

	public final Element agent;
	public Interpreter interpreter = null;
	
	public Map<ASTNode, ASTNode> nodeCopyCache = new HashMap<ASTNode, ASTNode>();

	public AgentContext(Element agent) {
		this.agent = agent;
	}
	
	
}
