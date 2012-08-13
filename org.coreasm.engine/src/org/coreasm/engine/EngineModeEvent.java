/*	
 * EngineModeEvent.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine;

/** 
 * Engine mode change event
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class EngineModeEvent extends EngineEvent {
	
	/** Old mode of the engine */
	protected final CoreASMEngine.EngineMode oldMode;
	
	/** New mode of the engine */
	protected final CoreASMEngine.EngineMode newMode;

	/**
	 * Creates a new engine mode event with the given old and new modes.
	 */
	public EngineModeEvent(CoreASMEngine.EngineMode oldMode,
			CoreASMEngine.EngineMode newMode) {
		this.oldMode = oldMode;
		this.newMode = newMode;
	}

	/**
	 * @return Returns the newMode.
	 */
	public CoreASMEngine.EngineMode getNewMode() {
		return newMode;
	}

	/**
	 * @return Returns the oldMode.
	 */
	public CoreASMEngine.EngineMode getOldMode() {
		return oldMode;
	}

}
