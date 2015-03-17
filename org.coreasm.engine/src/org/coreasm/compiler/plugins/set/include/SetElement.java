/*	
 * SetElement.java 	1.0 	$Revision: 243 $
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Copyright (C) 2005 Mashaal Memon
 * Copyright (c) 2007 Roozbeh Farahbob
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.compiler.plugins.set.include;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;

import CompilerRuntime.Rule;

import org.coreasm.engine.absstorage.Update;

import CompilerRuntime.UpdateList;

import org.coreasm.engine.plugins.collection.AbstractSetElement;
import org.coreasm.engine.plugins.set.SetBackgroundElement;
import org.coreasm.compiler.plugins.collection.include.ModifiableCollection;

/** 
 *	This implements the Set Element.
 *   
 *  @author  Mashaal Memon, Roozbeh Farahbod
 *  
 */
public class SetElement extends AbstractSetElement implements ModifiableCollection {

	protected static final Set<Element> falseValues;
	protected static final Set<Element> booleanValues;
	protected final Set<Element> members;
	
	protected Set<Element> enumCache = null;
	protected List<Element> enumListCache = null;
	protected Map<Element, Element> map = null;
	
	static {
		Set<Element> set = new HashSet<Element>();
		set.add(BooleanElement.FALSE);
		falseValues = Collections.unmodifiableSet(set);
		set = new HashSet<Element>();
		set.add(BooleanElement.FALSE);
		set.add(BooleanElement.TRUE);
		booleanValues = Collections.unmodifiableSet(set);
	}
	
	/**
	 * Constructs a new empty set
	 */
	public SetElement() {
		members = Collections.emptySet();
	}
	
	/**
	 * Constructs a new set from the given element source
	 * @param members The element source
	 */
	public SetElement(Collection<? extends Element> members) {
		this.members = Collections.unmodifiableSet(new HashSet<Element>(members));
	}
	
	/**
	 * Constructs a new set from the given element source
	 * @param anotherSet The element source
	 */
	public SetElement(SetElement anotherSet) {
		this(anotherSet.members);
	}
	
	public String getBackground() {
		return SetBackgroundElement.SET_BACKGROUND_NAME;
	}

	/*
	 * Adds a member element to this set. Returns true if element was not already present in set,
	 * and false otherwise.
	 *
	 * @return boolean value true if element was not already present in set, and false otherwise.
	 * @deprecated Set Elements should not be modified directly. Create new ones using java collections.
	 *
	@Deprecated
	public boolean addMember(Element m) {
		// returns true if member actually added, and false if it already existed and thus no add was done
		return members.add(m);
	}
	*/

	@Override
	public String denotation() {
	
		String str = "{";
		
		// for all members of this set
		for (Element m : enumerate()) {
			str = str + m.denotation() + ", ";
		}
				
		if (str.length() > 1)
			str = str.substring(0, str.length()-2);
		
		return str + "}";
	}
	
	/**
	 * Returns a <code>String</code> representation of 
	 * this set element.
	 * 
	 * @see org.coreasm.engine.absstorage.Element#toString()
	 */
	@Override
	public String toString() {
	
		String str = "";
		
		// left brace:
		str = str + "{";
		
		
		// for all members of this set
		for (Element m : enumerate()) {
			// if not first element, add comma to beginning
			if (str.length() > 1)
				str = str + ", ";
			
			// add string representation of element to set.
			str = str + m.toString();
		}
				
		// right brace:		
		str = str + "}";
				
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

 		// if both java objects are idential, no further checks are required
 		if (super.equals(anElement))
 			equals = true;
 		// else both java objects are not identical, have to check that
 		// both are set elements, both have same size, and same members
 		else
 		{
	 		// both set elements
	 		if (anElement instanceof SetElement)
	 		{
	 			SetElement oSet = (SetElement)anElement;
	 			
	 			// both contain same number of members
	 			if (intSize() == oSet.enumerate().size())
	 			{
	 				Collection<Element> oSetMember = oSet.enumerate();
	 				int matchCounter = 0;
	 				
	 				// for all members of this set
	 				for (Element m : enumerate())
	 				{
	 					// if any one member in this set is not contained in other set, break
	 					if (!oSetMember.contains(m))
	 						break;
	 					// else add one to match counter
	 					else
	 						matchCounter++;
	 						
	 				}
	 				
	 				// if number of matches is the same as size of this set
	 				// then other set is indeed equal to this set
	 				if (intSize() == matchCounter)
	 					equals = true;
	
	 			}
	 		}
 		}
 		
 		return equals;
	}
 	
 	/**
	 * Hashcode for Set elements. Must be overridden because equality is overridden. 
	 *  
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// two set elements with the same members will have members with the same
		// hashCodes so add it up
		
		int resultantHashCode = 0;
		
		// sum up hashcode of member elements
		for (Element e : enumerate())
		    resultantHashCode = resultantHashCode + (e==null ? 0 : e.hashCode());
		
		return resultantHashCode; 
	}

	@Override
	public Element get(Element key) {
		if (members.contains(key))
			return BooleanElement.TRUE;
		else
			return BooleanElement.FALSE;
	}

	public UpdateList computeAddUpdate(Location loc, Element e, Rule agent) {
		Update u = new Update(loc, e, SetAggregator.SETADD_ACTION, agent, null);
		return new UpdateList(u);
	}

	public UpdateList computeRemoveUpdate(Location loc, Element e, Rule agent) {
		Update u = new Update(loc, e, SetAggregator.SETREMOVE_ACTION, agent, null);
		return new UpdateList(u);
	}

	@Override
	public AbstractSetElement getNewInstance(Collection<? extends Element> set) {
		return new SetElement(set);
	}

	@Override
	public boolean containsKey(Element key) {
		return members.contains(key);
	}

	@Override
	public boolean containsValue(Element value) {
		// If the set has any member, it has 'true' as
		// a value. In any case, 'false' is always 
		// among its values
		if (value.equals(BooleanElement.TRUE)) {
			return members.size() > 0;
		} else
			if (value.equals(BooleanElement.FALSE))
				return true;
			else
				return false;
	}

	@Override
	public boolean isEmpty() {
		return members.isEmpty();
	}

	@Override
	public Set<Element> keySet() {
		return Collections.unmodifiableSet(members);
	}

	public int size() {
		return members.size();
	}

	/**
	 * Returns the size of this set in integer.
	 * @return the size of the set
	 */
	public int intSize() {
		return members.size();
	}
	
	@Override
	public Collection<Element> values() {
		if (members.size() > 0)
			return booleanValues;
		else
			return falseValues;
	}

	//----------------------
	// Enumerable Interface
	//----------------------

	public Collection<Element> enumerate() {
		if (enumCache == null)
			enumCache = Collections.unmodifiableSet(this.keySet());
		return enumCache;
	}
	
	public boolean contains(Element e) {
	    return this.keySet().contains(e);
    }

	public List<Element> getIndexedView() throws UnsupportedOperationException {
		if (enumListCache == null)
			enumListCache = Collections.unmodifiableList(new ArrayList<Element>(this.keySet()));
		return enumListCache;
	}

	public boolean supportsIndexedView() {
		return true;
	}

	@Override
	public Map<Element, Element> getMap() {
		if (map == null) {
			map = new HashMap<Element, Element>();
			for (Element e: members) 
				map.put(e, BooleanElement.TRUE);
			map = Collections.unmodifiableMap(map);
		}
		return map;
	}

	@Override
	public Set<Element> getSet() {
		return Collections.unmodifiableSet(members);
	}

}

