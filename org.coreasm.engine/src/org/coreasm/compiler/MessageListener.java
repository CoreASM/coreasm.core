package org.coreasm.compiler;

/**
 * Base interface for classes listening to compiler messages
 * @author Spellmaker
 *
 */
public interface MessageListener {
	/**
	 * Notifies the instance of a new logging message of the compiler
	 * @param level The severity level of the information
	 * @param msg The actual message
	 */
	public void receiveMessage(LoggingHelper.Level level, String msg);
}
