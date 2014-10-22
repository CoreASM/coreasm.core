/*	
 * BooleanElement.java 	1.0 	$Revision: 243 $
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
 
package CompilerRuntime;

/** 
 *	This implements the Boolean Element.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class BooleanElement extends Element {

  	/**
 	 * Name of the 'true' value.
 	 */
 	public static final String TRUE_NAME = "true";

 	/**
 	 * Name of the 'false' value.
 	 */
 	public static final String FALSE_NAME = "false";

 	/**
 	 * Holds the value of this Boolean Element as a 
 	 * Java boolean value 
 	 */
 	private Boolean value;
 	
 	/**
 	 * Represents the 'true' value in ASM.
 	 */
 	public static final BooleanElement TRUE = new BooleanElement(true);
 	
 	/**
 	 * Represents the 'flase' value in ASM.
 	 */
 	public static final BooleanElement FALSE = new BooleanElement(false);
 	
	/**
	 * Returns a Boolean Element of the given boolean value
	 */
	public static BooleanElement valueOf(boolean value) {
		return value?TRUE:FALSE;
	}

	/**
	 * A private constructor to creates a new Boolean value.
	 * 
	 * @param value the boolean value of this Element
	 */
	private BooleanElement(boolean value) {
		super();
		this.value = value;
	}

	public String getBackground() {
		return BooleanBackgroundElement.BOOLEAN_BACKGROUND_NAME;
	}
	
	/**
	 * Returns the Java boolean value of this 
	 * Boolean Element 
	 */
	public boolean getValue() {
		return value;
	}
	
	/**
	 * Returns a <code>String</code> representation of 
	 * this Boolean Element.
	 * 
	 * @see org.coreasm.engine.absstorage.Element#toString()
	 */
	@Override
	public String toString() {
		return value?TRUE_NAME:FALSE_NAME;
	}

	@Override
	public boolean equals(Object anElement) {
		if (super.equals(anElement))
			return true;
		else 
			if (anElement instanceof BooleanElement) {
				return this.value == ((BooleanElement)anElement).value;
			} else
				return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
}
