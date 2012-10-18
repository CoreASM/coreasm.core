/*	
 * ConsFunctionsElement.java  	$Revision: 243 $
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

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.collection.AbstractListElement;

/** 
 * Implementation of the 'cons' function for lists.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ConsFunctionElement extends FunctionElement {

	public static final String NAME = "cons";

	public ConsFunctionElement() {
		setFClass(FunctionClass.fcDerived);
	}
	
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (checkArguments(args)) {
			List<Element> newData = new ArrayList<Element>(((AbstractListElement)args.get(1)).getList());
			newData.add(0, args.get(0));
			result = new ListElement(newData);
		}
		return result;
	}
	
	public Signature getSignature() {
		Signature sig = new Signature();

		// TODO the domain should be ABSTRACT_LIST or something like that
		sig.setDomain(ElementBackgroundElement.ELEMENT_BACKGROUND_NAME, 
				ListBackgroundElement.LIST_BACKGROUND_NAME);
		sig.setRange(ListBackgroundElement.LIST_BACKGROUND_NAME);
		
		return sig;
	}
	
	/*
	 * Checks the arguments of the function
	 */
	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 2) && (args.get(1) instanceof AbstractListElement);
	}
}
