/*	
 * CtrlStateFunctionElement.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-04-30 01:05:27 +0200 (Fr, 30 Apr 2010) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.step;

import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;

/**
 * Implements the control state function element of the Step plugin.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class CtrlStateFunctionElement extends FunctionElement {

	private final StepPlugin plugin;
	
	public CtrlStateFunctionElement(StepPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Returns an unmodifiable copy of the control state
	 * of the given agent.
	 * 
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 1)
			return ((SystemControlState)plugin.getControlState(args.get(0))).snapshot();
		else
			return Element.UNDEF;
	}
	
}
