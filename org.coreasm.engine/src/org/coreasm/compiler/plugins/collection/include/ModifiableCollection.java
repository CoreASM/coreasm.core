/**
 * ModifiableCollection.java 		$Revision: 140 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-04-24 01:06:58 +0200 (Sa, 24 Apr 2010) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.compiler.plugins.collection.include;

import CompilerRuntime.CoreASMCException;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import CompilerRuntime.Rule;
import CompilerRuntime.UpdateList;

/**
 * The interface for all modifiable collections.
 *   
 * @author Roozbeh Farahbod
 * @see CollectionPlugin
 */

public interface ModifiableCollection {

	/**
	 * Computes an update-set that would add a new element to this collection.
	 * 
	 * @param loc the location of the update
	 * @param e the new element
	 * @param agent the contributing agent
	 * 
	 * @throws CoreASMCException if there is a problem with evaluating the update set
	 */
	public abstract UpdateList computeAddUpdate(Location loc, Element e, Rule agent) throws CoreASMCException;

	/**
	 * Computes an update-set that would remove the given element from this collection.
	 * 
	 * @param loc the location of the update
	 * @param e the element to be removed
	 * @param agent the contributing agent  
	 * 
	 * @throws CoreASMCException if there is a problem with evaluating the update set
	 */
	public abstract UpdateList computeRemoveUpdate(Location loc, Element e, Rule agent) throws CoreASMCException;

}
