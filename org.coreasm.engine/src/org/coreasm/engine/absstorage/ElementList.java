/*	
 * ElementList.java 	1.1 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** 
 *	A helper class that implements an umodifiable list of Elements.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
@SuppressWarnings("serial")
public class ElementList extends ArrayList<Element> {

	/** An empty element list to be used as an empty argument list */
	public static final ElementList NO_ARGUMENT = new ElementList();

	/**
	 * Creates an empty list of Elements.
	 * 
	 * @see ArrayList#ArrayList()
	 */
	public ElementList() {
		super();
	}

	/**
	 * Creates a new argument list based on the given
	 * Element collection.
	 *  
	 * @param c collection of Elements
	 * @see ArrayList#addAll(int, java.util.Collection)
	 */
	public ElementList(Collection<? extends Element> c) {
		super(c);
	}

	/**
	 * Creates a new argument list of the given 
	 * Elements.
	 * 
	 * @param args Elements
	 */
	public ElementList(Element ... args) {
		super();
		for (int i=0; i < args.length; i++) {
			super.add(args[i]);
		}
		
	}

	/** 
	 * An static method to create a list of elements.
	 * 
	 * @param args elements 
	 * @return a new <code>ElementList</code> containing the given elements.
	 */
	public static ElementList create(Element ... args) {
		if (args.length == 0)
			return NO_ARGUMENT;
		else
			return new ElementList(args);
	}
	
	/** 
	 * An static method to create a list of elements.
	 * 
	 * @param c collection of Elements
	 * @return a new <code>ElementList</code> containing the given elements.
	 */
	public static ElementList create(Collection<? extends Element> c) {
		if (c.size() == 0)
			return NO_ARGUMENT;
		else
			return new ElementList(c);
	}
	
	/**
	 * An element list is equivalent to any List if both have the
	 * same size and all the elements, in the same order, are equal.
	 * 
	 * @see java.util.AbstractList#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof List) {
			List list = (List)obj;
			result = list.size() == this.size();
			if (result && this.size() > 0) {
				for (int i = 0; i < this.size(); i++)
					if (!this.get(i).equals(list.get(i))) {
						result = false;
						break;
					}
			}
		}
		return result;
	}
	
	public String toString() {
		String str = "[";
		
		// for all members of this list
		for (Element m : this)
		{
			// if not first element, add comma to beginning
			if (str.length() > 1)
				str = str + ", ";
			// add string representation of element to set.
			str = str + m.denotation();
		}
		return str + "]";
	}

	@Override
	public boolean add(Element o) {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	public void add(int index, Element element) {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	public boolean addAll(Collection<? extends Element> c) {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	public boolean addAll(int index, Collection<? extends Element> c) {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	public Element remove(int index) {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	public Element set(int index, Element element) {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	public void trimToSize() {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("ElementList cannot be modified.");
	}
	
	
}
