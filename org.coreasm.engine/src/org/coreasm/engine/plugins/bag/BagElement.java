/*	
 * BagElement.java  	$Revision: 243 $
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.bag.BagUpdateElement.BagUpdateType;
import org.coreasm.engine.plugins.collection.AbstractBagElement;
import org.coreasm.engine.plugins.collection.ModifiableCollection;
import org.coreasm.engine.plugins.number.NumberElement;


/** 
 * Bag element.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class BagElement extends AbstractBagElement implements ModifiableCollection {

	protected final Map<Element,Integer> members;
	
	// It is a list to improve performance
	protected List<Element> enumerationCachse = null;
	
	public BagElement() {
		members = new HashMap<Element,Integer>();
	}
	
	public BagElement(Collection<? extends Element> newMembers) {
		this();
		for (Element e: newMembers) 
			this.addMember(e);
	}
	
	public BagElement(Map<? extends Element, Integer> map) {
		this();
		for (Entry<? extends Element, Integer> e: map.entrySet()) 
			this.addMember(e.getKey(), e.getValue());
	}
	
	public BagElement(BagElement anotherBag) {
		this(anotherBag.members);
	}
	
	public String getBackground() {
		return BagBackgroundElement.BAG_BACKGROUND_NAME;
	}

	/* 
	 * Adds a new member to this element 'count' times. Bag elements should not 
	 * be modified when created; so, this method should only 
	 * be called from a constructor. 
	 */
	private void addMember(Element newMember, int count) { 
		Integer c = members.get(newMember);
		if (c == null) {
			c = 0;
		}
		members.put(newMember, c + count);
	}
	
	/* 
	 * Adds a new member to this element. Bag elements should not 
	 * be modified when created; so, this method should only 
	 * be called from a constructor. 
	 */
	private void addMember(Element newMember) {
		addMember(newMember, 1);
	}
	
	@Override
	public String denotation() {
	
		String str = BagPlugin.BAG_OPEN_SYMBOL;
		
		// for all members of this bag
		for (Element m : enumerate()) {
			str = str + m.denotation() + ", ";
		}
				
		if (str.length() > BagPlugin.BAG_OPEN_SYMBOL.length())
			str = str.substring(0, str.length()-2);
		
		return str + BagPlugin.BAG_CLOSE_SYMBOL;
	}
	
	/**
	 * Returns a <code>String</code> representation of 
	 * this bag element.
	 * 
	 * @see org.coreasm.engine.absstorage.Element#toString()
	 */
	@Override
	public String toString() {
	
		String str = "";
		
		// left brace:
		str = str + BagPlugin.BAG_OPEN_SYMBOL;
		
		
		// for all members of this bag
		for (Element m : enumerate()) {
			// if not first element, add comma to beginning
			if (str.length() > BagPlugin.BAG_OPEN_SYMBOL.length())
				str = str + ", ";
			
			// add string representation of element to set.
			str = str + m.toString();
		}
				
		// right brace:		
		str = str + BagPlugin.BAG_CLOSE_SYMBOL;
				
		return str;
	}
	
	//----------------------
	// Equality interface
	//----------------------
	
	/**
 	 * Compares this Element to the specified Element. 
 	 * The result is <code>true</code> if the argument 
 	 * is not null and is considered to be equal to this Element.
 	 * 
 	 * @param anElement the Element to compare with.
 	 * @return <code>true</code> if the Elements are equal; <code>false</code> otherwise.
 	 * @throws IllegalArgumentException if <code>anElement</code> is not an instance
 	 * of <code>Element</code>
 	 */
 	public boolean equals(Object anElement) {
 		
 		boolean equals = false;

 		// if both java objects are identical, no further checks are required
 		if (super.equals(anElement))
 			equals = true;
 		// else both java objects are not identical, have to check that
 		// both are set elements, both have same size, and same members
 		else
 		{
	 		// both bag elements
	 		if (anElement instanceof BagElement)
	 		{
	 			BagElement oBag = (BagElement)anElement;
	 			
	 			// both contain same number of members
	 			if (this.enumerate().size() == oBag.enumerate().size())
	 			{
	 				for (Entry<? extends Element, Integer> e: this.members.entrySet()) {
	 					Integer c = oBag.members.get(e.getKey());
	 					if (c == null || !c.equals(e.getValue()))
	 						return false;
	 				}
	
	 				equals = true;
	 				
	 			}
	 		}
 		}
 		
 		return equals;
	}
 	
 	/**
	 * Hashcode for Bag elements. Must be overridden because equality is overridden. 
	 *  
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// two bag elements with the same members will have members with the same
		// hashCodes so add it up
		
		int resultantHashCode = 0;
		
		// sum up hashcode of member elements
		for (Element e : enumerate())
		    resultantHashCode = resultantHashCode + (e==null ? 0 : e.hashCode());
		
		return resultantHashCode; 
	}

	@Override
	public Element get(Element key) {
		Integer c = members.get(key);
		if (c != null && c > 0)
			return NumberElement.getInstance(c);
		else
			return NumberElement.getInstance(0);
	}

	public UpdateMultiset computeAddUpdate(Location loc, Element e, Element agent, Node node) {
		Update u = new Update(loc, new BagUpdateElement(BagUpdateType.ADD, e), BagPlugin.BAG_UPDATE_ACTION, agent, node.getScannerInfo());
		//Update u = new Update(loc, e, BagPlugin.BAG_ADD_ACTION);
		return new UpdateMultiset(u);
	}

	public UpdateMultiset computeRemoveUpdate(Location loc, Element e, Element agent, Node node) {
		Update u = new Update(loc, new BagUpdateElement(BagUpdateType.REMOVE, e), BagPlugin.BAG_UPDATE_ACTION, agent, node.getScannerInfo());
		//Update u = new Update(loc, e, BagPlugin.BAG_REMOVE_ACTION);
		return new UpdateMultiset(u);
	}

	@Override
	public AbstractBagElement getNewInstance(Collection<? extends Element> bag) {
		return new BagElement(bag);
	}

	@Override
	public boolean containsKey(Element key) {
		Integer c = members.get(key);
		return (c != null && c > 0);
	}

	@Override
	public boolean containsValue(Element value) {
		return (members.values().contains(value));
	}

	@Override
	public boolean isEmpty() {
		return members.isEmpty();
	}

	@Override
	public Set<Element> keySet() {
		return Collections.unmodifiableSet(members.keySet());
	}

	public int size() {
		return intSize();
	}

	/**
	 * Returns the size of this set in integer.
	 */
	public int intSize() {
		int c = 0;
		for (Entry<? extends Element, Integer> e: members.entrySet()) 
			if (e.getValue() != null)
				c += e.getValue();
		return c;
	}
	
	@Override
	public Collection<Element> values() {
		if (members.values().size() == 0)
			return Collections.emptyList();

		Collection<Element> result = new ArrayList<Element>();
		for (Integer i: members.values())
			result.add(NumberElement.getInstance(i));
		return result;
	}

	//----------------------
	// Enumerable Interface
	//----------------------

	public Collection<Element> enumerate() {
		return getIndexedView();
	}
	
	public boolean contains(Element e) {
	    return this.containsKey(e);
    }

	@Override
	public Map<Element, Element> getMap() {
		Map<Element, Element> result = new HashMap<Element, Element>();
		for (Entry<? extends Element, Integer> e: members.entrySet())
			if (e.getValue() != null && e.getValue() > 0) {
				result.put(e.getKey(), NumberElement.getInstance(e.getValue()));
			}
		return result;
	}

	public List<Element> getIndexedView()
			throws UnsupportedOperationException {
		if (enumerationCachse == null) {
			List<Element> result = new ArrayList<Element>();
			for (Entry<? extends Element, Integer> e: members.entrySet())
				if (e.getValue() != null && e.getValue() > 0) {
					for (int i=0; i < e.getValue(); i++)
						result.add(e.getKey());
				}
			enumerationCachse = Collections.unmodifiableList(result);
		}
		return enumerationCachse;
	}

	public boolean supportsIndexedView() {
		return true;
	}

}

