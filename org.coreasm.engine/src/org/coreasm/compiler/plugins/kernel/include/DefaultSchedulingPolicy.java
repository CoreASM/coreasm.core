package org.coreasm.compiler.plugins.kernel.include;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.scheduler.SchedulingPolicy;

/**
 * Default scheduling policy
 * @author Spellmaker
 *
 */
public class DefaultSchedulingPolicy implements SchedulingPolicy {

	/** Maximum number of elements considered, 30 */
	public static final int MAX_SET_SIZE = 30;

	public Iterator<Set<Element>> getNewSchedule(java.util.Set<? extends Element> set) {
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
		 * @param set The set of agents
		 */
		public DefaultIterator(Set<? extends Element> set) {
			List<Element> tempList = new ArrayList<Element>(set);

			// Here I pick a subset of the given set with a size of MAX_SET_SIZE
            if (set.size() > MAX_SET_SIZE) {
    			this.list = new ArrayList<Element>();
            	int clipIndex = CompilerRuntime.RuntimeProvider.getRuntime().randInt(set.size() - MAX_SET_SIZE + 1);
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
				throw new Error("There is no possible combination left.");
			
			if (list.size() == 1) {
				return new HashSet<Element>(list);
			}
			else {
				Set<Element> result = new HashSet<Element>();

	            // choose a subset index randomly
				int selectedIndex;
				do 
					selectedIndex = 1 + CompilerRuntime.RuntimeProvider.getRuntime().randInt(max_tries);
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
		
		public void remove() {
			throw new UnsupportedOperationException();
		}	
	}
}
