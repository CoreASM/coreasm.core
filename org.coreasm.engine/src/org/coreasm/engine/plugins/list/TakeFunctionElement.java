/*	
 * TakeFunctionElement.java  	$Revision: 243 $
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
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.plugins.collection.AbstractListElement;
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 * Implementation of the 'take(list, int)' function.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class TakeFunctionElement extends NthFunctionElement {

	public static final String NAME = "take";
	protected final ControlAPI capi;
	protected final AbstractStorage storage;
	
	public TakeFunctionElement(ControlAPI capi) {
		this.capi = capi;
		this.storage = capi.getStorage();
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
		List<Element> newValues = new ArrayList<Element>();
		
		int i = 1;
		while (i <= (int)n.getValue()) {
			if (i > list.size())
				break;
			newValues.add(list.get(NumberElement.getInstance(i)));
			i++;
		}
		
		return new ListElement(newValues);
	}

	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 2) 
				&& (args.get(0) instanceof AbstractListElement)
				&& (args.get(1) instanceof NumberElement)
				&& (((NumberElement)args.get(1)).isInteger())
				&& (((NumberElement)args.get(1)).getValue() >= 0);
	}

}
