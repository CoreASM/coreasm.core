/*	
 * ConstantFunction.java 	$Revision: 243 $
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
 
package org.coreasm.engine.absstorage;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 
 * Base of all constant functions.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ConstantFunction extends FunctionElement {

	protected final Element constantValue;
	protected final Set<? extends Element> range;
	
	/**
	 * Creates a constant function that returns a constant value.
	 * 
	 * @param constantValue the constant value
	 */
	public ConstantFunction(Element constantValue) {
		this.constantValue = constantValue;
		this.setFClass(FunctionClass.fcStatic);
		Set<Element> rangeSet = new HashSet<Element>();
		rangeSet.add(constantValue);
		this.range = Collections.unmodifiableSet(rangeSet);
	}

	/**
	 * Returns a constant value specified by the constructor.
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 0)
			return constantValue;
		else
			return Element.UNDEF;
	}

	/**
	 * Returns an unmodifiable set containing only the constant value.
	 */
	@Override
	public Set<? extends Element> getRange() {
		return range;
	}

	/**
	 * Returns the string representation of the constant value.
	 */
	@Override
	public String toString() {
		return constantValue.toString();
	}

	@Override
	public Set<Location> getLocations(String name) {
		Set<Location> locSet = new HashSet<Location>();
		locSet.add(new Location(name, ElementList.NO_ARGUMENT));
		return locSet;
	}
	
}
