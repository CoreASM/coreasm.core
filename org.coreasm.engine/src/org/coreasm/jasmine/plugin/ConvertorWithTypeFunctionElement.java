/*
 * ConvertorWithTypeFunctionElement.java 		$Revision: 9 $
 * 
 * Copyright (c) 2008 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.jasmine.plugin;

import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.Element;

import org.coreasm.engine.plugins.string.StringElement;
import org.coreasm.util.Logger;

/**
 * Converts CoreASM elements to Java objects casted to the given class name.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class ConvertorWithTypeFunctionElement extends org.coreasm.engine.absstorage.FunctionElement {

	/** suggested name of this function */
	public static final String NAME = "castToJava";
	
	private final JasminePlugin plugin;
	
	public ConvertorWithTypeFunctionElement(JasminePlugin plugin) {
		this.setFClass(FunctionClass.fcDerived);
		this.plugin = plugin;
	}

	@Override
	public Element getValue(List<? extends Element> args) {
		JObjectElement result = null;
		if (args.size() == 2) 
			if (args.get(1) instanceof StringElement) {
				Element value = args.get(0);
				String className = ((StringElement)args.get(1)).getValue();
				
				Class<? extends Object> requestedClass = null;
				try {
					requestedClass = JasmineUtil.getJavaClass(className, plugin.getClassLoader());
				} catch (Exception e) {
					Logger.log(Logger.ERROR, Logger.plugins, "Java class '" + className + "' not found (" + NAME + " function).");
					return result;
				}

				result = JasmineUtil.javaValue(value);
				Class actualClass = result.object.getClass();
				
				if (!actualClass.equals(requestedClass)) {
					try {
						actualClass.asSubclass(requestedClass);
						
					} catch (Exception e) {
						
					}
				}
				
			}
		
		return result;
	}

	
}
