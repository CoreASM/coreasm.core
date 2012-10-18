/*	
 * InitializationFailedException.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugin;

/** 
 * Exception thrown if a plugin initialization fails. 
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class InitializationFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	public final Plugin plugin;
	
	/**
	 * @param p failed plugin
	 */
	public InitializationFailedException(Plugin p) {
		super("Plugin " + p.getName() + " failed to initialize.");
		plugin = p;
	}

	/**
	 * @param p failed plugin
	 * @param reason the reason for failure
	 */
	public InitializationFailedException(Plugin p, String reason) {
		super("Plugin " + p.getName() + " failed to initialize. Reason: " + reason);
		plugin = p;
	}

	/**
	 * @param p failed plugin
	 * @param reason the {@link Throwable} that caused the failure
	 */
	public InitializationFailedException(Plugin p, Throwable reason) {
		super("Plugin " + p.getName() + " failed to initialize. Reason: " + reason);
		plugin = p;
	}

	/**
	 * @param p failed plugin
	 * @param reasonText the reason for failure
	 * @param reasonThrowable the {@link Throwable} that caused the failure
	 */
	public InitializationFailedException(Plugin p, String reasonText, Throwable reasonThrowable) {
		super("Plugin " + p.getName() + " failed to initialize. Reason: " + reasonText + " -- " + reasonThrowable);
		plugin = p;
	}

}
