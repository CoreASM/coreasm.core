/*
 * OneByOneUnfairSchedulingPolicy.java 		$Revision: 183 $
 * 
 * Copyright (c) 2010 Hiren D. Patel
 *
 * Last modified on $Date: 2010-08-31 23:52:36 +0200 (Di, 31 Aug 2010) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.schedulingpolicies;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.scheduler.SchedulingPolicy;

public class OneByOneUnfairSchedulingPolicy extends BasicSchedulingPolicy implements SchedulingPolicy {

	private Random rand;
	
	/**
	 * @see BasicSchedulingPolicy#BasicSchedulingPolicy(Set)
	 */
	public OneByOneUnfairSchedulingPolicy(Set<Element> suspendedAgents) {
		super(suspendedAgents);
		rand = new Random();
	}

	public Iterator<Set<Element>> getNewSchedule(Set<? extends Element> set) {
		return new ExtendedIterator(filteredSet(set), null);
	}

	public Iterator<Set<Element>> getNewSchedule(Object groupHandle, Set<? extends Element> set) {
		return new ExtendedIterator(filteredSet(set), groupHandle);
	}
	
	protected class ExtendedIterator implements Iterator<Set<Element>> {

		private final Set<Element> originalSet;

		public ExtendedIterator(Set<Element> set, Object groupHandle) {
			originalSet = Collections.unmodifiableSet(set);
			// we can ignore the group handle --R.F.
			//this.handle = groupHandle;
		}
		
		public boolean hasNext() {
			if (originalSet.size() > 0) 
				return true;
			else 
				return false;
		}

		public Set<Element> next() {
			if (!hasNext())
				throw new NoSuchElementException("There is no possible combination left.");

			// since hasNext() is true, workingSet has at least one element in it
			// TODO performance can be improved
			final Element element = (Element) originalSet.toArray()[rand.nextInt(originalSet.size())];
			Set<Element> result = new HashSet<Element>();
			result.add(element);
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

}
