/*	
 * ElementFormatException.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

/** 
 * Thrown to indicate that the application has attempted to 
 * convert a string to a specific element, but that the string 
 * does not have the appropriate format.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ElementFormatException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception that indicates conversion 
	 * to all the given element types have been tried
	 * but they all failed.
	 * 
	 *  @param value failed value
	 *  @param failedElementTypes element classes that failed
	 */
	public static ElementFormatException createInstance(String value, Class<? extends Element>...failedElementTypes) {
		String msg = "Failed to convert " + value;
		if (failedElementTypes.length == 0) {
			msg = msg + " to an element.";
		} else
			if (failedElementTypes.length == 1) {
				msg = msg + " to an instance of " + failedElementTypes[0].getSimpleName() + ".";
			} else {
				msg = msg + " to an instance of any of the types ";
				for (Class<? extends Element> c: failedElementTypes) {
					msg = msg + c.getSimpleName() + ", ";
				}
				msg = msg.substring(0, msg.length() - 2) + ".";
			}
		return new ElementFormatException(msg);
	}

	/**
	 * Creates a new instance of this exception 
	 * with the given message.
	 * 
	 * @param msg error message
	 */
	public ElementFormatException(String msg) {
		super(msg);
	}

}
