/*	
 * ToNumberFunctionElement.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.compiler.plugins.number.include;

import java.util.List;

import CompilerRuntime.Element;
import CompilerRuntime.FunctionElement;

/** 
 * Provides a Number Element equivalent of elements.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class ToNumberFunctionElement extends FunctionElement {

	/** name of the 'toNumber' function */
	public static final String TONUMBER_FUNC_NAME = "toNumber";
	
	public ToNumberFunctionElement() {
		this.setFClass(FunctionClass.fcDerived);
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 1) {
			String arg = args.get(0).toString();
			Double d = null;
			try {
				d = Double.valueOf(arg);
				return NumberElement.getInstance(d);
			} catch (NumberFormatException e) {
				return Element.UNDEF;
			}
		} else
			return Element.UNDEF;
	}

}
