/*	
 * PluginCompositionAPI.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.absstorage;

import java.util.Set;

import org.coreasm.engine.plugin.Plugin;

/** 
 * Interface providing all composition related actions/services which a plugin may/should have
 * access to.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public interface PluginCompositionAPI {

	/**
	 * Provides a set of all locations listed in the composition multisets.
	 * 
	 * @return set of affected locations
	 */
	public Set<Location> getAffectedLocations();
	
	/**
	 * Returns <code>true</code> if the given location is updated by the 
	 * given actions in the given update multiset (first or second);
	 * <code>false</code> otherwise.
	 * 
	 * @param setIndex 1 or 2, refers to composition update multiset
	 * @param l location 
	 * @param action list of actions
	 */
	public boolean isLocUpdatedWithActions(int setIndex, Location l, String ... action);

	/**
	 * Returns <code>true</code> if the given location is updated at all
	 * in the given composition multiset (1 or 2).
	 *  
	 * @param setIndex 1 or 2, refers to composition update multiset
	 * @param l location 
	 */
	public boolean isLocationUpdated(int setIndex, Location l);

	/** 
	 * Provides a multiset of all the updates on the given location,
	 * in the given composition update multiset.
	 * 
	 * @param setIndex 1 or 2, refers to composition update multiset
	 * @param l location 
	 */
	public UpdateMultiset getLocUpdates(int setIndex, Location l);
	
	/**
	 * Returns all the updates in the first or second multiset.
	 * 
	 * @param setIndex 1 or 2, refers to composition update multiset
	 */
	public UpdateMultiset getAllUpdates(int setIndex);
	
	/**
	 * Add an update the result of composition.
	 */
	public void addComposedUpdate(Update update, Plugin plugin);
}
