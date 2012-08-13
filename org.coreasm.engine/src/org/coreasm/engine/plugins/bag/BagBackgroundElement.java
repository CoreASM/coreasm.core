/*	
 * BagBackgroundElement.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.bag;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/** 
 * Background of bags.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class BagBackgroundElement extends BackgroundElement {

	/**
	 * Name of the bag background
	 */
	public static final String BAG_BACKGROUND_NAME = "BAG";
	
	/**
	 * Creates a new Bag background.
	 * 
	 * @see #BAG_BACKGROUND_NAME 
	 */
	public BagBackgroundElement() {
		super();
	}
	
	/**
	 * Returns a new Bag element with no members.
	 * 
	 * @return <code>Element<code> which is actually an empty bag in the simulated machine.
	 * 
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 */
	@Override
	public Element getNewValue() {
		return new BagElement();
	}

	/** 
	 * Returns a <code>TRUE</code> boolean for 
	 * Bag Elements. Otherwise <code>FALSE<code> is returned.
	 * 
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(Element)
	 * @see BooleanElement
	 */
	@Override
	protected BooleanElement getValue(Element e) {
		return (e instanceof BagElement)?BooleanElement.TRUE:BooleanElement.FALSE;
	}

}
