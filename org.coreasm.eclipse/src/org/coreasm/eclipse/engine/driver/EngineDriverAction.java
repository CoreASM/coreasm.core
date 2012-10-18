/*
 * EngineDriverAction.java 		$Revision: 8 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-01-28 03:32:43 -0500 (Wed, 28 Jan 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.eclipse.engine.driver;

/**
 * Actions dealing with {@link EngineDriver} should implement this method.
 *   
 * @author Roozbeh Farahbod
 *
 */

public interface EngineDriverAction {
	
	/**
	 * This method is called by the engine driver to update
	 * the status of the action.
	 */
	public void update(EngineDriver.EngineDriverStatus newStatus);

}
