/*	
 * CollectionFunctionElement.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.collection;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.FunctionElement;

/** 
 * The general class of derived functions provided by the 
 * collection plugin.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public abstract class CollectionFunctionElement extends FunctionElement {

	protected final ControlAPI capi;
	protected final AbstractStorage storage;
	
	public CollectionFunctionElement(ControlAPI capi) {
		setFClass(FunctionClass.fcDerived);
		this.capi = capi;
		this.storage = capi.getStorage();
	}
	
}
