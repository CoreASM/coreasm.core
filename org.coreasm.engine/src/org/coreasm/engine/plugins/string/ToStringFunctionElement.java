/*	
 * ToStringFunctionElement.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.string;

import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;

/** 
 * Provides a String Element equivalent of elements.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class ToStringFunctionElement extends FunctionElement {

	/** name of the 'toString' function */
	public static final String TOSTRING_FUNC_NAME = "toString";
	
	public ToStringFunctionElement() {
		this.setFClass(FunctionClass.fcDerived);
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 1) {
			return new StringElement(args.get(0).toString());
		} else
			return Element.UNDEF;
	}

}
