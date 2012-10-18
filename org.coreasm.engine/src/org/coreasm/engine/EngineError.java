/*	
 * EngineError.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine;

/** 
 * To be thrown when there is a serious error in the engine.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
@SuppressWarnings("serial")
public class EngineError extends Error {

	public EngineError() {
		super();
	}

	/**
	 * @param message
	 */
	public EngineError(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EngineError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public EngineError(Throwable cause) {
		super(cause);
	}

}
