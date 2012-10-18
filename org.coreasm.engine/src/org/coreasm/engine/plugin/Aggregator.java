/*
 * Aggregator.java 	1.0 	$Revision: 243 $
 * 				
 *
 * Copyright (C) 2005 George Ma
 * Copyright (C) 2006 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugin;

import org.coreasm.engine.absstorage.PluginAggregationAPI;
import org.coreasm.engine.absstorage.PluginCompositionAPI;

/**
 * Aggregator interface to be implemented by plug-ins. 
 *  
 * @author George Ma, Roozbeh Farahbod
 * 
 */
public interface Aggregator {

	/**
	 * Returns an array of update actions provided by this plugin.
	 * This method should NOT return <code>null</code>.
	 */
	public String[] getUpdateActions();
	
	/**
	 * Using the given plug-in aggregation api object, perform aggregation.
	 * 
	 * @param pluginAgg plugin aggregation API for current step/multiset
	 */
	public void aggregateUpdates(PluginAggregationAPI pluginAgg);

	/**
	 * Computes the sequential composition of two update multisets with 
	 * regard to a certain location.
	 * 
	 * @param compAPI an object implementing the composition API 
	 * 
	 */ 
	// This method is revised by Roozbeh Farahbod, 11-Aug-2006
	public void compose(PluginCompositionAPI compAPI);
}
