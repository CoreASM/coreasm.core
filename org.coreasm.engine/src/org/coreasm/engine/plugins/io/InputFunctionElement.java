/*	
 * InputFunctionElement.java 	1.0 	$Revision: 243 $
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

import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.string.StringElement;

/** 
 * Implements the <i>input</i> monitored function provided by IO Plugin.
 *   
 * @author  Roozbeh Farahbod
 * 
 * @see org.coreasm.engine.plugins.io.IOPlugin
 */
public class InputFunctionElement extends FunctionElement {

	private final IOPlugin plugin;
	
	/**
	 * Creates a new input function element with the given
	 * link to an IOPlugin.
	 *  
	 * @param ioPlugin the IOPlugin that created this object
	 * @see IOPlugin
	 */
	public InputFunctionElement(IOPlugin ioPlugin) {
		this.plugin = ioPlugin;
		this.setFClass(FunctionClass.fcMonitored);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		String msg;
		// get the message argument
		if (args.size() == 0)
			msg = "";
		else
			msg = args.get(0).toString();
		
		if (plugin.inputProvider != null) {
			String input = plugin.inputProvider.getValue(msg);
			if (input == null)
				return Element.UNDEF;
			else
				return new StringElement(input);
		} else
			return Element.UNDEF;
	}
}
