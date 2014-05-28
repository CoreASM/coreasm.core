/*	
 * ToSetFunctionElement.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
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

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;

/** 
 * Converts {@link Enumerable}s to {@link SetElement}s.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class ToSetFunctionElement extends FunctionElement {

	public static final String NAME = "toSet";
	
	protected Signature signature = null;

	public ToSetFunctionElement() {
		setFClass(FunctionClass.fcDerived);
	}
	
	public Signature getSignature() {
		if (signature == null) {
			signature = new Signature();
			signature.setDomain(ElementBackgroundElement.ELEMENT_BACKGROUND_NAME);
			signature.setRange(SetBackgroundElement.SET_BACKGROUND_NAME);
		}
		return signature;
	}
	
	/**
	 * If args contains only one instance of {@link Enumerable}, 
	 * this method returns a {@link SetElement} view of that 
	 * enumerable.
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!(args.size() == 1 && args.get(0) instanceof Enumerable))
			throw new CoreASMError("Illegal arguments for " + NAME + ".");
		
		return new SetElement(((Enumerable)args.get(0)).enumerate());
	}

}
