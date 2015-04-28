package org.coreasm.compiler.components.logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages logging for the compiler.
 * Stores message receivers and distributes incoming messages
 * @author Spellmaker
 *
 */
public class LoggingHelper {
	/**
	 * Message severity levels
	 * @author Spellmaker
	 *
	 */
	public enum Level {
		/**
		 * No relevant user information
		 */
		DEBUG,
		/**
		 * Mildly severe information
		 */
		WARN,
		/**
		 * Critical information
		 */
		ERROR
	}
	
	private Map<Level, List<MessageListener>> listeners;
	private Map<Class<?>, Logger> loggers;
	
	/**
	 * Public constructor
	 */
	public LoggingHelper(){
		listeners = new HashMap<Level, List<MessageListener>>();
		loggers = new HashMap<Class<?>, Logger>();
	}
	/**
	 * Registers a new message listener
	 * @param level The severity level of the messages this listener is registering for
	 * @param listener The listener
	 */
	public void addListener(Level level, MessageListener listener){
		List<MessageListener> list = listeners.get(level);
		if(list == null){
			list = new ArrayList<MessageListener>();
			listeners.put(level, list);
		}
		
		if(!list.contains(listener)){
			list.add(listener);
		}
	}
	
	/**
	 * Removes a listener from the provided severity level
	 * @param level The level of the messages the listener no longer wants to receive
	 * @param listener The listener
	 */
	public void removeListener(Level level, MessageListener listener){
		List<MessageListener> list = listeners.get(level);
		if(list != null){
			list.remove(listener);
		}
	}
	
	/**
	 * Logs a new message using the default logger and passes the message to registered listeners
	 * @param level The severity level of the message
	 * @param clazz The class issuing the message
	 * @param message The message
	 */
	public void log(Level level, Class<?> clazz, String message){
		List<MessageListener> l = listeners.get(level);
		if(l != null){
			for(MessageListener m : l) m.receiveMessage(level, message);
		}
		
		Logger log = loggers.get(clazz);
		if(log == null){
			log = LoggerFactory.getLogger(clazz);
			loggers.put(clazz, log);
		}
		
		switch(level){
			case DEBUG: log.debug(message);
				break;
			case WARN: log.warn(message);
				break;
			case ERROR: log.error(message);
				break;
		}
	}
	
	/**
	 * Logs a debug message
	 * @param clazz The cause of the message
	 * @param message The message
	 */
	public void debug(Class<?> clazz, String message){
		log(Level.DEBUG, clazz, message);
	}

	/**
	 * Logs a warning message
	 * @param clazz The cause of the message
	 * @param message The message
	 */	
	public void warn(Class<?> clazz, String message){
		log(Level.WARN, clazz, message);
	}
	
	/**
	 * Logs an error message
	 * @param clazz The cause of the message
	 * @param message The message
	 */
	public void error(Class<?> clazz, String message){
		log(Level.ERROR, clazz, message);
	}
	
	
}
