/*	
 * ToMapFunctionElement.java  	$Revision: 243 $
 * 
 * Copyright (C) 2009 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.collection.AbstractListElement;

/** 
 * A function that creates map elements from lists of pairs of elements.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ToMapFunctionElement extends FunctionElement {
	
	public static final String NAME = "toMap";
	
	protected Signature signature = null;
	
	public ToMapFunctionElement() {
		setFClass(FunctionClass.fcDerived);
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!checkArguments(args))
			throw new CoreASMError("Illegal arguments for " + NAME + ".");
		
		Enumerable s = (Enumerable)args.get(0);
		Map<Element, Element> map = new HashMap<Element, Element>();
		
		// go over all the elements of the enumerable and also
		// check that all the elements are pairs of size two 
		// and no two key values are equal
		for (Element e: s.enumerate()) {
			// if all the elements are not tuples return undef
			if (!(e instanceof AbstractListElement)) 
				throw new CoreASMError("Not all elements provided to " + NAME + " are tuples.");
			List<? extends Element> pair = ((AbstractListElement)e).getList();
			
			// if all the elements are not pairs (tuples of size 2) return undef
			if (pair.size() != 2)
				throw new CoreASMError("Not all elements provided to " + NAME + " are pairs.");
			Element k = pair.get(0);
			Element v = pair.get(1);
			
			// if there are two tuples with the same key, return undef
			if (map.put(k, v) != null)
				throw new CoreASMError("Duplicate key encountered by " + NAME + ": " + k);
		}
		return new MapElement(map);
	}

	@Override
	public Signature getSignature() {
		if (signature == null) {
			signature = new Signature();
			signature.setDomain(ElementBackgroundElement.ELEMENT_BACKGROUND_NAME);
			signature.setRange(MapBackgroundElement.NAME);
		}
		return signature;
	}
	
	/*
	 * Checks the arguments of the function
	 */
	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 1) && (args.get(0) instanceof Enumerable);
	}

}
