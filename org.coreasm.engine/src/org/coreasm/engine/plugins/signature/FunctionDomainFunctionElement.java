/*	
 * FunctionDomainFunctionElement.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.signature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.plugins.list.ListElement;
import org.coreasm.engine.plugins.set.SetElement;

/** 
 * Provides a 'domain(f)' function that returns a set of lists of elements
 * for which function <i>f</i> has a value.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class FunctionDomainFunctionElement extends FunctionElement {

	/**
	 * Suggested name of this function
	 */
	public static final String FUNCTION_NAME = "domain";
	
	
	public FunctionDomainFunctionElement() {
		setFClass(FunctionClass.fcDerived);
	}
	
	/**
	 * If the args is a list of only one function element, this method
	 * returns a set of {@link ListElement ListElements} that contains values
	 * for which the given function element has values. Otherwise, returns
	 * {@link Element#UNDEF undef}.
	 * 
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 * @see ListElement
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 1) {
			Element e = args.get(0);
			if (e instanceof FunctionElement) {
				Set<Location> locs = ((FunctionElement)e).getLocations("");
				Set<Element> newSet = new HashSet<Element>();
				
				boolean unary = true;
				for (Location loc: locs) {
					newSet.add(new ListElement(loc.args));
					if (loc.args.size() != 1)
						unary = false;
				}
				
				// if the function is a unary function, 
				// return a set of values instead of list values
				if (unary) {
					Set<Element> newSet2 = new HashSet<Element>();
					for (Element l: newSet) 
						newSet2.add(((ListElement)l).get(1));
					newSet = newSet2;
				}
				
				return new SetElement(newSet);
				// Here it would be better if we used the SetBackground
				// to get a new value, but it doesn't matter for sets.
			}
		} 
		return Element.UNDEF;
	}

}
