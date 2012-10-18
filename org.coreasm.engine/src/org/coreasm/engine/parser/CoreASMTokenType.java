/*	
 * CoreASMTokenType.java  	$Revision: 243 $
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
 * Token types specific to the CoreASM parser.
 *   
 * @see jfun.parsec.tokens.TokenType
 * 
 * @author  Roozbeh Farahbod
 * 
 */
public enum CoreASMTokenType {

	/** comments */
	Comment,
	
	/** whitespace */
	Whitespace
}
