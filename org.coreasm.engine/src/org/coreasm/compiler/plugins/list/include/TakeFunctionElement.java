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
 
package org.coreasm.compiler.plugins.list.include;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.engine.plugins.number.NumberElement;

import org.coreasm.engine.plugins.collection.AbstractListElement;
import CompilerRuntime.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
import CompilerRuntime.Runtime;
import CompilerRuntime.RuntimeProvider;

/** 
 * Implementation of the 'take(list, int)' function.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class TakeFunctionElement extends NthFunctionElement {

	public static final String NAME = "take";
	protected final Runtime capi;
	protected final AbstractStorage storage;
	
	public TakeFunctionElement() {
		this.capi = RuntimeProvider.getRuntime();
		this.storage = capi.getStorage();
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (checkArguments(args)) {
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
			
			result = new ListElement(newValues);
		}
		
		return result;
	}

	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 2) 
				&& (args.get(0) instanceof AbstractListElement)
				&& (args.get(1) instanceof NumberElement)
				&& (((NumberElement)args.get(1)).isInteger())
				&& (((NumberElement)args.get(1)).getValue() >= 0);
	}

}
