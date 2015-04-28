/*	
 * EngineCompositionAPI.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package CompilerRuntime;

/** 
 * Interface providing all composition actions/services required by the engine
 * to perform and get information about composition performed by plugins.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public interface EngineCompositionAPI {

	/**
	 * Set the multisets of update instructions which need to be composed.
	 */
	void setUpdateInstructions(UpdateList updates1, UpdateList updates2);

	/**
	 * Return the set of composed updates.
	 */
	UpdateList getComposedUpdates();

}
