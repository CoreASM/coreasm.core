package org.coreasm.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingHelper {
	public enum Level {
		DEBUG,
		WARN,
		ERROR
	}
	
	private Map<Level, List<MessageListener>> listeners;
	private Map<Class<?>, Logger> loggers;
	
	public LoggingHelper(){
		listeners = new HashMap<Level, List<MessageListener>>();
		loggers = new HashMap<Class<?>, Logger>();
	}
	
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
	
	public void removeListener(Level level, MessageListener listener){
		List<MessageListener> list = listeners.get(level);
		if(list != null){
			list.remove(listener);
		}
	}
	
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
	
	public void debug(Class<?> clazz, String message){
		log(Level.DEBUG, clazz, message);
	}
	
	public void warn(Class<?> clazz, String message){
		log(Level.DEBUG, clazz, message);
	}
	
	public void error(Class<?> clazz, String message){
		log(Level.ERROR, clazz, message);
	}
	
	
}
