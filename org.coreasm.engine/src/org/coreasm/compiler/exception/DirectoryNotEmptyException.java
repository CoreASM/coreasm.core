package org.coreasm.compiler.exception;

/**
 * Signals, that the temporary directory is not empty
 * @author Markus Brenner
 *
 */
public class DirectoryNotEmptyException extends Exception {
	private static final long serialVersionUID = 4960230388525320056L;

	/**
	 * Builds a new exception with the given error message
	 * @param s The error message
	 */
	public DirectoryNotEmptyException(String s){
		super(s);
	}
}
