/*	
 * FunctionRangeFunctionElement.java 	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.signature;

import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.set.SetElement;

/** 
 * The 'range(f)' function that returns the range of a function
 * in form of a set of elements.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class FunctionRangeFunctionElement extends FunctionElement {

	/**
	 * Suggested name of this function
	 */
	public static final String FUNCTION_NAME = "range";
	
//	private ControlAPI capi;
	
	public FunctionRangeFunctionElement() {
		setFClass(FunctionClass.fcDerived);
//		this.capi = capi;
	}
	
	/**
	 * If the args is a list of only one function element, this method
	 * returns a {@link SetElement set} of the values of the function element. Otherwise, returns
	 * {@link Element#UNDEF undef}.
	 * 
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 * @see SetElement
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 1) {
			Element e = args.get(0);
			if (e instanceof FunctionElement) {
				// Here it would be better if we used the SetBackground
				// to get a new value, but it doesn't matter for sets.
				Set<? extends Element> elements = ((FunctionElement)e).getRange();
				return new SetElement(elements);
			}
		} 
		return Element.UNDEF;
	}

}
