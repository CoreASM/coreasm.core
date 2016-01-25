/*
 * AgentContextMap.java 		$Revision: 80 $
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

/**
 * Keeps a volatile map of agent context
 *
 */
public class AgentContextMap {

	private final Map<Element, AgentContext> map = new HashMap<Element, AgentContext>();
	
	public synchronized void put(Element agent, AgentContext context) {
		map.put(agent, context);
	}
	
	public synchronized AgentContext get(Element agent) {
		return map.get(agent);
	}
	
	public synchronized void clear() {
		for (AgentContext context : map.values()) {
			context.interpreter.dispose();
			context.interpreter = null;
			context.nodeCopyCache.clear();
		}
		map.clear();
	}
}
