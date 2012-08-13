/*	
 * InterpreterException.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.interpreter;

import org.coreasm.engine.EngineException;

/** 
 * Interpreter Exceptions
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
@SuppressWarnings("serial")
public class InterpreterException extends EngineException {

	/**
	 * 
	 */
	public InterpreterException() {
		super();
	}

	/**
	 * @param message
	 */
	public InterpreterException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InterpreterException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public InterpreterException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

}
