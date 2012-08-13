/*	
 * BackgroundElement.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.absstorage;

/**
 * An abstract class that implements the BACKGROUND Element.
 * 
 * @author Roozbeh Farahbod
 * 
 */
public abstract class BackgroundElement extends AbstractUniverse {

	/**
	 * Creates a new background element. The function class of this
	 * background is set to <code>FunctionClass.fcStatic</code>.
	 * 
	 * @see AbstractUniverse#AbstractUniverse()
	 * @see FunctionElement.FunctionClass
	 */
	public BackgroundElement() {
		super.setFClass(FunctionClass.fcStatic);
	}

	/**
	 * Overrides the <code>setFClass</code> function to prevent changing the
	 * function class of Backgrounds.
	 * 
	 * @see org.coreasm.engine.absstorage.FunctionElement#setFClass(org.coreasm.engine.absstorage.FunctionElement.FunctionClass)
	 * @throws UnsupportedOperationException
	 *             always.
	 */
	@Override
	public final void setFClass(FunctionClass fClass) {
		throw new UnsupportedOperationException(
				"Function class of backgrounds cannot be changed.");
	}

	/**
	 * Returns a possibly virtual new value of this background.
	 * 
	 * @return and Element as a new value from this background
	 */
	public abstract Element getNewValue();

	/**
	 * If this method is supported by this background,
	 * it returns an element of this background that 
	 * is represented by the given string value. 
	 * 
	 * @param denotation the string representation of the element
	 * @return an element of this background that is represented by the given string value
	 * 
	 * @throws ElementFormatException if the conversion fails
	 * @throws UnsupportedOperationException if this operation is not supported by this background 
	 */
	public Element valueOf(String denotation) throws ElementFormatException {
		throw new UnsupportedOperationException("This background cannot parse values from a String representation.");
	}
	
	/*
	 * Returns the suggested name of this background
	 * which must be the name under which this background
	 * is registered in the state.
	 *
	public abstract String getBackgroundName();
	*/
}
