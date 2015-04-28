package org.coreasm.compiler.exception;

/**
 * Signals a general error within a library entry
 * @author Markus Brenner
 *
 */
public class LibraryEntryException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a new exception with the given child exception
	 * @param e The child exception
	 */
	public LibraryEntryException(Exception e){
		super(e);
	}

	/**
	 * Builds a new exception with the given error message
	 * @param string An error message
	 */
	public LibraryEntryException(String string) {
		super(string);
	}
}
