/*
 * AbstractBagElement.java 		$Revision: 80 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.plugins.number.NumberElement;

/**
 * The base class for all bag elements (collections).
 *   
 * @author Roozbeh Farahbod
 * @see CollectionPlugin
 */

public abstract class AbstractBagElement extends AbstractMapElement {

	/**
	 * Creates a new instance of this element loaded with the given
	 * collection of elements. 
	 * 
	 * @param collection a non-null collection of elements
	 */
	public abstract AbstractBagElement getNewInstance(Collection<? extends Element> collection);
	
	/**
	 * Creates a new instance of this element loaded with the given 
	 * map. The map should be of the form of {@link Element} to {@link NumberElement}
	 * in which the numbers are all natural numbers;
	 * otherwise, this method throws {@link IllegalArgumentException}. 
	 * 
	 * @throws IllegalArgumentException if the given map is not of the form Element to NumberElement.
	 */
	public AbstractBagElement getNewInstance(Map<? extends Element, ? extends Element> map) {
		Collection<Element> bag = new ArrayList<Element>();
		
		for (Entry<? extends Element, ? extends Element> e: map.entrySet()) 
			if (e.getValue() instanceof NumberElement
					&& ((NumberElement)e.getValue()).isNatural()) {
				int number = (int)((NumberElement)e.getValue()).getValue();
				for (int i=0; i < number; i++)
					bag.add(e.getKey());
			} else
				throw new IllegalArgumentException("Expecting map of Element to NumberElement with natural numbers.");
		
		return getNewInstance(bag);
	}
	
}
