/*
 * AbstractSetElement.java 		$Revision: 80 $
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/**
 * The base class for all set elements.
 *   
 * @author Roozbeh Farahbod
 * @see CollectionPlugin
 */

public abstract class AbstractSetElement extends AbstractBagElement {

	/**
	 * Creates a new instance of this element loaded with the given
	 * set of elements.
	 */
	public abstract AbstractSetElement getNewInstance(Collection<? extends Element> set);
	
	/**
	 * Creates a new instance of this element loaded with the given 
	 * map. The map should be of the form of {@link Element} to {@link BooleanElement}.
	 * Otherwise, this method throws {@link IllegalArgumentException}. 
	 * 
	 * @throws IllegalArgumentException if the given map is not of the form Element to BooleanElement.
	 */
	public AbstractSetElement getNewInstance(Map<? extends Element, ? extends Element> map) {
		Set<Element> set = new HashSet<Element>();
		
		for (Entry<? extends Element, ? extends Element> e: map.entrySet()) 
			if (e.getValue() instanceof BooleanElement) {
				if (((BooleanElement)e.getValue()).getValue())
					set.add(e.getKey());
			} else
				throw new IllegalArgumentException("Expecting map of Element to BooleanElement.");
		
		return getNewInstance(set);
	}
	
	/**
	 * Returns the contents of this set in a java {@link Set}
	 * instance. If this is not possible, this method
	 * should throw an instance of
	 *  {@link UnsupportedOperationException}.
	 *  
	 *  @throws UnsupportedOperationException
	 */
	public abstract Set<? extends Element> getSet();

}
