/*	
 * SetBackgroundElement.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Mashaal Memon
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.set;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/**
 * An abstract class that implements the SET background Element.
 * 
 * @author Mashaal Memon
 * 
 */
public class SetBackgroundElement extends BackgroundElement {

	/**
	 * Name of the set background
	 */
	public static final String SET_BACKGROUND_NAME = "SET";
	
	/**
	 * Creates a new Set background.
	 * 
	 * @see #SET_BACKGROUND_NAME 
	 */
	public SetBackgroundElement() {
		super();
	}
	
	/**
	 * Returns a new set element with no set members.
	 * 
	 * @return <code>Element<code> which is actually an empty set in the simulated machine.
	 * 
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 */
	@Override
	public Element getNewValue() {
		return new SetElement();
	}

	/** 
	 * Returns a <code>TRUE</code> boolean for 
	 * Set Elements. Otherwise <code>FALSE<code> is returned.
	 * 
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(Element)
	 * @see BooleanElement
	 */
	@Override
	protected BooleanElement getValue(Element e) {
		return (e instanceof SetElement)?BooleanElement.TRUE:BooleanElement.FALSE;
	}

}
