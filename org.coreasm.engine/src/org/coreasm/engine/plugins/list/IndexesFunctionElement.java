/*	
 * IndexesFunctionElement.java  	$Revision: 243 $
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
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.collection.AbstractListElement;

/** 
 * Implementation of the 'indexes(e, list)' function.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class IndexesFunctionElement extends FunctionElement {

	public static final String NAME = "indexes";
	public static final String NAME_ALTERNATIVE = "indices";
	
	protected final ControlAPI capi;
	protected final AbstractStorage storage;
	protected Signature signature;
	
	public IndexesFunctionElement(ControlAPI capi) {
		this.capi = capi;
		this.storage = capi.getStorage();
		setFClass(FunctionClass.fcDerived);
		signature = new Signature(2);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (checkArguments(args)) {
			AbstractListElement list = (AbstractListElement)args.get(0);
			result = new ListElement(list.indexesOf(args.get(1)));
		} 
		
		return result;
	}

	public Signature getSignature() {
		return signature;
	}
	
	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 2) 
				&& (args.get(0) instanceof AbstractListElement);
	}

}
