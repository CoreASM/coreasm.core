/*	
 * InputFunctionElement.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.compiler.plugins.io.include;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.coreasm.engine.plugins.string.StringElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;

/** 
 * Implements the <i>input</i> monitored function provided by IO Plugin.
 *   
 * @author  Roozbeh Farahbod
 * 
 * @see org.coreasm.engine.plugins.io.IOPlugin
 */
public class InputFunctionElement extends FunctionElement {	
	/**
	 * Creates a new input function element
	 */
	public InputFunctionElement() {
		this.setFClass(FunctionClass.fcMonitored);
	}
	
	@Override
	public Element getValue(List<? extends Element> args) {
		String msg;
		// get the message argument
		if (args.size() == 0)
			msg = "";
		else
			msg = args.get(0).toString();
		
		String input = getMessage(msg);
		if (input == null)
			return Element.UNDEF;
		else
			return new StringElement(input);
	}
	
	
	private String getMessage(String message){
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));       
		System.out.print(message + " ");
		String result;
		try {
			result = stdin.readLine();
		} catch (IOException e) {
			result = "";
		}
		return result;
	}
}
