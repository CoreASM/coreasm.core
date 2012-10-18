/*
 * AbstractListElement.java 		$Revision: 239 $
 * 
 * Copyright (c) 2007-2009 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-09 09:57:08 +0100 (Mi, 09 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.collection;

import java.util.Collection;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.plugins.number.NumberElement;

/**
 * The base class for all list elements.
 *   
 * @author Roozbeh Farahbod
 * @see CollectionPlugin
 */

public abstract class AbstractListElement extends AbstractBagElement {

	/**
	 * Creates a new instance of this list loaded with the given
	 * collection of elements. 
	 * 
	 * @param collection a non-null collection of elements
	 */
	public abstract AbstractListElement getNewInstance(Collection<? extends Element> collection);
	

	/**
	 * Gets the element at the given index.
	 * If the element is not a number element, 
	 * it throws an instance of {@link IllegalArgumentException}. 
	 * 
	 * Returns {@link Element#UNDEF} if the index is 
	 * out of range. The index of the first element is 1.
	 * 
	 * @param index an instance of {@link NumberElement}.
	 */
	public abstract Element get(Element index);

	/**
	 * Returns the contents of this list in a java {@link List}
	 * instance. If this is not possible, this method
	 * should throw an instance of
	 *  {@link UnsupportedOperationException}.
	 *  
	 *  @throws UnsupportedOperationException
	 */
	public abstract List<? extends Element> getList();
	
	/**
	 * Returns the first index in which the given 
	 * element exists. It returns <code>null</code>
	 * if this enumerable does not include the given element.
	 * The index of the first element is 1.
	 * 
	 * @param e
	 */
	public abstract NumberElement indexOf(Element e);

	/**
	 * Returns the indexes of all the occurrences of 
	 * element in this enumerable. It returns an empty 
	 * collection if this enumerable
	 * does not include the given element.
	 * The index of the first element is 1.
	 * 
	 * @param e
	 */
	public abstract Collection<NumberElement> indexesOf(Element e);

	/**
	 * If this list contains any element, returns 
	 * the first element of this list. Otherwise,
	 * it returns {@link Element#UNDEF}.
	 */
	public abstract Element head();
	
	/**
	 * If there is more than one element in this list,
	 * this method will return a new list which is the 
	 * tail of this list. 
	 * <p>
	 * Otherwise, it returns an empy list.
	 */
	public abstract AbstractListElement tail();

	/**
	 * If this list contains any element, this method 
	 * returns the last element of this list.
	 */
	public abstract Element last();
	
	/**
	 * Returns a new list element that has the given
	 * element <code>e</code> as its first element, and then 
	 * all the elements of this list in the given order.
	 */
	public abstract AbstractListElement cons(Element e);
	
	
	/**
	 * Concats the specified list to the end of this list.
	 * If the size of the argument list is 0, then this list 
	 * object is returned. Otherwise, a new list is created, 
	 * representing the concatenation of this list and the 
	 * argument list.
	 * 
	 * @param e the list that is concatinated to the end of this list
	 */
	public abstract AbstractListElement concat(AbstractListElement e);
	
}
