/*	
 * NumberRealFunction.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 George Ma
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.compiler.plugins.number.include;

import java.util.List;

import CompilerRuntime.BooleanElement;
import CompilerRuntime.Element;
import CompilerRuntime.FunctionElement;

/** 
 *  Function to determine if an Element represents a real number
 *   
 *  @author  George Ma
 *  
 */
public class NumberRealFunction extends FunctionElement {

    public static String NUMBER_REAL_FUNCTION_NAME = "isRealNumber";
 
    /**
     * Creates a new NumberRealFunction 
     */
    public NumberRealFunction() {
        setFClass(FunctionClass.fcDerived);
    }

    @Override
    public Element getValue(List<? extends Element> args) {
        if (args.size() == 1) {
        	if(args.get(0) instanceof NumberElement){
        		NumberElement tmp = (NumberElement) args.get(0);
        		return BooleanElement.valueOf(!(Double.isInfinite(tmp.getValue()) && Double.isNaN(tmp.getValue())));
        	}
        }
        return BooleanElement.FALSE;
    }
}
