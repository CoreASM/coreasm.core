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

package org.coreasm.engine.plugins.collection;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.number.NumberElement;

/**
 * The interface for all modifiable indexed collections.
 *   
 * @author Roozbeh Farahbod
 * @see CollectionPlugin
 */

public interface ModifiableIndexedCollection extends ModifiableCollection {

	/**
	 * Computes an updateset that would add a new element to this enumerable
	 * at the given index. The index of the first element is 1.
	 * 
	 * @param loc location of the update
	 * @param index 
	 * @param e the new element
	 * @param agent the contributing agent
	 * @param node the contributing parse-tree nodes
	 * 
	 * @throws IndexOutOfBoundsException if the index is out of bound.
	 * @throws InterpreterException if there is any problem with computing this update
	 */
	public abstract UpdateMultiset computeAddUpdate(Location loc, NumberElement index, Element e, Element agent, Node node) throws InterpreterException;


	/**
	 * Computes an update set that would remove the element at the given index 
	 * from this enumerable. The index of the first element is 1.
	 * 
	 * @param loc location of the update
	 * @param index
	 * @param agent the contributing agent
	 * @param node the contributing parse-tree nodes
	 * 
	 * @throws IndexOutOfBoundsException if the index is out of bound.
	 * @throws InterpreterException if there is any problem with computing this update
	 */
	public abstract UpdateMultiset computeRemoveUpdate(Location loc, NumberElement index, Element agent, Node node) throws InterpreterException;

}
