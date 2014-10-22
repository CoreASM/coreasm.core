package org.coreasm.compiler.exception;

/**
 * Signals a general error in the compilation process
 * @author Markus Brenner
 *
 */
public class CompilerException extends Exception {
	private static final long serialVersionUID = -3225927157444291083L;
	
	/**
	 * Builds a new exception with the given child
	 * @param e A child exception
	 */
	public CompilerException(Exception e){
		super(e);
	}
	
	/**
	 * Builds a new exception with the given error message
	 * @param s The error message
	 */
	public CompilerException(String s){
		super(s);
	}
}
