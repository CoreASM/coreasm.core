/*	
 * NumberElement.java 	1.0 	$Revision: 253 $
 * 
 * Copyright (C) 2006 Mashaal Memon
 * Copyright (c) 2007-2011 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.compiler.plugins.number.include;

import CompilerRuntime.Element;

/** 
 *	This represents a numeric element. 
 *   
 *  @author  Mashaal Memon, Roozbeh Farahbod
 *  
 */
public class NumberElement extends Element implements Comparable<NumberElement> {

	public static final NumberElement POSITIVE_INFINITY = new NumberElement(Double.POSITIVE_INFINITY);
	public static final NumberElement NEGATIVE_INFINITY = new NumberElement(Double.NEGATIVE_INFINITY);
	public static final NumberElement MAX_VALUE = new NumberElement(Double.MAX_VALUE);
	public static final NumberElement MIN_VALUE = new NumberElement(Double.MIN_VALUE);
	
	protected final Double value;
	protected final boolean isInteger;
	
	/*
	 * Instantiate this number element with double
	 * 
	 * @param number is an <code>double</code> number.
	 */
	protected NumberElement(double number)
	{
		this.value = new Double(number);
		this.isInteger = (this.value - this.value.longValue() == 0);
	}
	
	/**
	 * Creates a new NumberElement instance with the given double value.
	 * 
	 * @param d double value
	 */
	public synchronized static NumberElement getInstance(double d) {
		return new NumberElement(d);
	}

	public String getBackground() { 
		return NumberBackgroundElement.NUMBER_BACKGROUND_NAME;
	}
	
	/**
	 * Returns a string representation 
	 * of the numeric value of this element.
	 */
	@Override
	public String denotation() {
		return value.toString();
	}
	
	/**
	 * Returns a <code>String</code> representation of 
	 * this Number Element.
	 */
	@Override
	public String toString() {
		if (isInteger)
			return String.valueOf(longValue());
		else
			return value.toString();
	}
	
	/**
	 * Returns <code>true</code> if this number element
	 * has a valid non-infinite value.
	 */
    public boolean isReal() {
     	return !Double.isInfinite(value) && !Double.isNaN(value);
    }
    
	/**
	 * Returns <code>true</code> if this number element
	 * represents a java long value.
	 */
	public boolean isInteger() {
		return (value - value.longValue() == 0);
	}

	/**
	 * Returns <code>true</code> if this number element
	 * represents an natural value; i.e., a non-zero 
	 * positive java long value.
	 */
	public boolean isNatural() {
		return isInteger() && value > 0;
	}
	
	/**
	 * Returns the value of this element
	 * as a double.
	 */
	public double doubleValue() {
		return this.getValue();
	}

	/**
	 * Returns the value of this element
	 * as a long.
	 * 
	 * @see Double#longValue()
	 */
	public long longValue() {
		return value.longValue();
	}
	
	/**
	 * Returns the value of this element
	 * as an int.
	 * 
	 * @see Double#intValue()
	 */
	public int intValue() {
		return value.intValue();
	}
	
	//----------------------
	// Equality interface
	//----------------------
	
	/**
 	 * Compares this Element to the specified Element. 
 	 * The result is <code>true</code> if the argument 
 	 * is not null and is considered to be equal to this Element.
 	 * 
 	 * @param anElement the Element to compare with.
 	 * @return <code>true</code> if the Elements are equal; <code>false</code> otherwise.
 	 * @throws IllegalArgumentException if <code>anElement</code> is not an instance
 	 * of <code>Element</code>
 	 */
 	public synchronized boolean equals(Object anElement) {
 		// if both java objects are idential, no further checks are required
 		if (this == anElement)
 			return true;
 		// else both java objects are not identical, have to check that
 		// both are number elements, and both have the same numerical value
 		else
 		{
	 		// both number elements
	 		if (anElement instanceof NumberElement)
	 		{
	 			// if the current number and the other number equal each other
	 			// then objects are equal
	 			return value.equals(((NumberElement)anElement).value);
	 		} else
	 			return false;
 		}
	}
 	
 	/**
	 * Hashcode for Number elements. Must be overridden because equality is overridden. 
	 *  
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return value.hashCode(); 
	}

	/**
	 * Compares this number with the given number. 
	 * 
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(NumberElement n) {
		return value.compareTo(n.value);
	}

    /**
     * Returns the double value of this NumberElement
     * 
     */
    public double getNumber() {
        return value.doubleValue();
    }

    /**
     * Returns the double value of this NumberElement
     * 
     */
    public double getValue() {
        return getNumber();
    }
    
}
