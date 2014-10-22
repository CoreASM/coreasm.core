package org.coreasm.compiler.exception;

/**
 * Signals an exception in the main library entry
 * @author Markus Brenner
 *
 */
public class MainFileException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a new exception with the given message
	 * @param s The error message
	 */
	public MainFileException(String s){
		super(s);
	}
}
