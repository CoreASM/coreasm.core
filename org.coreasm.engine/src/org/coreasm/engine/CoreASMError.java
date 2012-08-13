/*
 * CoreASMError.java 		$Revision: 94 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-08-03 15:23:52 +0200 (Mo, 03 Aug 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.engine;

import java.util.Stack;

import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.Interpreter.CallStackElement;
import org.coreasm.engine.parser.CharacterPosition;
import org.coreasm.engine.parser.Parser;
import org.coreasm.engine.parser.ParserException;

/**
 * Represents a general error in CoreASM engine. 
 *   
 * @author Roozbeh Farahbod
 *
 */

public class CoreASMError extends CoreASMIssue {

	private static final long serialVersionUID = 1L;
	
	public CoreASMError(String msg, Throwable cause, CharacterPosition pos, Stack<CallStackElement> stack, Node node) {
		super(msg, cause, pos, stack, node);
	}
	
	public CoreASMError(String msg, Stack<CallStackElement> stack, Node node) {
		this(msg, null, null, stack, node);
	}
	
	public CoreASMError(Throwable cause, Stack<CallStackElement> stack, Node node) {
		this(null, cause, null, stack, node);
	}
	
	public CoreASMError(String msg, Node node) {
		this(msg, null, null, null, node);
	}
	
	public CoreASMError(String msg) {
		this(msg, null, CharacterPosition.NO_POSITION, null, null);
	}
	
	public CoreASMError(ParserException cause) {
		this(cause.msg, cause, cause.pos, null, null);
	}
	
	/**
	 * Creates and returns a string representation of this error.
	 */
	public String showError(Parser parser, Specification spec) {
		return showIssue(parser, spec);
	}
	
	public String showError() {
		return showError(parser, spec);
	}
	
}
