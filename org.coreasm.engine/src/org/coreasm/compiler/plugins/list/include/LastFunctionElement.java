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
 
package org.coreasm.compiler.plugins.list.include;

import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.plugins.collection.AbstractListElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.list.ListBackgroundElement;
import org.coreasm.engine.plugins.list.ListFunctionElement;

/** 
 * Implementation of head and last functions on lists.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class LastFunctionElement extends ListFunctionElement {
	/**
	 * The head function name
	 */
	public static final String HEAD_FUNC_NAME = "head";
	/**
	 * The last function name
	 */
	public static final String LAST_FUNC_NAME = "last";
	
	protected Signature signature = null;
	protected final boolean isHead;
	
	/**
	 * Constructs the element
	 * @param capi The capi for this element
	 */
	public LastFunctionElement(ControlAPI capi) {
		super(capi);
		this.isHead = false;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (checkArguments(args)) {
			AbstractListElement list = (AbstractListElement)args.get(0);
			if (isHead) {
				result = list.head();
			} else {
				result = list.last();
			}
		}
		return result;
	}

	@Override
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
