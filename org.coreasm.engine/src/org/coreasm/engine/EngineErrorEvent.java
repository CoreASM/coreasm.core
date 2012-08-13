/*	
 * EngineErrorEvent.java 	1.0 	$Revision: 243 $
 *
 * Copyright (C) 2006-2009 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine;

import java.util.Date;

/** 
 * Error events of the engine
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class EngineErrorEvent extends EngineEvent {

	/** CoreASM error associated with this event. */
	protected final CoreASMError error;
	
	protected final Date time;
	
	/** 
	 * Creates a new error event with the given CoreASM error.
	 */
	public EngineErrorEvent(CoreASMError error) {
		this.error = error;
		this.time = new Date();
	}

	/**
	 * Returns the exception associated with this event.
	 */
	public CoreASMError getError() {
		return error;
	}
	
	/** 
	 * Returns the time this event created. This is usually LATER than 
	 * the time the actual error occurred.
	 */
	public Date getEventTime() {
		return time;
	}
}
