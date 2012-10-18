/*	
 * TreeMultiset.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** 
 * Implementation of <code>AbstractMultiset</code> using <code>TreeMap</code>s.
 *   
 * @author  Roozbeh Farahbod
 * 
 * @see org.coreasm.util.AbstractMultiset
 * @see java.util.TreeMap
 */
public class TreeMultiset<E> extends AbstractMultiset<E> {

	/**
	 * Creates a new empty <code>TreeMultiset</code>.
	 * 
	 * @see AbstractMultiset#AbstractMultiset()
	 */
	public TreeMultiset() {
		super();
	}
	
	/**
	 * Creates a new <code>TreeMultiset</code> and adds
	 * the given elements to it.
	 * 
	 * @see AbstractMultiset#AbstractMultiset(Object[])
	 */
	public TreeMultiset(E ... elements) {
		super(elements);
	}
	
	/**
	 * Creates a new <code>TreeMultiset</code> and adds all the
	 * elements of collection <code>c</code> to it. 
	 * 
	 * @see AbstractMultiset#AbstractMultiset(Collection)
	 */
	public TreeMultiset(Collection<? extends E> c) {
		super(c);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.util.AbstractMultiset#toSet()
	 */
	@Override
	public Set<E> toSet() {
		return new TreeSet<E>(map.keySet());
	}

	/* (non-Javadoc)
	 * @see org.coreasm.util.AbstractMultiset#createMap()
	 */
	@Override
	protected Map<E,Integer> createMap() {
		return new TreeMap<E,Integer>();
	}

}
