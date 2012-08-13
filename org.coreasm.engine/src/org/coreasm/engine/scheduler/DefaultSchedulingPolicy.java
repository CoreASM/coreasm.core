/*
 * DefaultSchedulingPolicy.java 		$Revision: 95 $
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


package org.coreasm.engine.scheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.util.Tools;


/**
 * Default scheduling policy of the scheduler component of the engine.
 * 
 * Different schedules provided by this policy are always independent.
 *   
 * @author Roozbeh Farahbod
 */

public class DefaultSchedulingPolicy implements SchedulingPolicy {

	/** Maximum number of elements considered, 30 */
	public static final int MAX_SET_SIZE = 30;

	public Iterator<Set<Element>> getNewSchedule(Set<? extends Element> set) {
		return new DefaultIterator(set);
	}
	

	/**
	 * Does nothing.
	 * 
	 * @see SchedulingPolicy#clearGroup(Object)
	 */
	public void clearGroup(Object groupHandle) {
		// do nothing
	}

	/**
	 * @return null
	 * 
	 * @see SchedulingPolicy#getNewGroup()
	 */
	public Object getNewGroup() {
		return null;
	}

	/**
	 * @see #getNewSchedule(Set)
	 */
	public Iterator<Set<Element>> getNewSchedule(Object groupHandle, Set<? extends Element> set) {
		return getNewSchedule(set);
	}

	/**
	 * Iterator for the default scheduling policy. 
	 * This iterator gets a set of elements and provides 
	 * an iterator over all the possible subsets of the
	 * given set.
	 *   
	 * @author Roozbeh Farahbod
	 *
	 */
	protected class DefaultIterator implements Iterator<Set<Element>> {
		
		private final List<Element> list;
		private final List<Integer> iteratedIndices;
		private final int max_tries;	// this is actually an int value
		
		/**
		 * Creates a new default iterator over the given set.
		 * If the set is larger than {@link DefaultSchedulingPolicy#MAX_SET_SIZE}
		 * then a subset of the given set (no larger than {@link DefaultSchedulingPolicy#MAX_SET_SIZE}
		 * is considered.  
		 */
		public DefaultIterator(Set<? extends Element> set) {
			List<Element> tempList = new ArrayList<Element>(set);

			// Here I pick a subset of the given set with a size of MAX_SET_SIZE
            if (set.size() > MAX_SET_SIZE) {
    			this.list = new ArrayList<Element>();
            	int clipIndex = Tools.randInt(set.size() - MAX_SET_SIZE + 1);
            	for (int i = 0; i < MAX_SET_SIZE; i++)
            		list.add(tempList.get(i + clipIndex));
            } else
    			this.list = new ArrayList<Element>(set);

			this.iteratedIndices = new ArrayList<Integer>();
			this.max_tries = (int)Math.round(Math.pow(2, list.size())) - 1; 
		}

		public boolean hasNext() {
			return iteratedIndices.size() < max_tries;
		}

		public Set<Element> next() {
			if (!hasNext()) 
				throw new NoSuchElementException("There is no possible combination left.");
			
			if (list.size() == 1) {
				return new HashSet<Element>(list);
			}
			else {
				Set<Element> result = new HashSet<Element>();

	            // choose a subset index randomly
				int selectedIndex;
				do 
					selectedIndex = 1 + Tools.randInt(max_tries);
				while 
					(iteratedIndices.contains(selectedIndex));
				
				iteratedIndices.add(selectedIndex);
					
	            // compose the resultant subset based on the binary 
				// representation of the  selected subset index
				int temp = selectedIndex;
				int listIndex = 0;
				while (temp > 0) {
					if ((temp % 2) == 1)
						result.add(list.get(listIndex));
					temp = temp/2;
					listIndex++;
				}
				return result;
			}
			
		}

		/**
		 * Not supported.
		 * 
		 * @throws UnsupportedOperationException 
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		
	}

	/*
	 * @throws UnsupportedOperationException
	 * 
	 * @see {@link SchedulingPolicy#getNewSchedule(Set, Set)}
	 *
	public <E> Iterator<Set<E>> getNewSchedule(Set<E> set, Set<E> blacklist) {
		// TODO Not implemented yet!
		throw new UnsupportedOperationException("getNewSchedule(Set<E>, Set<E>) is not supported by " + this.getClass().getSimpleName());
	}
	*/

}
