/*	
 * NowFunctionElement.java 	1.0 	$Revision: 243 $
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

import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 * Implements 'now' as a monitored function that returns current time in milliseconds.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class NowFunctionElement extends FunctionElement {

	/** Name of this function */
	public static final String NOW_FUNC_NAME = "now";
	
	public NowFunctionElement() {
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
			return NumberElement.getInstance(System.currentTimeMillis());
	}

}
