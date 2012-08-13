/*	
 * SizeFunctionElement.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.number;

import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 * Impelements the 'size' function.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class SizeFunctionElement extends FunctionElement {

	public static final String NAME = "size";
	
	public SizeFunctionElement() {
		setFClass(FunctionClass.fcDerived);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (checkArguments(args)) 
			result = getValue((Enumerable)args.get(0));
		return result;
	}

	/**
	 * Returns the size of the given enumerable as a 
	 * {@link NumberElement}.
	 * 
	 * @param e an {@link Enumerable}
	 */
	public Element getValue(Enumerable e) {
		Element result = Element.UNDEF;
		if (e.size() == Long.MAX_VALUE)
			result = NumberElement.POSITIVE_INFINITY;
		else
			result = NumberElement.getInstance(e.size());
		return result;
	}
	
	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 1) 
				&& (args.get(0) instanceof Enumerable);
	}

}
