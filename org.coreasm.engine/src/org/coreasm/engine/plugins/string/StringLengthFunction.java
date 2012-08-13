/*	
 * StringLengthFunction.java 	1.0 	$Revision: 243 $
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
 *  Function that gives the length of a string element
 *   
 *  @author  George Ma
 *  
 */
public class StringLengthFunction extends FunctionElement {

    public static String STRING_LENGTH_FUNCTION_NAME = "stringLength";
   
    /**
     * Creates a new StringLengthFunction 
     */
    public StringLengthFunction() {
        setFClass(FunctionClass.fcDerived);
    }
    
    /* (non-Javadoc)
     * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
     */
    @Override
    public Element getValue(List<? extends Element> args) {
        Element ret = Element.UNDEF;

        if (args.size() == 1) {
            if (args.get(0) instanceof StringElement) {
                ret = NumberElement.getInstance(((StringElement) args.get(0)).string.length());
            }
        }
        return ret;
    }

}
