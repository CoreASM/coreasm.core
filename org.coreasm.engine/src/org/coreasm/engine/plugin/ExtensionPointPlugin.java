/*	
 * ExtensionPointPlugin.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugin;

import java.util.Map;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.CoreASMEngine.EngineMode;

/** 
 *	Interface for plugins that extend the engine's life-cycle.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public interface ExtensionPointPlugin {

	/**
	 * default call priority
	 */
	public static final Integer DEFAULT_PRIORITY = 50;
	
	/**
	 * Returns a map of engine modes to call priorities; 
	 * upon transition of 
	 * the engine mode to any of these modes, the given 
	 * plug-in must be notified.  
	 * 
	 * Zero (0) is the lowest priority and 100 is the highest calling 
	 * priority. The engine will consider this priority when 
	 * calling plug-ins at extension point transitions. 
	 * All plug-ins with the same priority level will
	 * be called in a non-deterministic order.
	 * Default call priority is {@link #DEFAULT_PRIORITY}.
	 * 
	 * @return a map of engine modes to priorities
	 */
	public abstract Map<ControlAPI.EngineMode, Integer> getTargetModes();
	
	/**
	 * Returns a map of engine modes to call priorities; 
	 * upon transition of 
	 * the engine mode from any of these modes, the given 
	 * plug-in must be notified.  
	 * 
	 * Zero (0) is the lowest priority and 100 is the highest calling 
	 * priority. The engine will consider this priority when 
	 * calling plug-ins at extension point transitions. 
	 * All plug-ins with the same priority level will
	 * be called in a non-deterministic order.
	 * Default call priority is {@link #DEFAULT_PRIORITY}.
	 * 
	 * @return a map of engine modes to priorities
 	 */
	public abstract Map<ControlAPI.EngineMode, Integer> getSourceModes();
	
	/**
	 * Is called by the engine whenever the engine mode is changed
	 * from <code>source</code> to <code>target</code>.
	 * 
	 * @param source the source mode
	 * @param target the target mode
	 */
	public abstract void fireOnModeTransition(EngineMode source, EngineMode target) throws EngineException;

}
