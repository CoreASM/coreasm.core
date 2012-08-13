/*	
 * UnmodifiableFunctionException.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import org.coreasm.engine.EngineException;

/** 
 * Thrown in case of an attempt to change the value of a non-modifiable function.
 *  
 * @author  Roozbeh Farahbod
 * 
 */
@SuppressWarnings("serial")
public class UnmodifiableFunctionException extends EngineException {

	public UnmodifiableFunctionException() {
		super();
	}

	public UnmodifiableFunctionException(String message) {
		super(message);
	}

	public UnmodifiableFunctionException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnmodifiableFunctionException(Throwable cause) {
		super(cause);
	}

}
