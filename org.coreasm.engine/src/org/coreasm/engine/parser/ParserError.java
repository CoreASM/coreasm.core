/*	
 * ParserError.java  	$Revision: 243 $
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
 
package org.coreasm.engine.parser;

/** 
 * This error is used to report errors during the parsing, specially
 * where an exception cannot be eaily thrown.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ParserError extends Error {

	public ParserError() {
	}

	public ParserError(String message) {
		super(message);
	}

	public ParserError(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	public ParserError(String message, Throwable cause) {
		super(message, cause);
	}

}
