/*	
 * MinimumEngineVersion.java 	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
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
 * Declares the minimum version of the engine 
 * required by this plugin.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public @interface MinimumEngineVersion {

	String value();
}
