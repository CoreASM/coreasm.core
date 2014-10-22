package org.coreasm.compiler.exception;

/**
 * Signals an error in the state machine creation
 * @author Markus Brenner
 *
 */
public class InvalidStateMachineException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new Exception with the given child exception
	 * @param e A child exception
	 */
	public InvalidStateMachineException(Exception e) {
		super(e);
	}

	/**
	 * Creates an empty exception object
	 */
	public InvalidStateMachineException() {
	}

}
