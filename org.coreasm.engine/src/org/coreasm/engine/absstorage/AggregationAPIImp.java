/*/*	
 * AggregationAPIImp.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Mashaal Memon
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.coreasm.engine.plugin.Plugin;

/** 
 *	Provide aggregation related services to the engine and to the plugins, but
 *  encapsulate all aggregation and datastructure specific information in this object.
 *   
 *  @author  Mashaal Memon
 *  
 */
public class AggregationAPIImp implements EngineAggregationAPI, PluginAggregationAPI {

	/** Update multiset of update instructions **/
	private UpdateMultiset updates;
	
	/** UpdateMultiset of unprocessed update instructions **/
	private UpdateMultiset unprocessedUpdates;
	
	/** Hashtable from resultant updates to plug-ins **/
	private Hashtable<Update,Set<Plugin>> htResultantUpdatesToPlugins = new Hashtable<Update,Set<Plugin>>();
	
	/** Hashtable from actions to a set of locations **/
	private Hashtable<String,Set<Location>> htActionToLocs = new Hashtable<String,Set<Location>>();
	
	/** Hashtable from location to set of update instructions */
	private Hashtable<Location,UpdateMultiset> htLocToUpdates = new Hashtable<Location,UpdateMultiset>();
	
	/** Hashtable from failed update instructions to vector of plug-ins **/
	private Hashtable<Update, Vector<Plugin>> htFailedUpdatesToPlugin = new Hashtable<Update, Vector<Plugin>>();
	
	/** Hashtable from sucessful update instructions to vector of plug-ins **/
	private Hashtable<Update, Vector<Plugin>> htSuccessfulUpdatesToPlugin = new Hashtable<Update, Vector<Plugin>>();
	
	
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.EngineAggregationAPI#setUpdateInstructions(org.coreasm.engine.absstorage.UpdateMultiset)
	 */
	public void setUpdateInstructions(UpdateMultiset updates) {
		
		// store update multiset produced
		this.updates = updates;
		
		// unprocessed updates begins as a copy of the original update multiset
		unprocessedUpdates = new UpdateMultiset(updates);
		
		// populate the hashtables for quick access to info when plug-ins need it
		populateHashtables();
		

	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.EngineAggregationAPI#isConsistent()
	 */
	public boolean isConsistent() {
		
		// if all instructions processed, and no failed instructions then aggregation completed
		// successfully
		if (getUnprocessedInstructions().size() == 0 && getFailedInstructions().size() == 0)
			return true;
		// else aggregation failed
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.EngineAggregationAPI#getFailedInstructions()
	 */
	public Collection<Update> getFailedInstructions() {
		return htFailedUpdatesToPlugin.keySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.EngineAggregationAPI#getUnprocessedInstructions()
	 */
	public Collection<Update> getUnprocessedInstructions() {
		return unprocessedUpdates.toSet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.EngineAggregationAPI#getResultantUpdates()
	 */
	public Set<Update> getResultantUpdates() {
		
		// set keys of this hashtable is essentailly resultant update set
		return htResultantUpdatesToPlugins.keySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.PluginAggregationAPI#getLocsWithAnyAction(java.lang.String...)
	 */
	public Set<Location> getLocsWithAnyAction(String... actions) {
		
		// collection of locations to be returned
		HashSet<Location> locs = new HashSet<Location>();
		
		// forall actions
		for (String a : actions)
		{
			// if such an action exists in the update multiset
			if (htActionToLocs.containsKey(a))
				// get set of all locations on which action is being taken on that location,
				// and add it to big collection of locations.
				locs.addAll(htActionToLocs.get(a));
		}
		
		return locs;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.PluginAggregationAPI#getLocsWithActionOnly(java.lang.String)
	 */
	public Set<Location> getLocsWithActionOnly(String action) {
		
		// collection of locations to be returned
		HashSet<Location> locs = new HashSet<Location>();

		// get all locations on which the given action is performed
		Set<Location> actionLocs = getLocsWithAnyAction(action);
		
		// for each location which is affected by update instructions doing the given action
		for (Location l : actionLocs)
		{
			// get all updates for this location
			UpdateMultiset locUpdates = getLocUpdates(l);
			
			// counter for number of updates that are of the specific action
			int actionCount = 0;
			
			for (Update u : locUpdates)
			{
				// if the action of this update is equal the action we are looking for
				if (u.action.equals(action))
					// increment counter
					actionCount++;
				else
					// break out of inner loop
					break;
			}
			
			// if all actions affecting the location are the action we are looking for
			if (actionCount == locUpdates.size())
				// add location to locs we are going to return
				locs.add(l);
			
		}
		
		return locs;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.PluginAggregationAPI#getLocUpdates(org.coreasm.engine.absstorage.Location)
	 */
	public UpdateMultiset getLocUpdates(Location loc) {
		return htLocToUpdates.get(loc);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.PluginAggregationAPI#regularUpdatesAffectsLoc(org.coreasm.engine.absstorage.Location)
	 */
	public boolean regularUpdatesAffectsLoc(Location loc)
	{
		UpdateMultiset locUpdates = getLocUpdates(loc);
		
		// if any update action affects given location, return true
		for (Update u : locUpdates)
			if (u.action.equals(Update.UPDATE_ACTION))
				return true;
		
		// otherwise return false
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.PluginAggregationAPI#inconsistentRegularUpdatesOnLoc(org.coreasm.engine.absstorage.Location)
	 */
	public boolean inconsistentRegularUpdatesOnLoc(Location loc)
	{
		UpdateMultiset locUpdates = getLocUpdates(loc);
		
		// for all updates
		for (Update u1 : locUpdates)
			// if this update is a regular update
			if (u1.action.equals(Update.UPDATE_ACTION))
				// for all updates
				for (Update u2 : locUpdates)
					// if we find a second regular update operating on the location with a different value, then regular updates are inconsistent
					if (u2.action.equals(Update.UPDATE_ACTION) && !u1.value.equals(u2.value))
						return true;
		
		// otherwise return false
		return false;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.PluginAggregationAPI#flagUpdate(org.coreasm.engine.absstorage.Update, org.coreasm.engine.absstorage.PluginAggregationAPI.Flag, org.coreasm.engine.Plugin)
	 */
	public void flagUpdate(Update update, Flag flag, Plugin plugin) {
		
		// remove update from unprocessed updates
		unprocessedUpdates.remove(update);
		
		// if update flagged as successful
		if (flag.equals(Flag.SUCCESSFUL))
		{
			// create an entry in hashtable for update if it doesn't exist
			if(!htSuccessfulUpdatesToPlugin.containsKey(update))
				htSuccessfulUpdatesToPlugin.put(update,new Vector<Plugin>());
			
			// add plugin to update
			htSuccessfulUpdatesToPlugin.get(update).add(plugin);
		}
		// else if flagged as failed
		else if (flag.equals(Flag.FAILED))
		{
			// create an entry in hashtable for update if it doesn't exist
			if(!htFailedUpdatesToPlugin.containsKey(update))
				htFailedUpdatesToPlugin.put(update,new Vector<Plugin>());
			
			// add plugin to update
			htFailedUpdatesToPlugin.get(update).add(plugin);
		}


	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.PluginAggregationAPI#handleInconsistentAggregationOnLocation(org.coreasm.engine.absstorage.Location, org.coreasm.engine.Plugin)
	 */
	public void handleInconsistentAggregationOnLocation(Location loc, Plugin plugin)
	{
		UpdateMultiset locUpdates = getLocUpdates(loc);
		
		// for all updates of this location, flag updates as failed
		for (Update u : locUpdates)
			flagUpdate(u,Flag.FAILED,plugin);
		
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.PluginAggregationAPI#addResultantUpdate(org.coreasm.engine.absstorage.Update, org.coreasm.engine.Plugin)
	 */
	public void addResultantUpdate(Update update, Plugin plugin) {
		
		// create an entry in hashtable for update if it doesn't exist
		if(!htResultantUpdatesToPlugins.containsKey(update))
			htResultantUpdatesToPlugins.put(update,new HashSet<Plugin>());
		
		// add plugin to update
		htResultantUpdatesToPlugins.get(update).add(plugin);

	}
	
	//------------------------
	// Private Helper Methods
	//------------------------
	
	private void populateHashtables()
	{
		// for all updateInstructions in the update multiset
		for (Update u : updates)
		{
			//--- add action to loc mapping
			
			// create entry in hashtable for given action if doesn't exist
			if (!htActionToLocs.containsKey(u.action))
				htActionToLocs.put(u.action,new HashSet<Location>());
			
			// add location to action
			htActionToLocs.get(u.action).add(u.loc);
			
			//--- add location to update instruction mapping
			
			// create an entry in hashtable for given location if doesn't exist
			if(!htLocToUpdates.containsKey(u.loc))
				htLocToUpdates.put(u.loc,new UpdateMultiset());
			
			// add update to location
			htLocToUpdates.get(u.loc).add(u);
			
		}
		
	}

}
