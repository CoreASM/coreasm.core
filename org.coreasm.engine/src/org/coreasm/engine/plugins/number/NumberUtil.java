/*	
 * NumberUtil.java  1.0 	$Revision: 243 $
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

import org.coreasm.engine.absstorage.Element;

/** 
 *  NumberUtil
 *	A utility class containing functions for number properties and operations
 *   
 *  @author  George Ma, Roozbeh Farahbod
 *  
 */
public class NumberUtil {
	
	/**
	 * Returns <code>true</code> if the given element
	 * is a {@link NumberElement} and it has a valid 
	 * non-infinite value.
	 */
    public static boolean isReal(Element e) {
        if (e instanceof NumberElement) {
        	final double d = ((NumberElement)e).value;
        	return !Double.isInfinite(d) && !Double.isNaN(d);
        } else
        	return false;
    }
    
    public static boolean isInteger(Element e) {
        boolean ret = false;
        
        if (isReal(e)) {
            NumberElement n = (NumberElement) e;
            
            if (n.value - n.value.longValue() == 0) {
                ret = true;
            }
        }
        
        return ret;
    }
    
    public static boolean isNatural(Element e) {
        boolean ret = false;
        
        if (isInteger(e)) {
            if (((NumberElement) e).value > 0) {
                ret = true;
            }
        }
        
        return ret;
    }
    
    public static boolean isPositive(Element e) {
        boolean ret = false;
        
        if (isReal(e)) {
            if (((NumberElement) e).value > 0) {
                ret = true;
            }
        }
        
        return ret;
    }
    
    public static boolean isNegative(Element e) {
        boolean ret = false;
        
        if (isReal(e)) {
            if (((NumberElement) e).value < 0) {
                ret = true;
            }
        }
        
        return ret;
    }
    
    public static boolean isEven(Element e) {
        boolean ret = false;
        
        if (isInteger(e)) {
            if ((((NumberElement) e).value.longValue() % 2) == 0) {
                ret = true;
            }
        }
        
        return ret;
    }
    
    // There is a problem with the spec.  Odd is not simply the inverse of Even
    public static boolean isOdd(Element e) {
        boolean ret = false;
        
        if (isInteger(e)) {
            if ((((NumberElement) e).value.longValue() % 2) == 1) {
                ret = true;
            }
        }
        
        return ret;
    }
}
