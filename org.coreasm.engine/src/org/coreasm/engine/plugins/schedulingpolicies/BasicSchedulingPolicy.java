/*	
 * BasicSchedulingPolicy.java  	$Revision: 95 $
 * 
 * Copyright (C) 2009 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-08-04 12:40:53 +0200 (Di, 04 Aug 2009) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.schedulingpolicies;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.scheduler.DefaultSchedulingPolicy;

/**
 * Default scheduling policy of the SchedulingPolicy plugin that extends the one 
 * offered by the engine to add a filter on the set of agents that are considered 
 * for scheduling.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class BasicSchedulingPolicy extends DefaultSchedulingPolicy {

	protected Set<Element> suspendedAgents = null;
	
	/**
	 * Creates a new basic scheduling policy with a reference to a set 
	 * of "suspended" agents. These agents will be removed from the set
	 * of agents available for scheduling.
	 * 
	 * @param suspendedAgents set of suspended agents
	 */
	public BasicSchedulingPolicy(Set<Element> suspendedAgents) {
		this.suspendedAgents = suspendedAgents;
	}

	@Override
	public Iterator<Set<Element>> getNewSchedule(Set<? extends Element> set) {
		return new DefaultIterator(filteredSet(set));
	}

	protected Set<Element> filteredSet(Set<? extends Element> elements) {
		final Set<Element> filteredSet = new HashSet<Element>(elements);
		filteredSet.removeAll(suspendedAgents);
		return filteredSet;
	}
}
