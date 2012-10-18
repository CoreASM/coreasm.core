/*	
 * StringLengthFunctionElement.java 	1.0 	$Revision: 243 $
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
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 * Computes the length of a string element.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class StringLengthFunctionElement extends FunctionElement {

	/** name of the 'strlen' function */
	public static final String STRLENGTH_FUNC_NAME = "strlen";
	
	/**
	 * Creates a derived function.
	 */
	public StringLengthFunctionElement() {
		this.setFClass(FunctionClass.fcDerived);
	}

	/**
	 * @return the length of the string element, if the argument is a list of one 
	 * string element; otherwise, returns <code>Element.UNDEF</code>.
	 * 
	 * @see Element#UNDEF
	 * @see StringElement
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 1) {
			Element str = args.get(0);
			if (str instanceof StringElement)
				return NumberElement.getInstance(((StringElement)str).toString().length());
		}
		
		return Element.UNDEF;
	}

}
