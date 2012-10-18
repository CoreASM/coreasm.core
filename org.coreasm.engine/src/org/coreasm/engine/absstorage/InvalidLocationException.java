/*	
 * InvalidLocationException.java 	1.0 	$Revision: 243 $
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
 *	@author  Roozbeh Farahbod
 *  
 */
@SuppressWarnings("serial")
public class InvalidLocationException extends EngineException {

	public InvalidLocationException() {
		super();
	}

	public InvalidLocationException(String message) {
		super(message);
	}

	public InvalidLocationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidLocationException(Throwable cause) {
		super(cause);
	}

}
