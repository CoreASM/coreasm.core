/*	
 * OutputFunctionElement.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.io;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.plugins.string.StringElement;

/** 
 * This class implements the 'output' function provided by the IO Plugin.
 *   
 * @author  Roozbeh Farahbod
 * 
 * @see org.coreasm.engine.plugins.io.IOPlugin
 */
public class OutputFunctionElement extends FunctionElement {

	private Set<Location> locations;
	private StringElement outputValues;
	
	public OutputFunctionElement() {
		setFClass(FunctionClass.fcOut);
		outputValues = new StringElement("");
		locations = new HashSet<Location>();
		locations.add(IOPlugin.PRINT_OUTPUT_FUNC_LOC);
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 0)
			return outputValues;
		else
			return Element.UNDEF;
	}

	/**
	 * Sets the value of this function only if there 
	 * is no argument.
	 */
	public void setValue(List<? extends Element> args, Element value) {
		if (args.size() == 0) {
			if (value instanceof StringElement) 
				outputValues = (StringElement)value;
			else
				outputValues = new StringElement(value.toString());
		}
	}

	/**
	 * Parameter <code>name</code> is ignored.
	 * 
	 * @see FunctionElement#getLocations(String)
	 */
	public Set<Location> getLocations(String name) {
		return locations;
	}

}
