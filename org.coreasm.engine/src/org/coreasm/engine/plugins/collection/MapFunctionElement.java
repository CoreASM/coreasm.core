/*	
 * MapFunctionElement.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;

/** 
 * Function element providing the 'map' function.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class MapFunctionElement extends CollectionFunctionElement {

	/** suggested name for this function */
	public static final String NAME = "map";

	private Signature signature = new Signature("ELEMENT", "FUNCTION", "ELEMENT");

	public MapFunctionElement(ControlAPI capi) {
		super(capi);
	}

	@Override
	public Signature getSignature() {
		return signature;
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!checkArguments(args))
			throw new CoreASMError("Illegal arguments for map.");
		Collection<? extends Element> values = ((Enumerable)args.get(0)).enumerate();
		FunctionElement f = (FunctionElement)args.get(1);
		Collection<Element> resultValues = new ArrayList<Element>();
		for (Element e: values) 
			resultValues.add(f.getValue(ElementList.create(e)));
		return ((AbstractMapElement)args.get(0)).getNewInstance(resultValues);
	}

	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 2) 
				&& (args.get(0) != null && args.get(0) instanceof AbstractMapElement)
				&& (args.get(1) != null && args.get(1) instanceof FunctionElement);
	}
}
