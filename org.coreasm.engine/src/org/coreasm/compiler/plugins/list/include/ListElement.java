/*	
 * ListElement.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.compiler.plugins.list.include;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coreasm.engine.plugins.list.ListBackgroundElement;
import org.coreasm.engine.plugins.number.NumberElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Location;

import CompilerRuntime.Rule;

import org.coreasm.engine.absstorage.Update;

import CompilerRuntime.UpdateList;

import org.coreasm.engine.plugins.collection.AbstractListElement;
import org.coreasm.compiler.plugins.collection.include.ModifiableIndexedCollection;

/** 
 * This class implements list elements in CoreASM.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ListElement extends AbstractListElement implements ModifiableIndexedCollection {

	private List<Element> listElements;
	private List<Element> enumerationCache = null;
	
	public ListElement() {
		listElements = Collections.emptyList();
	}
	
	public ListElement(Collection<? extends Element> collection) {
		if (collection.size() == 0)
			listElements = Collections.emptyList();
		else
			listElements = Collections.unmodifiableList(new ArrayList<Element>(collection));
	}
	
	public ListElement(ListElement list) {
		this(list.listElements);
	}
	
	public ListElement(Element ... elements) {
		if (elements.length == 0)
			listElements = Collections.emptyList();
		else {
			listElements = new ArrayList<Element>();
			for (Element e: elements) 
				listElements.add(e);
			listElements = Collections.unmodifiableList(listElements);
		}
	}
	
	/**
	 * Creates a new list which is <i>cons(e, list)</i>.
	 */
	public ListElement(Element e, ListElement list) {
		listElements = new ArrayList<Element>(list.listElements);
		listElements.add(0, e);
		listElements = Collections.unmodifiableList(listElements);
	}
	
	@Override
	public boolean equals(Object anElement) {
		if (anElement instanceof ListElement) {
			if (super.equals(anElement))
				return true;
			else {
				List<Element> otherList = (List<Element>)((ListElement)anElement).enumerate();
				if (otherList.size() == listElements.size()) {
					for (int i=0; i < listElements.size(); i++)
						if (!otherList.get(i).equals(listElements.get(i))) {
							return false;
						}
					return true;
				}
			}
		}
		
		return false;
	}

	public String getBackground() {
		return ListBackgroundElement.LIST_BACKGROUND_NAME;
	}
	
	/**
	 * Returns the contents of this list as a java List 
	 * object. The returned list is unmodifiable.
	 * 
	 * @see AbstractListElement#getList()
	 */
	public List<? extends Element> getList() {
		return Collections.unmodifiableList(listElements);
	}
	
	@Override
	public String denotation() {
		String str = "[";
		
		// for all members of this list
		for (Element m : listElements)
		{
			// if not first element, add comma to beginning
			if (str.length() > 1)
				str = str + ", ";
			// add string representation of element to set.
			str = str + m.denotation();
		}
		str = str + "]";
				
		return str;
	}

	@Override
	public String toString() {
		String str = "[";
		
		// for all members of this list
		for (Element m : listElements)
		{
			// if not first element, add comma to beginning
			if (str.length() > 1)
				str = str + ", ";
			// add string representation of element to set.
			str = str + m.toString();
		}
		str = str + "]";
				
		return str;
	}

	@Override
	public int hashCode() {
		int result = 0;
		for (Element e: listElements) 
		    result = 31*result + (e==null ? 0 : e.hashCode());
		return result;
	}

	public boolean contains(Element e) {
		return listElements.contains(e);
	}

	public Collection<Element> enumerate() {
		return getIndexedView();
	}
	
	/*
	 * Returns true if the given number is natural and its 
	 * value is a java integer.
	 */
	protected boolean isValidIndex(NumberElement index) {
		return index.isNatural() && index.getValue() < Integer.MAX_VALUE;
	}
	
	/**
	 * Returns the size of this list as
	 * an integer. The value of this integer 
	 * is equal to the value of the number
	 * element returned by {@link #size()}.
	 */
	public int intSize() {
		return listElements.size();
	}
	
	@Override
	public UpdateList computeAddUpdate(Location loc, NumberElement index, Element e, Rule agent) {
		if (isValidIndex(index) && index.getValue() <= this.intSize()+1) {
			List<Element> newListData = new ArrayList<Element>(this.listElements);
			newListData.add(index.intValue() - 1, e);
			ListElement newList = new ListElement(newListData);
			Update u = new Update(loc, newList, Update.UPDATE_ACTION, agent, null);
			return new UpdateList(u);
		} else
			throw new IndexOutOfBoundsException("Index is invalid or out of bound. (index = " + index + ")");
	}

	@Override
	public UpdateList computeRemoveUpdate(Location loc, NumberElement index, Rule agent) {
		if (isValidIndex(index) && index.getValue() <= this.intSize()) {
			List<Element> newListData = new ArrayList<Element>(this.listElements);
			newListData.remove(index.intValue() - 1);
			ListElement newList = new ListElement(newListData);
			Update u = new Update(loc, newList, Update.UPDATE_ACTION, agent, null);
			return new UpdateList(u);
		} else
			throw new IndexOutOfBoundsException("Index is invalid or out of bound. (index = " + index + ")");
	}

	@Override
	public Element get(Element index) {
		if (index instanceof NumberElement 
				&& isValidIndex((NumberElement)index)
				&& ((NumberElement)index).getValue() <= this.intSize()) {
			Element result = null;
			int i = ((NumberElement)index).intValue();
			// note: in CoreASM list indices start from 1
			result = listElements.get(i - 1);
			if (result == null) 
				result = defaultValue;
			return result;
		} else
			return Element.UNDEF;
			//throw new IndexOutOfBoundsException("Index is invalid or out of bound. (index = " + index + ")");
			
	}

	/**
	 * @see #get(Element)
	 */
	public Element get(int index) {
		return get(NumberElement.getInstance(index));
	}
	
	@Override
	public NumberElement indexOf(Element e) {
		// note: in CoreASM list indices start from 1
		final int i = listElements.indexOf(e);
		if (i >=0) 
			return NumberElement.getInstance(i+1);
		else
			return null;
	}

	@Override
	public Collection<NumberElement> indexesOf(Element e) {
		List<NumberElement> result = new ArrayList<NumberElement>();
		
		int i = 1;
		for (Element ith: listElements) {
			if (ith.equals(e))
				result.add(NumberElement.getInstance(i));
			i++;
		}
		
		return result;
	}
	
	@Override
	public UpdateList computeAddUpdate(Location loc, Element e, Rule agent) {
		return computeAddUpdate(loc, NumberElement.getInstance(this.intSize() + 1), e, agent);
	}

	@Override
	public UpdateList computeRemoveUpdate(Location loc, Element e, Rule agent) {
		List<Element> newListData = new ArrayList<Element>(listElements);
		if (newListData.remove(e)) {
			ListElement newList = new ListElement(newListData);
			Update u = new Update(loc, newList, Update.UPDATE_ACTION, agent, null);
			return new UpdateList(u);
		} else
			return new UpdateList();
	}

	@Override
	public boolean containsKey(Element key) {
		return (key instanceof NumberElement)
				&& (isValidIndex((NumberElement)key))
				&& (((NumberElement)key).getValue() <= this.intSize());
	}

	@Override
	public boolean containsValue(Element value) {
		return listElements.contains(value);
	}

	@Override
	public boolean isEmpty() {
		return listElements.isEmpty();
	}

	@Override
	public Set<Element> keySet() {
		HashSet<Element> result = new HashSet<Element>(); 
		for (int i=0; i < listElements.size(); i++)
			// note: in CoreASM list indices start from 1
			result.add(NumberElement.getInstance(i + 1));
		return result;
	}

	public int size() {
		return listElements.size();
	}

	@Override
	public Collection<Element> values() {
		return listElements;
	}
	
	public Set<Location> getLocations(String name) {
		Set<Location> locSet = new HashSet<Location>();
		Set<ElementList> argSet = new HashSet<ElementList>();
		for (Element value: keySet())
			argSet.add(ElementList.create(value));
		Location loc = null;
		for (ElementList l : argSet) {
			if (!getValue(l).equals(defaultValue)) {
				loc = new Location(name, l);
				locSet.add(loc);
			}
		}
		return locSet;
	}

	@Override
	public AbstractListElement getNewInstance(Collection<? extends Element> bag) {
		return new ListElement(bag);
	}

	@Override
	public Element head() {
		if (intSize() > 0) 
			return listElements.get(0);
		else
			return Element.UNDEF;
	}

	@Override
	public Element last() {
		if (intSize() > 0) 
			return listElements.get(listElements.size()-1);
		else
			return Element.UNDEF;
	}

	@Override
	public AbstractListElement tail() {
		if (this.intSize() < 2)
			return new ListElement();
		else {
			return new ListElement(new ArrayList<Element>(listElements.subList(1, listElements.size())));
		}
			
	}

	@Override
	public AbstractListElement cons(Element e) {
		return new ListElement(e, this);
	}
	
	@Override
	public AbstractListElement concat(AbstractListElement e) {
		if (e.size() == 0)
			return this;
		else {
			List<Element> list = new ArrayList<Element>(this.getList());
			list.addAll(e.enumerate());
			return new ListElement(list);
		}
	}

	/**
	 * This operation is not supported in ListElement.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public Map<Element, Element> getMap() {
		throw new UnsupportedOperationException("ListElement does not provide a Java Map view of its elements.");
		// TODO it doesn't hurt to implement it later.
	}

	public List<Element> getIndexedView() throws UnsupportedOperationException {
		if (enumerationCache == null) 
			enumerationCache = Collections.unmodifiableList(listElements);
		return enumerationCache;
	}

	public boolean supportsIndexedView() {
		return true;
	}

}
