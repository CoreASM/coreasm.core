/**
 * ModifiableIndexedCollection.java 		$Revision: 140 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-04-23 16:06:58 -0700 (Fri, 23 Apr 2010) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.compiler.plugins.collection.include;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.plugins.number.NumberElement;

import CompilerRuntime.CoreASMCException;
import CompilerRuntime.Rule;
import CompilerRuntime.UpdateList;


/**
 * The interface for all modifiable indexed collections.
 *   
 * @author Roozbeh Farahbod
 */

public interface ModifiableIndexedCollection extends ModifiableCollection {
	/**
	 * Computes a new add operation
	 * @param loc The location for the operation
	 * @param index The index in the collection
	 * @param e The element to be added
	 * @param agent The responsible agent
	 * @return An update list containing the generated update
	 * @throws CoreASMCException If an error occured
	 */
	public abstract UpdateList computeAddUpdate(Location loc, NumberElement index, Element e, Rule agent) throws CoreASMCException;

	/**
	 * Computes a new remove operation
	 * @param loc The location for the operation
	 * @param index The index in the collection
	 * @param agent The responsible agent
	 * @return An update list containing the generated update
	 * @throws CoreASMCException If an error occured
	 */
	public abstract UpdateList computeRemoveUpdate(Location loc, NumberElement index, Rule agent) throws CoreASMCException;

}
