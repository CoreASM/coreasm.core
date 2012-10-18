/*	
 * BagUpdateContainer.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.bag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.plugins.bag.BagUpdateElement.BagUpdateType;
import org.coreasm.util.HashMultiset;
import org.coreasm.util.Multiset;

/** 
 * Collection of bag updates.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class BagUpdateContainer extends BagAbstractUpdateElement {
	
	protected final Collection<? extends BagAbstractUpdateElement> updateElements;
	
	/**
	 * Creates a new Bag update container with the given 
	 * collection of elements. The given collection can NOT be null.
	 */
	public BagUpdateContainer(Collection<? extends BagAbstractUpdateElement> updateElements) {
		if (updateElements == null)
			throw new NullPointerException("Bag update elements cannot be null.");
		
		if (updateElements instanceof List)
			this.updateElements = Collections.unmodifiableList((List<? extends BagAbstractUpdateElement>)updateElements);
		else
		if (updateElements instanceof Set)
			this.updateElements = Collections.unmodifiableSet((Set<? extends BagAbstractUpdateElement>)updateElements);
		else
			this.updateElements = Collections.unmodifiableCollection(updateElements);

	}
	
	/**
	 * Creates a new container that is a sequential composition of the given 
	 * bag update elements.
	 */
	public static BagUpdateContainer compose(BagAbstractUpdateElement firstUpdate, 
			BagAbstractUpdateElement secondUpdate) {
		ArrayList<BagAbstractUpdateElement> result = new ArrayList<BagAbstractUpdateElement>();
		result.add(firstUpdate);
		result.add(secondUpdate);
		return new BagUpdateContainer(result);
	}
		
	public boolean equals(Object other) {
		if (other instanceof BagUpdateContainer) {
			return this.updateElements.equals(((BagUpdateContainer)other).updateElements);
		} else
			return false;
	}
	
	public int hashCode() {
		return updateElements.hashCode() + 1;
	}
	
	public String toString() {
		String result = updateElements.toString();
		
		if (updateElements instanceof Set) {
			result = "{" + result.substring(1, result.length()-1) + "}";
		}
		
		return result;
	}
	
	/*
	 * Returns true if this update container, has any absolute update.
	 *
	public boolean hasAbsoluteUpdate() {
		if (hasAbsoluteUpdateCache == null) { 
			for (BagAbstractUpdateElement ue: updateElements) {
				if (ue instanceof BagUpdateElement) {
					if (((BagUpdateElement)ue).type.equals(BagUpdateType.ABSOLUTE)) {
						hasAbsoluteUpdateCache = true;
						break;
					}
				} 
				else if (ue instanceof BagUpdateContainer) {
					if (((BagUpdateContainer)ue).hasAbsoluteUpdateCache) {
						hasAbsoluteUpdateCache = true;
						break;
					}
				}
			}
			if (hasAbsoluteUpdateCache == null)
				hasAbsoluteUpdateCache = false;
		}
		return hasAbsoluteUpdateCache;
	}
	*/	

	/**
	 * Assuming that the updates in this update container are consistent, 
	 * this method applies the updates in this update container to the given 
	 * bag value and returns the resulting value.
	 */
	public BagElement aggregateUpdates(BagElement currentValue) {
		Collection<Element> result = new ArrayList<Element>(currentValue.enumerate());
		aggregateUpdates(result);
		return new BagElement(result);
	}

	/**
	 * Assuming that the updates in this update container are consistent, 
	 * this method applies the updates in this update container to the given 
	 * collection.
	 */
	private void aggregateUpdates(Collection<Element> currentValue) {
		List<BagUpdateElement> updates = aggregateUpdates();
		for (BagUpdateElement ue: updates) {
			if (ue.type.equals(BagUpdateType.REMOVE))
				currentValue.remove(ue.value);
		}
		for (BagUpdateElement ue: updates) {
			if (ue.type.equals(BagUpdateType.ADD))
				currentValue.add(ue.value);
		}
	}

	/*
	 * Assuming that the updates in this update container are consistent, 
	 * this method combines them into a single multiset of add/remove operations.
	 *
	private Multiset<BagUpdateElement> aggregateUpdates(Multiset<BagUpdateElement> updatesSoFar) {
		boolean isSequential = updateElements instanceof List;
		Multiset<BagUpdateElement> updates = updatesSoFar;//new HashMultiset<BagUpdateElement>();
		
		if (isSequential) {
			// changes carry forward to the next operations
			for (BagAbstractUpdateElement u: updateElements) {
				if (u instanceof BagUpdateContainer)
					for (BagUpdateElement ue: ((BagUpdateContainer)u).aggregateUpdates())
						addUpdateInSequence(updates, ue);
				else 
				if (u instanceof BagUpdateElement) 
					addUpdateInSequence(updates, (BagUpdateElement)u);
			}
		} else {
			// changes do not carry forward to the next operations
			for (BagAbstractUpdateElement u: updateElements) {
				if (u instanceof BagUpdateContainer)
					updates.addAll(((BagUpdateContainer)u).aggregateUpdates());
				else
					if (u instanceof BagUpdateElement) {
						BagUpdateElement ue = (BagUpdateElement)u;
						switch(ue.type) {
						case ADD:
							updates.add(ue);
							break;
						case REMOVE:
							updates.add(ue);
							break;
						}
					}
			}
		}
		
		return updates;
	}
	*/
	
	/*
	 * Assuming that the updates in this update container are consistent, 
	 * this method combines them into a single multiset of add/remove operations.
	 */
	protected List<BagUpdateElement> aggregateUpdates() {
		boolean isSequential = updateElements instanceof List; 
		List<BagUpdateElement> updates = new ArrayList<BagUpdateElement>();
		
		if (isSequential) {
			// changes carry forward to the next operations
			if (updateElements.size() == 1 && ((List<? extends BagAbstractUpdateElement>)updateElements).get(0) instanceof BagUpdateContainer) 
				return ((BagUpdateContainer)((List<? extends BagAbstractUpdateElement>)updateElements).get(0)).aggregateUpdates();
			
			for (BagAbstractUpdateElement u: updateElements) {
				if (u instanceof BagUpdateContainer)
					for (BagUpdateElement ue: ((BagUpdateContainer)u).aggregateUpdates())
						addUpdateInSequence(updates, ue);
				else 
				if (u instanceof BagUpdateElement) 
					addUpdateInSequence(updates, (BagUpdateElement)u);
			}
		} else {
			// changes do not carry forward to the next operations
			for (BagAbstractUpdateElement u: updateElements) {
				if (u instanceof BagUpdateContainer)
					updates.addAll(((BagUpdateContainer)u).aggregateUpdates());
				else
					if (u instanceof BagUpdateElement) {
						BagUpdateElement ue = (BagUpdateElement)u;
						switch(ue.type) {
						case ADD:
							updates.add(ue);
							break;
						case REMOVE:
							updates.add(ue);
							break;
						}
					}
			}
		}
		
		return updates;
	}
	/**/
	
	/*
	 * Adds a new update to the update multiset in a sequential manner. So, if the given 
	 * update overwrites one that is already in the set, it removes the existing one. 
	 */
	private void addUpdateInSequence(List<BagUpdateElement> updates, BagUpdateElement nextUpdate) {
		switch(nextUpdate.type) {
		
		case ADD:
			BagUpdateElement removeU = new BagUpdateElement(BagUpdateType.REMOVE, nextUpdate.value);
				updates.add(nextUpdate);
			break;

		case REMOVE:
			BagUpdateElement addU = new BagUpdateElement(BagUpdateType.ADD, nextUpdate.value);
			if (updates.contains(addU))
				updates.remove(addU);
			else
				updates.add(nextUpdate);
			break;
		}
	}
	
	/*
	 * Assuming that the updates in this update container are consistent, 
	 * this method applies the updates in this update container to the given 
	 * collection.
	 *
	private void aggregateUpdates_OLD(Collection<Element> currentValue) {
		for (BagAbstractUpdateElement u: updateElements) {
			if (u instanceof BagUpdateContainer)
				((BagUpdateContainer)u).aggregateUpdates_OLD(currentValue);
			else
				if (u instanceof BagUpdateElement) {
					BagUpdateElement ue = (BagUpdateElement)u;
					switch(ue.type) {
					case ADD:
						currentValue.add(ue.value);
						break;
					case REMOVE:
						currentValue.remove(ue.value);
						break;
//					case ABSOLUTE:
//						if (ue.value instanceof BagElement)
//							currentValue = ((BagElement)ue.value).enumerate(); 
//						else
//							throw new EngineError("Invalid Bag value in aggregation: " + ue.value);
					}
				}
		}
	}
	*/
	

}
