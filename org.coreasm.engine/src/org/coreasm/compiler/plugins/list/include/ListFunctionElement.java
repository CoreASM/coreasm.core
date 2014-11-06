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
 
package org.coreasm.compiler.plugins.list.include;

import CompilerRuntime.AbstractStorage;
import org.coreasm.engine.absstorage.FunctionElement;
import CompilerRuntime.Runtime;
import CompilerRuntime.RuntimeProvider;

/** 
 * The general class of derived functions provided by the 
 * list plugin.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public abstract class ListFunctionElement extends FunctionElement {

	protected final Runtime capi;
	protected final AbstractStorage storage;
	
	public ListFunctionElement() {
		setFClass(FunctionClass.fcDerived);
		this.capi = RuntimeProvider.getRuntime();
		this.storage = capi.getStorage();
	}
	
}
