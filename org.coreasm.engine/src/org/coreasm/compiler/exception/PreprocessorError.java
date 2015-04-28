package org.coreasm.compiler.exception;

/**
 * Signals an error in the preprocessor
 * @author Markus Brenner
 *
 */
public class PreprocessorError extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Passes the message to the super constructor
	 * @param e An error message
	 */
	public PreprocessorError(String e){
		super(e);
	}
}
