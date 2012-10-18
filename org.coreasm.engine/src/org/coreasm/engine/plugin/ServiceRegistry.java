/*	
 * ServiceRegistry.java 
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

import java.util.Map;
import java.util.Set;


/**
 * Interface of the service registry functionality of the engine.
 * Plugin can register to offer services (mostly to other plugins)
 * by responding to certain types of service requests.
 * 
 * This is a different service mechanism from the one possible by 
 * providing extensions of {@link PluginServiceInterface}. This service
 * registry does not impose compile-time dependency of plugin classes.
 *  
 * @author Roozbeh Farahbod
 *
 */
public interface ServiceRegistry {

	/**
	 * Registers a service provider for a specific type of service.
	 * 
	 * @param type a {@link String} denotation of a service type
	 * @param provider a service provider
	 */
	public void addServiceProvider(String type, ServiceProvider provider);
	
	/**
	 * Deregisters a service provider for a particular type of service.
	 * 
	 * @param type a {@link String} denotation of a service type
	 * @param provider a service provider
	 */
	public void removeServiceProvider(String type, ServiceProvider provider);
	
	/**
	 * Returns the set of service providers for a particular service type.
	 * 
	 * @param type a {@link String} denotation of a service type
	 */
	public Set<ServiceProvider> getServiceProviders(String type);
	
	/**
	 * Performs a service call.
	 * 
	 * @param sr service request
	 * @param withResults if <code>true</code> gathers the result and returns them in a map
	 * @return the returning result for every service provider; can be empty.
	 * If <code>withResults</code> is false, it returns null.
	 */
	public Map<String, Object> serviceCall(ServiceRequest sr, boolean withResults);
}
