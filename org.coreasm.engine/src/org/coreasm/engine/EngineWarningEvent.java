/*	
 * EngineWarningEvent.java  	$Revision: 7 $
 *
 * Copyright (C) 2009 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine;

import java.util.Date;

/** 
 * Warning events of the engine
 *   
 *  @author  Roozbeh Farahbod
 */
public class EngineWarningEvent extends EngineEvent {

	/** CoreASM warning associated with this event. */
	protected final CoreASMWarning warning;
	
	protected final Date time;
	
	/** 
	 * Creates a new warning event with the given CoreASM error.
	 */
	public EngineWarningEvent(CoreASMWarning w) {
		this.warning = w;
		this.time = new Date();
	}

	/**
	 * Returns the warning associated with this event.
	 */
	public CoreASMWarning getWarning() {
		return warning;
	}
	
	/** 
	 * Returns the time this event created. This is usually LATER than 
	 * the time the actual error occurred.
	 */
	public Date getEventTime() {
		return time;
	}
}
