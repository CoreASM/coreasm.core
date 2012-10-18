/*	
 * PluginAggregationAPI.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Mashaal Memon
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
 *	Interface providing all aggregation related actions/services which a plugin may/should have
 *  access to.
 *   
 *  @author  Mashaal Memon
 *  
 */
public interface PluginAggregationAPI {
	
	/** Instruction Flags. */
	public enum Flag {SUCCESSFUL, FAILED};
	
	/**
	 * Get all locations on which an instruction, with any one of the given actions, is operating.
	 * 
	 * @param actions any number of actions.
	 * 
	 * @return a <code>Set</code> of <code>Location</code> representing locations on which
	 * any one of more of the given actions is operating.
	 */
	Set<Location> getLocsWithAnyAction(String ... actions);
	
	/**
	 * Get all locations on which all instructions operating on the location, have only the given 
	 * action.
	 * 
	 * @return a <code>Set</code> of <code>Location</code> representing locations on which
	 * only the given action is done on said location.
	 */
	Set<Location> getLocsWithActionOnly(String action);
	
	/**
	 * Get all update instructions made to the given location.
	 * 
	 * @return a <code>UpdateMultiset</code> of <code>Update</code> representing all update instructions
	 * made on the given location.
	 */
	UpdateMultiset getLocUpdates(Location loc);
	
	/**
	 * Regular update affects this location.
	 * 
	 * @param loc is the <code>Location</code> to check.
	 * 
	 * @return a <code>boolean</code> value of true if a regular update affects this location, and false
	 * otherwise.
	 */
	boolean regularUpdatesAffectsLoc(Location loc);
	
	/**
	 * Regular updates on this location conflict, and are therefore inconsistent.
	 * 
	 * @param loc is the <code>Location</code> to check.
	 * 
	 * @return a <code>boolean</code> value of true if regular updates to this location conflict and thus 
	 * are inconsistent, and false otherwise.
	 */
	boolean inconsistentRegularUpdatesOnLoc(Location loc);
	
	/**
	 * Flag a given update, noting which plug-in flagged said update
	 * 
	 * @param update the update being flagged.
	 * @param flag what to flag the given upate.
	 * @param plugin which plugin is flagging the update.
	 */
	void flagUpdate(Update update, Flag flag, Plugin plugin);
	
	/**
	 * Handle inconsistent aggregation for this location, by flagging all updates of the location
	 * as failed.
	 * 
	 * @param loc the location who's aggregation has failed.
	 * @param plugin which plugin is flagging the update.
	 */
	void handleInconsistentAggregationOnLocation(Location loc, Plugin plugin);
	
	/**
	 * Add a resultant update to the set of resultant updates resulting from aggregation.
	 * 
	 * @param update the resultant update produced by a plugin.
	 * @param plugin the plugin producing the resultant update.
	 */
	void addResultantUpdate(Update update, Plugin plugin);
	
	

}
