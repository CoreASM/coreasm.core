package org.coreasm.compiler.codefragment;

/**
 * Signals an error within a code fragment
 * @author Markus Brenner
 *
 */
public class CodeFragmentException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new CodeFragmentException with the given error message
	 * @param string The error message
	 */
	public CodeFragmentException(String string) {
		super(string);
	}
	
	/**
	 * Calls the super constructor, building a new CodeFragmentException
	 */
	public CodeFragmentException(){
		super();
	}

}
