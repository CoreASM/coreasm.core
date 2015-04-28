package org.coreasm.compiler.exception;

/**
 * Signals an error in the variable manager.
 * Occurs when trying to pop a context when there is no context on the stack
 * @author Markus Brenner
 *
 */
public class EmptyContextStackException extends Exception {
	private static final long serialVersionUID = 1513069534185329843L;

	/**
	 * Builds a new Exception with the given error message
	 * @param s The error message
	 */
	public EmptyContextStackException(String s){
		super(s);
	}
}
