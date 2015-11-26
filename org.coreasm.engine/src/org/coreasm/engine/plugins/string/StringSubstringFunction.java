/*	
 * StringSubstringFunction.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 George Ma
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.string;

import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 *  Function that gives the substring, from specified indices, of a string element
 *   
 *  @author  George Ma
 *  
 */
public class StringSubstringFunction extends FunctionElement {

	public static final String STRING_SUBSTRING_FUNCTION_NAME = "substring";
   
    /**
     * Creates a new StringLengthFunction 
     */
    public StringSubstringFunction() {
        setFClass(FunctionClass.fcDerived);
    }
    
    /* (non-Javadoc)
     * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
     */
    @Override
    public Element getValue(List<? extends Element> args) {
        Element ret = Element.UNDEF;

        // if we have the correct number of arguments
        if (args.size() == 3) {
            // if we have the correct type of arguments
            if ((args.get(0) instanceof StringElement) &&
                (args.get(1) instanceof NumberElement) && 
                (args.get(2) instanceof NumberElement)) {
                
                String s = ((StringElement) args.get(0)).string;
                double index1 = ((NumberElement) args.get(1)).getNumber();
                double index2 = ((NumberElement) args.get(2)).getNumber();
                
                // check if the number arguments are integers
                if (((index1 - (int) index1) == 0) && 
                    ((index2 - (int) index2) == 0)) {
                    ret = new StringElement(s.substring((int)index1,(int)index2));
                }                
            }
        }
        
        return ret;
    }

}
