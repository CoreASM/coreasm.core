/*	
 * NumberOddFunction.java 	1.0 	$Revision: 243 $
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

import java.util.List;

import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;

/** 
 *  Function to determine if an Element represents an odd number
 *   
 *  @author  George Ma
 *  
 */public class NumberOddFunction extends FunctionElement {

    public static String NUMBER_ODD_FUNCTION_NAME = "isOddNumber";
    
    /**
     * Creates a new NumberOddFunction 
     */
    public NumberOddFunction() {
        setFClass(FunctionClass.fcDerived);
    }
    
    /* (non-Javadoc)
     * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
     */
    @Override
    public Element getValue(List<? extends Element> args) {
        Element ret = BooleanElement.FALSE;

        if (args.size() == 1) {
            if (NumberUtil.isOdd(args.get(0))) {
                ret = BooleanElement.TRUE;
            }
        }
        return ret;
    }

}
