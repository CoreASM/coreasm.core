/*	
 * NodeToFormatStringMapper.java  	$Revision: 243 $
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
 
package org.coreasm.engine.interpreter;

/** 
 * The interface to map nodes to format strings.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public interface NodeToFormatStringMapper<N extends Node>  {
	
	/**
	 * Provides a format string to unparse the given node. 
	 * The resulting format string is used to format an array of
	 * string objects that includes the string representation 
	 * of the node itself and all its children in order. 
	 */
	public String getFormatString(N node);

}
