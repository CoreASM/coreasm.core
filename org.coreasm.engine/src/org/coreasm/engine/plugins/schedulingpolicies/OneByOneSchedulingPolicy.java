/*
 * OneByOneSchedulingPolicy.java 		$Revision: 95 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-08-04 12:40:53 +0200 (Di, 04 Aug 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.engine.plugins.schedulingpolicies;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.scheduler.SchedulingPolicy;

/**
 * A scheduling policy that chooses agents one by one.
 * 
 * For those schedules that belong 
 *   
 * @author Roozbeh Farahbod
 *
 */

public class OneByOneSchedulingPolicy extends BasicSchedulingPolicy implements SchedulingPolicy {

	private long handle = 1;
	
	private Map<Object, Set<Object>> groupMemory = new HashMap<Object, Set<Object>>();
	
	/**
	 * @see BasicSchedulingPolicy#BasicSchedulingPolicy(Set)
	 */
	public OneByOneSchedulingPolicy(Set<Element> suspendedAgents) {
		super(suspendedAgents);
	}

	public Iterator<Set<Element>> getNewSchedule(Set<? extends Element> set) {
		return new ExtendedIterator(filteredSet(set), null);
	}

	public void clearGroup(Object groupHandle) {
		groupMemory.remove(groupHandle);
	}

	public Object getNewGroup() {
		return handle++;
	}

	public Iterator<Set<Element>> getNewSchedule(Object groupHandle, Set<? extends Element> set) {
		Set<Object> memory = groupMemory.get(groupHandle);
		if (memory == null) {
			groupMemory.put(groupHandle, new HashSet<Object>());
		}
		return new ExtendedIterator(filteredSet(set), groupHandle);
	}
	
	protected class ExtendedIterator implements Iterator<Set<Element>> {

		private final Set<Element> workingSet;
		private final Set<Element> originalSet;
		private final Object handle;
		private final Set<Object> memory;
		private boolean chosenOnce = false;
		
		public ExtendedIterator(Set<Element> set, Object groupHandle) {
			workingSet = new HashSet<Element>(set);
			originalSet = Collections.unmodifiableSet(set);
			this.handle = groupHandle;
			if (handle != null) {
				memory = groupMemory.get(handle);
				if (workingSet.size() > 0) {
					workingSet.removeAll(memory);
					if (workingSet.size() == 0)
						resetMemory();
				}					
			} else
				memory = null;
		}
		
		public boolean hasNext() {
			if (!chosenOnce && workingSet.size() > 0) 
				return true;
			else 
				return false;
		}

		public Set<Element> next() {
			if (!hasNext())
				throw new NoSuchElementException("There is no possible combination left.");

			Iterator<Element> i = workingSet.iterator();
			// since hasNext() is true, workingSet has at least one element in it
			final Element element = i.next();
			Set<Element> result = new HashSet<Element>();
			result.add(element);
			if (memory != null) {
				memory.add(element);
			}
			chosenOnce = true;
			return result;
		}

		private void resetMemory() {
			memory.clear();
			workingSet.clear();
			workingSet.addAll(originalSet);
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

}
