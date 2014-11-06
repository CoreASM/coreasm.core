/*	
 * FlattenListFunctionElement.java  	$Revision: 80 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-07-24 10:25:41 -0400 (Fri, 24 Jul 2009) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.compiler.plugins.list.include;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.engine.plugins.collection.AbstractListElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;

/** 
 * Flattens a {@link ListElement} containing other {@link ListElement}s.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class FlattenListFunctionElement extends FunctionElement {

	public static final String NAME = "flattenList";
	
	protected Signature signature = null;

	public FlattenListFunctionElement() {
		setFClass(FunctionClass.fcDerived);
	}
	
	public Signature getSignature() {
		if (signature == null) {
			signature = new Signature();
			signature.setDomain(ListBackgroundElement.LIST_BACKGROUND_NAME);
			signature.setRange(ListBackgroundElement.LIST_BACKGROUND_NAME);
		}
		return signature;
	}
	
	/**
	 * If args contains only one instance of {@link AbstractListElement}, 
	 * this method returns a flatten {@link ListElement}.
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (args.size() == 1 && args.get(0) instanceof AbstractListElement) {
			result = new ListElement(flattenList(((AbstractListElement) args.get(0)).getList()));
		}
		return result;
	}
	
	/*
	 * Flattens a list of elements such that all the immediate list elements are expanded.
	 */
	protected List<? extends Element> flattenList(List<? extends Element> list) {
		ArrayList<Element> result = null;
		boolean compound = false;
		for (Element e: list) 
			if (e instanceof AbstractListElement) 
				compound = true;
		if (compound) {
			result = new ArrayList<Element>();
			for (Element e: list) 
				if (e instanceof AbstractListElement)
					result.addAll(flattenList(((AbstractListElement) e).getList()));
				else
					result.add(e);
			return result;
		} else
			return list;
	}
}
