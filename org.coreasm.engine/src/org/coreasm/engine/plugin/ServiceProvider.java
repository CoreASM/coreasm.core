/*	
 * ServiceProvider.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author$ on $Date$.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugin;


/**
 * Interface of an inter-plugin service provider.
 * 
 * @author Roozbeh Farahbod
 *
 */
public interface ServiceProvider {

	/**
	 * Responds to a service request.
	 */
	public Object call(ServiceRequest request);

	/**
	 * @return the name of this service provider
	 */
	public String getName();
}
