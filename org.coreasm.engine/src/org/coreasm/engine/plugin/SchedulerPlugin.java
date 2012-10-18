/*	
 * SchedulerPlugin.java 	1.0 	$Revision: 243 $
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

import org.coreasm.engine.scheduler.SchedulingPolicy;

/** 
 *	Interface for plugins that extend the Scheduler module.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public interface SchedulerPlugin {

	/**
	 * Returns the scheduling policy provided by this plugin.
	 * 
	 * @return the scheduling policy
	 */
	public abstract SchedulingPolicy getPolicy();

}
