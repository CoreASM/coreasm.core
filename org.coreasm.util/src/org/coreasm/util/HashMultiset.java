/*	
 * HashMultiset.java 	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** 
 * Implementation of {@link AbstractMultiset} using {@link HashMap}.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class HashMultiset<E> extends AbstractMultiset<E> {

	/**
	 * Creates an empty HashMultiset.
	 */
	public HashMultiset() {
		super();
	}

	/**
	 * Creates a new <code>HashMultiset</code> and adds
	 * the given elements to it.
	 * 
	 * @see AbstractMultiset#AbstractMultiset(Object[])
	 */
	public HashMultiset(E... elements) {
		super(elements);
	}

	/**
	 * Creates a new <code>HashMultiset</code> and adds all the
	 * elements of collection <code>c</code> to it. 
	 * 
	 * @see AbstractMultiset#AbstractMultiset(Collection)
	 */
	public HashMultiset(Collection<? extends E> c) {
		super(c);
	}

	/* (non-Javadoc)
	 * @see org.coreasm.util.AbstractMultiset#createMap()
	 */
	@Override
	protected Map<E, Integer> createMap() {
		return new HashMap<E,Integer>();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.util.AbstractMultiset#toSet()
	 */
	@Override
	public Set<E> toSet() {
		return new HashSet<E>(map.keySet());
	}

}
