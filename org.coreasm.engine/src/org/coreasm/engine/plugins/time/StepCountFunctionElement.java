/*	
 * StepCountFunctionElement.java 	1.0 	$Revision: 237 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-08 03:48:20 +0100 (Di, 08 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.time;

import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 * Provides a monitored function that returns current step count.
 *   
 * @author  Roozbeh Farahbod
 */
public class StepCountFunctionElement extends FunctionElement {

	/** Name of this function */
	public static final String FUNC_NAME = "stepcount";
	
	private final ControlAPI capi;
	
	public StepCountFunctionElement(ControlAPI capi) {
		super();
		setFClass(FunctionClass.fcMonitored);
		this.capi = capi;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() > 0) 
			return Element.UNDEF;
		else
			return NumberElement.getInstance(capi.getStepCount());
	}

}
