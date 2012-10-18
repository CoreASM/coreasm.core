/*	
 * NumberBackgroundElement.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Mashaal Memon
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.number;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/** 
 *	Background of numbers.
 *   
 *  @author  Mashaal Memon
 *  
 */
public class NumberBackgroundElement extends BackgroundElement {

	/**
	 * Name of the Number background
	 */
	public static final String NUMBER_BACKGROUND_NAME = "NUMBER";
	
	/**
	 * Creates a new Number background.
	 * 
	 * @see #NUMBER_BACKGROUND_NAME 
	 */
	public NumberBackgroundElement() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 */
	@Override
	public Element getNewValue() {
		return NumberElement.getInstance(0);
		
		// The following lines were removed by Roozbeh Farahbod, Aug 2006
//		throw new UnsupportedOperationException(
//			"New number element cannot be returned without specifying the number that the element should represent.");
	}
	
	/**
	 * Return new number element representing specified number.
	 * 
	 * @param number A <code>double</code> representing number element requested.
	 * 
	 * @return a <code>NumberElement</code> representing specified number.
	 */
	public NumberElement getNewValue(double number) {
		return NumberElement.getInstance(number);
	}
	
	/**
	 * Return new number element representing specified number.
	 * 
	 * @param number A <code>int</code> representing number element requested.
	 * 
	 * @return a <code>NumberElement</code> representing specified number.
	 */
	public NumberElement getNewValue(int number) {
		return NumberElement.getInstance(number);
	}

	/** 
	 * Returns a <code>TRUE</code> boolean for 
	 * Number Elements. Otherwise <code>FALSE<code> is returned.
	 * 
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(Element)
	 * @see BooleanElement
	 */
	@Override
	protected BooleanElement getValue(Element e) {
		return (e instanceof NumberElement)?BooleanElement.TRUE:BooleanElement.FALSE;
	}

}
