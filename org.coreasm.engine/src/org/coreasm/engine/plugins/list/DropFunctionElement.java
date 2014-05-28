/*	
 * DropFunctionElement.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.list;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.plugins.collection.AbstractListElement;
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 * Implementation of the 'drop(list, int)' function.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class DropFunctionElement extends TakeFunctionElement {

	public static final String NAME = "drop";
	
	public DropFunctionElement(ControlAPI capi) {
		super(capi);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!checkArguments(args))
			throw new CoreASMError("Illegal arguments for " + NAME + ".");
		
		AbstractListElement list = (AbstractListElement)args.get(0);
		NumberElement n = (NumberElement)args.get(1);
		List<Element> resultValues = new ArrayList<Element>();

		int i = (int)n.getValue() + 1;
		while (i <= list.size()) {
			resultValues.add(list.get(NumberElement.getInstance(i)));
			i++;
		}
		
		return new ListElement(resultValues);
	}

}
