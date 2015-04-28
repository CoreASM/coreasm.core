package org.coreasm.compiler.exception;

/**
 * Signals an error in a file inclusion
 * @author Markus Brenner
 *
 */
public class IncludeException extends Exception {
	private static final long serialVersionUID = 6239979635723045111L;
	/**
	 * Builds a new exception object with the given child exception
	 * @param e The child exception
	 */
	public IncludeException(Exception e){
		super(e);
	}
	/**
	 * Builds a new exception with the given error message
	 * @param string An error message
	 */
	public IncludeException(String string) {
		super(string);
	}
}
