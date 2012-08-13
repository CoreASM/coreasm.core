/*	
 * Logger.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.util;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

//import java.rmi.server.LogStream;


/** 
 *	Provides a mechanism for logging errors, warnings, and debugging info.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class Logger {
	
	/** Standard verbosity levels. */
	public static final int FATAL = 1;
	public static final int ERROR = 2;
	public static final int WARNING = 3;
	public static final int INFORMATION = 4;
	
	/** Set of all the visible loggers. */
	public static final Set<Logger> visibleLoggers = new HashSet<Logger>();
	
	/** Standard loggers, one for each module. */
	public static final Logger parser = new Logger("Parser");
	public static final Logger interpreter = new Logger("Interpreter");
	public static final Logger scheduler = new Logger("Scheduler");
	public static final Logger storage = new Logger("Storage");
	public static final Logger controlAPI = new Logger("ControlAPI");
	public static final Logger plugins = new Logger("Plugins");
	public static final Logger ui = new Logger("UI");

	/** A general purpose/ global logger */
	public final static Logger global = new Logger("Global");
	
	/** Current verbosity level. */
	public static int verbosityLevel = 10;

	/** Default stream which is <code>System.err</code>. */
	private static PrintStream defaultStream = System.err;
	
	/** Output stream */
	private static PrintStream stream = defaultStream;
	
	/** Defines whether logs should be time stamped or not. */
	private static boolean timeStamp = true;
	
	/** Defines whether the current thread's name should be printed out or not */
	private static boolean threadNameStamp = true;

	/** Name of a logger instance. */
	public final String name;
	
	/**
	 * Creates a new logger and adds it to the 
	 * set of visible loggers.
	 *
	 */
	public Logger(String name) {
		visibleLoggers.add(this);
		this.name = name;
	}
	
	/**
	 * Logs a new message in a given level. If the level is not visible,
	 * it does nothing. If time stamping is set, the message will be logged
	 * with a time stamp.
	 * 
	 * @param level the verbosity level of the message
	 * @param msg the message to be logged
	 */
	public synchronized void log(int level, String msg) {
		if (verbosityLevel >= level && visibleLoggers.contains(this)) {
			StringBuffer str = new StringBuffer();
			if (threadNameStamp) 
				str.append(Thread.currentThread().getName() + " ");
			str.append("[" + name + "] ");
			if (timeStamp)
				str.append("@ " + getCurrentTime() + " ");
				//str.append((new Date()).toString() + ": ");
			str.append("* " + msg);
			stream.println(str.toString());
		}
	}
	
	/**
	 * Turns the visibility of this logger on or off.
	 * 
	 * @param visible if <code>true</code>, turns this logger visible.
	 */
	public synchronized void setVisible(boolean visible) {
		if (visible) 
			visibleLoggers.add(this);
		else
			visibleLoggers.remove(this);
	}
	
	/**
	 * Returns <code>true</code> if this logger is a visible logger.
	 */
	public boolean isVisible() {
		return visibleLoggers.contains(this);
	}
	
	/** 
	 * Logs a new message in a given level. It uses 
	 * <code>logger.log(int, String)</code> to log this message.
	 * 
	 * @param level the verbosity level of the message
	 * @param logger the logger that should log this message
	 * @param msg the message to be logged
	 */
	public static synchronized void log(int level, Logger logger, String msg) {
		logger.log(level, msg);
	}
	
	/**
	 * Sets a new output stream. 
	 * 
	 * @param newStream new stream
	 */
	public static synchronized void setStream(PrintStream newStream) {
		if (newStream != null) {
			stream = newStream;
		}
	}
	
	/**
	 * Turn time stamping on or off.
	 * 
	 * @param stamp <code>true</code> will turn time stamping on. 
	 */
	public static synchronized void setTimeStamp(boolean stamp) {
		timeStamp = stamp;
	}
	
	/** 
	 * Returns the current status of time stamping.
	 * 
	 * @return <code>true</code> if time stamping is on.
	 */
	public static boolean getTimeStamp() {
		return timeStamp;
	}
	
	private static String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		StringBuffer str = new StringBuffer();
		str.append(cal.get(Calendar.YEAR) + "-");
		str.append(cal.get(Calendar.MONTH) + "-");
		str.append(cal.get(Calendar.DAY_OF_MONTH) + " ");
		str.append(cal.get(Calendar.HOUR_OF_DAY) + ":");
		str.append(cal.get(Calendar.MINUTE) + ":");
		str.append(cal.get(Calendar.SECOND));
		return str.toString();
	}
}
