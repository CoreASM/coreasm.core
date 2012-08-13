/*
 * EngineException.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine;

/**
 * This is the superclass of all CoreASM engine exceptions.
 * 
 * @author Roozbeh Farahbod
 * 
 */
public class EngineException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	* @see Exception#Exception()
	*/
	public EngineException() {
		super();
	}
	
	/**
	* @see Exception#Exception(java.lang.String)
	*/
	public EngineException(String message) {
		super(message);
	}
	
	/**
	* @see Exception#Exception(java.lang.String, java.lang.Throwable)
	*/
	public EngineException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	* @see Exception#Exception(java.lang.Throwable)
	*/
	public EngineException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
}


