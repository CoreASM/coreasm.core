/*	
 * ReverseFunctionElement.java  	$Revision: 243 $
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
import org.coreasm.engine.absstorage.FunctionElement;
import CompilerRuntime.Runtime;
import CompilerRuntime.RuntimeProvider;

/** 
 * Impelements the 'reverse' function.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ReverseFunctionElement extends FunctionElement {

	public static final String NAME = "reverse";
	protected final Runtime capi;
	protected final AbstractStorage storage;
	
	public ReverseFunctionElement() {
		setFClass(FunctionClass.fcDerived);
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
			ArrayList<Element> resultValues = new ArrayList<Element>();
			
			int i = list.size();
			while (i > 0) {
				resultValues.add(list.get(NumberElement.getInstance(i)));
				i--;
			}

			result = new ListElement(resultValues);
			
		}
		return result;
	}

	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 1) 
				&& (args.get(0) instanceof AbstractListElement);
	}

}
