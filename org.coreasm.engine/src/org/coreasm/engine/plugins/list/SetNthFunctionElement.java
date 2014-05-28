/*	
 * SetNthFunctionElement.java  	$Revision: 243 $
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
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.number.NumberBackgroundElement;
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 * Implementation of the 'setnth' function which sets a new 
 * value at a specific index in the list and returns a new list.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class SetNthFunctionElement extends ListFunctionElement {

	public static final String NAME = "setnth";
	
	protected Signature signature;
	
	public SetNthFunctionElement(ControlAPI capi) {
		super(capi);
		setFClass(FunctionClass.fcDerived);
		signature = new Signature();
		signature.setDomain(
				ListBackgroundElement.LIST_BACKGROUND_NAME,
				NumberBackgroundElement.NUMBER_BACKGROUND_NAME,
				ElementBackgroundElement.ELEMENT_BACKGROUND_NAME);
		signature.setRange(ListBackgroundElement.LIST_BACKGROUND_NAME);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!checkArguments(args))
			throw new CoreASMError("Illegal arguments for " + NAME + ".");
		
		ListElement list = (ListElement) args.get(0);
		NumberElement n = (NumberElement)args.get(1);
		if (n.getValue() >= list.size())
			throw new CoreASMError("Index out of range for setnth.");
		
		List<Element> resultValues = new ArrayList<Element>();
		for (Element e: list.enumerate()) 
			resultValues.add(e);
		resultValues.set((int)n.getValue()-1, args.get(2));
		
		return new ListElement(resultValues);
	}

	public Signature getSignature() {
		return signature;
	}
	
	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 3) 
				&& (args.get(0) instanceof ListElement)
				&& (args.get(1) instanceof NumberElement)
				&& (((NumberElement)args.get(1)).isNatural())
				&& (args.get(2) != null);
	}
}

