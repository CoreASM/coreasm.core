package org.coreasm.compiler;

/**
 * Thrown to signalize an error in the command line
 * of CoreASMC
 * @author Markus Brenner
 *
 */
public class CommandLineException extends Exception {
	/**
	 * @param string The concrete error message
	 */
	public CommandLineException(String string) {
		super(string);
	}

	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

}
