/*
 * InvalidSpecificationException.java 	1.0 	$Revision: 243 $
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
 * An exception thrown when there is an error within a CoreASM specification.
 * 
 * @author Roozbeh Farahbod
 * 
 */
@SuppressWarnings("serial")
public class InvalidSpecificationException extends EngineException {

	/**
	 * @see Exception#Exception()
	 */
	public InvalidSpecificationException() {
		super();
	}

	/**
	 * @see Exception#Exception(java.lang.String)
	 */
	public InvalidSpecificationException(String message) {
		super(message);
	}

	/**
	 * @see Exception#Exception(java.lang.String, java.lang.Throwable)
	 */
	public InvalidSpecificationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see Exception#Exception(java.lang.Throwable)
	 */
	public InvalidSpecificationException(Throwable cause) {
		super(cause);
	}

}

