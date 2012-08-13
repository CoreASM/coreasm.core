/*
 * EngineEvent.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine;

/**
 * Interface for engine events.
 * 
 * @author Roozbeh Farahbod
 * 
 */
public abstract class EngineEvent {

	/** Controls whether or not this event is consumed */
	protected boolean consumed;
	
	/**
	 * Consumes this event.
	 *
	 */
	public void consume() {
		consumed = true;
	}
	
	/**
	 * Returns whether this event has been consumed.
	 */
	public boolean isConsumed() {
		return consumed;
	}
	
}

