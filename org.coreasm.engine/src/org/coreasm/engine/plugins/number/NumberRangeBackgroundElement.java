/*  
 * IntegerRangeBackgroundElement.java    1.0     27-Jun-2006
 * 
 *
 * Copyright (C) 2006 George Ma
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
 *	Class for NumberRange Background Elements
 *   
 *  @author  George Ma
 *  
 */
public class NumberRangeBackgroundElement extends BackgroundElement {

    /**
     * Name of the integer range background
     */
    public static final String NUMBER_RANGE_BACKGROUND_NAME = "NUMBER_RANGE";
    
    public NumberRangeBackgroundElement() {
        super();
    }

    /**
     * Returns a new NumberRangeElement
     * @param start - lower bound of range
     * @param end - upper bound of range
     * @return a new NumberRangeElement with the specified range and step size of 1
     */
    public NumberRangeElement getNewValue(double start, double end) {
        return new NumberRangeElement(start,end);
    }
    
    /**
     * Returns a new NumberRangeElement
     * @param start - lower bound of range
     * @param end - upper bound of range
     * @return a new NumberRangeElement with the specified range and step size
     */
    public NumberRangeElement getNewValue(double start, double end, double step) {
        return new NumberRangeElement(start,end,step);
    }

    /* (non-Javadoc)
     * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(org.coreasm.engine.absstorage.Element)
     */
    @Override
    protected Element getValue(Element e) {
        return (e instanceof NumberRangeElement?BooleanElement.TRUE:BooleanElement.FALSE);
    }

    /* (non-Javadoc)
     * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
     */
    @Override
    public Element getNewValue() {
    	return new NumberRangeElement(0,1);

    	// The following lines were removed by Roozbeh Farahbod, Aug 2006
        // throw new UnsupportedOperationException(
        // 		"New number range element cannot be returned without specifying the bounds of the range.");
    }

}
