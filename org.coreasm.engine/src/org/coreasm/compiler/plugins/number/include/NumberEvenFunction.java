/*	
 * NumberEvenFunction.java 	1.0 	$Revision: 243 $
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
 *	Function to determine if an Element represents an even number
 *   
 *  @author  George Ma
 *  
 */
public class NumberEvenFunction extends FunctionElement {

    public static String NUMBER_EVEN_FUNCTION_NAME = "isEvenNumber";
    
    /**
     * Create a new instance of NumberEvenFunction
     */
    public NumberEvenFunction() {
        setFClass(FunctionClass.fcDerived);
    }
    
    /* (non-Javadoc)
     * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
     */
    @Override
    public Element getValue(List<? extends Element> args) {
        if (args.size() == 1) {
        	if(args.get(0) instanceof NumberElement){
        		NumberElement tmp = (NumberElement) args.get(0);
        		if(tmp.getValue() % 2 == 0) return BooleanElement.TRUE;
        	}
        }
        return BooleanElement.FALSE;
    }

}