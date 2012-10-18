/*	
 * UpdateMultiset.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import java.util.Collection;

import org.coreasm.util.HashMultiset;

/** 
 * Provides a multiset of updates. This class extends the <code>HashMultiset</code> class
 * and specializes it to a multiset of updates. 
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class UpdateMultiset extends HashMultiset<Update> {

	/**
	 * Creates a new empty update multiset.
	 * 
	 * @see HashMultiset#HashMultiset()
	 */
	public UpdateMultiset() {
		super();
	}

	/**
	 * Creates a new update multiset with the given updates.
	 * 
	 * @see HashMultiset#HashMultiset(Collection)
	 */
	public UpdateMultiset(Collection<? extends Update> c) {
		super(c);
	}

	/**
	 * Creates a new update multiset with the given updates.
	 * 
	 * @see HashMultiset#HashMultiset(Object[])
	 */
	public UpdateMultiset(Update... elements) {
		super(elements);
	}

}
