/*	
 * FunctionBackgroundElement.java 	1.0 	$Revision: 243 $
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
 
package CompilerRuntime;


/** 
 *	Class of Function Background Element. There should only be
 *  one instance of this class in each state.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class FunctionBackgroundElement extends BackgroundElement {

	/**
	 * Name of the function background
	 */
	public static final String FUNCTION_BACKGROUND_NAME = "FUNCTION";

	/**
	 * Creates a new Function background.
	 * 
	 * @see #FUNCTION_BACKGROUND_NAME 
	 */
	public FunctionBackgroundElement() {
		super();
	}

	/**
	 * Overriden to prevent creation of any new function.
	 * 
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public Element getNewValue() {
		throw new UnsupportedOperationException("Cannot create new function.");
	}

	/** 
	 * Returns a <code>TRUE</code> boolean for 
	 * Function Elements.
	 * 
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(java.util.List)
	 * @see BooleanElement
	 */
	@Override
	protected BooleanElement getValue(Element e) {
		return (e instanceof FunctionElement)?BooleanElement.TRUE:BooleanElement.FALSE;
	}

}
