/*  
 * ExtendRulePlugin.java    1.0     $Revision: 243 $
 * 
 * Copyright (C) 2006 Mashaal Memon
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.set;

import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.number.NumberBackgroundElement;
import org.coreasm.engine.plugins.number.NumberPlugin;

/** 
 *	setCardinality function element gives the cardinality of a set
 *   
 *  @author  Mashaal Memon
 *  
 */
public class SetCardinalityFunctionElement extends FunctionElement {

	/**
	 * Suggested name of this function
	 */
	public static final String SET_CARINALITY_FUNCTION_NAME = "setCardinality";
	
	private final ControlAPI capi;
	private NumberBackgroundElement numberBackground;
	
	/**
	 * Creates new set cardinality function, which is actually derived.
	 * 
	 * @see #SET_CARINALITY_FUNCTION_NAME 
	 */
	public SetCardinalityFunctionElement(ControlAPI capi) {
		setFClass(FunctionClass.fcDerived);
		this.capi = capi;
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		
		// if not one single argument, return undef
		if (args.size() != 1)
			return defaultValue;
		else
		{
			Element argument = (Element)(args.toArray()[0]);

			// if argument is not a set element, return undef
			if (!(argument instanceof SetElement))
			{
				return defaultValue;
			}
			// otherwise return a number element representing cardinality of the set.
			else
			{
				SetElement se = (SetElement)argument;
				if (numberBackground == null) {
					NumberPlugin p = (NumberPlugin)capi.getPlugin(NumberPlugin.PLUGIN_NAME);
					numberBackground = p.getNumberBackgroundElement();
				}
				return numberBackground.getNewValue(se.enumerate().size());
			}
		}
	}


}
