/*	
 * Multiset.java 	1.0 	$Revision: 243 $
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
import java.util.Set;


/** 
 * Interface to multisets or bags.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public interface Multiset<E> extends Collection<E> {

	/** 
	 * Returns the multiplicity of an element in the multiset. 
	 * If the element is not in the multiset, returns 0.
	 */
	public int multiplicity(Object element);
	
	/**
	 * Flattens this multiset into a set.
	 */
	public Set<E> toSet();
	
}
