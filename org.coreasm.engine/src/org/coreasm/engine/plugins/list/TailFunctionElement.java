/*	
 * HeadLastFunctionElement.java  	$Revision: 243 $
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

import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.collection.AbstractListElement;

/** 
 * Impelementation of the 'tail' function on lists.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class TailFunctionElement extends ListFunctionElement {
	
	public static final String NAME = "tail";
	
	protected Signature signature = null;
	
	public TailFunctionElement(ControlAPI capi) {
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
		return list.tail();
	}

	public Signature getSignature() {
		if (signature == null) {
			signature = new Signature();
			signature.setDomain(ListBackgroundElement.LIST_BACKGROUND_NAME);
			signature.setRange(ElementBackgroundElement.ELEMENT_BACKGROUND_NAME);
		}
		return signature;
	}
	
	/*
	 * Checks the arguments of the function
	 */
	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 1) && (args.get(0) instanceof AbstractListElement);
	}

}
