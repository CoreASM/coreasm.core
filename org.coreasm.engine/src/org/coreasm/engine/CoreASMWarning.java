/*	
 * CoreASMWarning.java  	$Revision$
 * 
 * Copyright (C) 2009 Roozbeh Farahbod
 *
 * Last modified by $Author$ on $Date$.
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
 * Represents a CoreASM warning message.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class CoreASMWarning extends CoreASMIssue {

	private static final long serialVersionUID = 1L;

	public final String src; 
	
	/**
	 * Creates a new CoreASM Warning.
	 * 
	 * @param src the module generating this warning 
	 * @param msg the warning msg
	 * @param cause the throwable that casused this warning
	 * @param pos position of the cause of this warning in the spec
	 * @param stack rule call stack
	 * @param node the parse-tree node that caused this warning
	 */
	public CoreASMWarning(String src, String msg, Throwable cause, CharacterPosition pos,
			Stack<CallStackElement> stack, Node node) {
		super(msg, cause, pos, stack, node);
		this.src = src;
	}

	/**
	 * Creates a new CoreASM Warning.
	 * 
	 * @param src the module generating this warning 
	 * @param msg the warning msg
	 * @param stack rule call stack
	 * @param node the parse-tree node that caused this warning
	 */
	public CoreASMWarning(String src, String msg, Stack<CallStackElement> stack, Node node) {
		super(msg, stack, node);
		this.src = src;
	}

	/**
	 * Creates a new CoreASM Warning.
	 * 
	 * @param src the module generating this warning 
	 * @param cause the throwable that casused this warning
	 * @param stack rule call stack
	 * @param node the parse-tree node that caused this warning
	 */
	public CoreASMWarning(String src, Throwable cause, Stack<CallStackElement> stack,
			Node node) {
		super(cause, stack, node);
		this.src = src;
	}

	/**
	 * Creates a new CoreASM Warning.
	 * 
	 * @param src the module generating this warning 
	 * @param msg the warning msg
	 * @param node the parse-tree node that caused this warning
	 */
	public CoreASMWarning(String src, String msg, Node node) {
		super(msg, node);
		this.src = src;
	}

	/**
	 * Creates a new CoreASM Warning.
	 * 
	 * @param src the module generating this warning 
	 * @param msg the warning msg
	 */
	public CoreASMWarning(String src, String msg) {
		super(msg);
		this.src = src;
	}

	/**
	 * Creates a new CoreASM Warning.
	 * 
	 * @param src the module generating this warning 
	 * @param cause the throwable that casused this warning
	 */
	public CoreASMWarning(String src, ParserException cause) {
		super(cause);
		this.src = src;
	}

	/**
	 * Creates and returns a string representation of this warning.
	 * 
	 * @param parser the current instance of {@link Parser}
	 * @param spec the current specification that caused this warning
	 */
	public String showWarning(Parser parser, Specification spec) {
		return "WARNING (" + src + "): " + super.showIssue(parser, spec);
	}

	/**
	 * Creates and returns a string representation of this warning.
	 */
	public String showWarning() {
		return showWarning(parser, spec);
	}
}
