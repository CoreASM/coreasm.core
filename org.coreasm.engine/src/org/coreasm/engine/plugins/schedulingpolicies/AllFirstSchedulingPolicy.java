/*
 * AllFirstSchedulingPolicy.java 		$Revision: 95 $
 * 
 * Copyright (c) 2008 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-08-04 12:40:53 +0200 (Di, 04 Aug 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.engine.plugins.schedulingpolicies;

import java.util.Iterator;

import java.util.NoSuchElementException;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;

/**
 * A scheduling policy that first tries to run all the agents
 * together, if failed, drops down to the default policy. 
 *   
 * @author Roozbeh Farahbod
 *
 */

public class AllFirstSchedulingPolicy extends BasicSchedulingPolicy {

	/**
	 * @see BasicSchedulingPolicy#BasicSchedulingPolicy(Set)
	 */
	public AllFirstSchedulingPolicy(Set<Element> suspendedAgents) {
		super(suspendedAgents);
	}

	@Override
	public Iterator<Set<Element>> getNewSchedule(Set<? extends Element> set) {
		return new ExtendedIterator(filteredSet(set), super.getNewSchedule(set));
	}

	/*
	 * @throws {@link UnsupportedOperationException} 
	 *
	@Override
	public <E> Iterator<Set<E>> getNewSchedule(Set<E> set, Set<E> blacklist) {
		throw new UnsupportedOperationException();
	}
	*/
	
	protected class ExtendedIterator implements Iterator<Set<Element>> {

		private final Iterator<Set<Element>> iterator;
		private final Set<Element> set;
		private boolean firstTime = true;
		
		public ExtendedIterator(Set<Element> set, Iterator<Set<Element>> iterator) {
			this.set = set;
			this.iterator = iterator;
		}
		
		public boolean hasNext() {
			if (firstTime)
				return set.size() > 0;
			else
				return iterator.hasNext();
		}

		public Set<Element> next() {
			if (firstTime) {
				if (set.size() == 0)
					throw new NoSuchElementException("There is no possible combination left.");
				firstTime = false;
				return set;
			} else
				return iterator.next();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

}
