/*
 * CoreASMIssue.java 		$Revision: 109 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-12-15 20:08:31 +0100 (Di, 15 Dez 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.engine;

import java.util.Stack;

import org.coreasm.engine.interpreter.Interpreter.CallStackElement;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.CharacterPosition;
import org.coreasm.engine.parser.Parser;
import org.coreasm.engine.parser.ParserException;

/**
 * Represents an issue (error or warning) in the CoreASM engine. 
 *   
 * @author Roozbeh Farahbod
 *
 */

public class CoreASMIssue extends Error {

	private static final long serialVersionUID = 1L;
	
	public final String message; 
	public final Throwable cause;
	public final CharacterPosition pos;
	public final Node node;
	public final Stack<CallStackElement> callStack;
	
	protected Parser parser = null;
	protected Specification spec = null;
	
	@SuppressWarnings("unchecked")
	public CoreASMIssue(String msg, Throwable cause, CharacterPosition pos, Stack<CallStackElement> stack, Node node) {
		this.message = msg;
		this.cause = cause;
		this.pos = pos;
		if (stack != null)
			this.callStack = (Stack<CallStackElement>) stack.clone();
		else
			this.callStack = null;
		this.node = node;
	}
	
	public CoreASMIssue(String msg, Stack<CallStackElement> stack, Node node) {
		this(msg, null, null, stack, node);
	}
	
	public CoreASMIssue(Throwable cause, Stack<CallStackElement> stack, Node node) {
		this(null, cause, null, stack, node);
	}
	
	public CoreASMIssue(String msg, Node node) {
		this(msg, null, null, null, node);
	}
	
	public CoreASMIssue(String msg) {
		this(msg, null, CharacterPosition.NO_POSITION, null, null);
	}
	
	public CoreASMIssue(ParserException cause) {
		this(cause.msg, cause, cause.pos, null, null);
	}
	
	public void setContext(Parser parser, Specification spec) {
		this.parser = parser;
		this.spec = spec;
	}
	
	public CharacterPosition getPos() {
		if (pos == null && node != null && node.getScannerInfo() != null && parser != null) 
			return node.getScannerInfo().getPos(parser.getPositionMap());
		return pos;
	}
	
	public Specification getSpec() {
		return spec;
	}
	
	/**
	 * Creates and returns a string representation of this issue.
	 */
	public String showIssue(Parser parser, Specification spec) {
		StringBuffer buf = new StringBuffer();
		if (message == null)
			if (cause != null)
				buf.append("Exception occured: " + (cause.getCause() != null ? cause.getCause() : cause));
			else
				buf.append("Something is not right.");
		else
			buf.append(message);
		
		if (buf.charAt(buf.length()-1) == '\n')
			buf.deleteCharAt(buf.length() - 1);
		
		CharacterPosition tempPos = getPos();
		
		if (tempPos != null && spec != null) {
			final String posStr = tempPos.toString(spec);
			if (posStr.length() > 1)
				buf.append(" (check " + tempPos.toString(spec) + ")");
		}
		
		if (callStack != null) 
			buf.append("\n" + printCallStack(callStack));
		
		return buf.toString();
	}
	
	public String showIssue() {
		return showIssue(parser, spec);
	}
	
	
	@Override
	public Throwable getCause() {
		return this.cause;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/*
	 * Creates a string representation of the call stack.
	 */
	private String printCallStack(Stack<CallStackElement> stack) {
		String result = "";
		if (stack.size() > 0) { 
			result += "    in " + stack.lastElement() + "\n";
			for (int i = stack.size() - 2; i >= 0; i--) {
				CallStackElement cse = stack.get(i); 
				result += "    called from " + cse + "\n";
			}
		}
		return result;
	}

}
