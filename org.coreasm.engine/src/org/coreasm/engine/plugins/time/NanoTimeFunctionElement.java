/*	
 * NanoTimeFunctionElement.java 	1.0
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
 
package org.coreasm.engine.plugins.time;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.number.NumberElement;

import java.util.List;

/** 
 * Implements 'nanoTime' as a monitored function that returns the current value of the running Java Virtual Machine's
 *         high-resolution time source, in nanoseconds.
 *   
 * @author  André Wolski
 * 
 */
public class NanoTimeFunctionElement extends FunctionElement {

	/** Name of this function */
	public static final String NANOTIME_FUNC_NAME = "nanoTime";

	public NanoTimeFunctionElement() {
		super();
		setFClass(FunctionClass.fcMonitored);
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() > 0) 
			return Element.UNDEF;
		else
			return NumberElement.getInstance(System.nanoTime());
	}

}
