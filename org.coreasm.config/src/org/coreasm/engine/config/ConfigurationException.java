/**
 * Copyright (C) 2012 Roozbeh Farahbod 
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.config;

/**
 * Thrown when there is a problem with the CoreASM configuration module.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class ConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a default configuration exception. 
	 */
	public ConfigurationException() {
		super();
	}

	/**
	 * Creates a configuration exception with the given message
	 * and cause. 
	 * 
	 * @param msg exception message
	 * @param cause the cause of this exception
	 */
	public ConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Creates a configuration exception with the given message. 
	 * 
	 * @param msg exception message
	 */
	public ConfigurationException(String msg) {
		super(msg);
	}

	/**
	 * Creates a configuration exception with the given cause. 
	 * 
	 * @param cause the cause of this exception
	 */
	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	
}
