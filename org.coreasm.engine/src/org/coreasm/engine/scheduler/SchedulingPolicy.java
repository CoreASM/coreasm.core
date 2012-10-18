/*	
 * SchedulingPolicy.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005-2008 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.scheduler;

import java.util.Iterator;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;

/** 
 *	Interface for scheduling policy.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public interface SchedulingPolicy {
	
	/**
	 * Returns a new schedule (an iterator to a list of possible
	 * subsets of the given set). This will mainly be used by the
	 * scheduler to try various sets of agents at runtime.
	 */
	public Iterator<Set<Element>> getNewSchedule(Set<? extends Element> set);

	/**
	 * Returns a new schedule (an iterator to a list of possible
	 * subsets of the given set). The schedule will belong to a group 
	 * of schedules denoted by the given <code>handle</code>.
	 * Depending on the implementation, there can be a correlation in the 
	 * behaviors of schedules in a group. 
	 * 
	 * @param groupHandle an object representing the group
	 * @param set set of objects to be scheduled
	 * @return an iterator to a set of objects (the schedule)
	 */
	public Iterator<Set<Element>> getNewSchedule(Object groupHandle, Set<? extends Element> set);
	
	/**
	 * @return a new handle (unique for this policy) for a group of schedules 
	 */
	public Object getNewGroup();
	
	/**
	 * Clears the 'memory' of the group. 
	 */
	public void clearGroup(Object groupHandle);

	/*
	 * Returns an iterator to a list of possible
	 * subsets of the given set, avoiding any subset that has the
	 * black listed elements together in it. 
	 * 
	 * This will mainly be used by the
	 * scheduler to try various sets of agents at runtime.
	 * 
	 * @throws UnsupportedOperationException if this operation is not supported.
	 *
	public <E> Iterator<Set<E>> getNewSchedule(Set<E> set, Set<E> blacklist);
	*/
}
