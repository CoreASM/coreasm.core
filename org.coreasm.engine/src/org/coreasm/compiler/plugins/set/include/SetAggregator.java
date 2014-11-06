package org.coreasm.compiler.plugins.set.include;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import CompilerRuntime.AggregationHelper;
import CompilerRuntime.AggregationHelper.Flag;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;

import CompilerRuntime.PluginCompositionAPI;
import CompilerRuntime.Rule;
import CompilerRuntime.RuntimeProvider;

import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.plugins.set.SetElement;

import CompilerRuntime.UpdateAggregator;
import CompilerRuntime.UpdateList;

public class SetAggregator implements UpdateAggregator{
	public static final String SETADD_ACTION = "setAddAction";
	public static final String SETREMOVE_ACTION = "setRemoveAction";

	@Override
	public void aggregateUpdates(AggregationHelper pluginAgg) {
		// all locations on which contain set incremental updates
				Set<Location> locsToAggregate = pluginAgg.getLocsWithAnyAction(SETADD_ACTION,SETREMOVE_ACTION);
				
				// for all locations to aggregate
				for (Location l : locsToAggregate)
				{
					// if regular update affects this location
					if (pluginAgg.regularUpdatesAffectsLoc(l))
					{
						// Case 1a
						// if regular updates are inconsitent, then aggregation inconsistency
						if (pluginAgg.inconsistentRegularUpdatesOnLoc(l))
							pluginAgg.handleInconsistentAggregationOnLocation(l,this);
						// Case 1b
						// if regular update is not a set, then aggregation inconsistency
						else if (regularUpdateIsNotSet(l,pluginAgg))
							pluginAgg.handleInconsistentAggregationOnLocation(l,this);
						// Case 1c
						// if add/remove are inconsistent with regular update, then aggregation inconsistency
						else if (addRemoveConflictWithRU(l,pluginAgg))
							pluginAgg.handleInconsistentAggregationOnLocation(l,this);
						// otherwise set aggregation can be performed
						else
							// get regular update to add to resultant updates set
							pluginAgg.addResultantUpdate(getRegularUpdate(l,pluginAgg),this);
					}
					// else only partial updates affect this location
					else
					{	// Case 2a
						// if set add/remove failure, then aggregation inconsistency
						if (addRemoveConflict(l,pluginAgg))
							pluginAgg.handleInconsistentAggregationOnLocation(l,this);
						// Case 2b
						// if set not currently at the location, then aggregation inconsistency
						else if (setNotInLocation(l))
							pluginAgg.handleInconsistentAggregationOnLocation(l,this);
						// otherwise set aggregation can be performed
						else
							// get resultant update to add to resultant updates set
							pluginAgg.addResultantUpdate(buildResultantUpdate(l,pluginAgg),this);
					}	
				}	
	}
	
	private boolean regularUpdateIsNotSet(Location loc, AggregationHelper pluginAgg)
	{
		// updates for this location
		UpdateList locUpdates = pluginAgg.getLocUpdates(loc);
		
		// for all updates
		for (Update u : locUpdates)
			// if this update is a regular update
			if (u.action.equals(Update.UPDATE_ACTION))
				if (!(u.value instanceof SetElement))
						return true;
		
		// otherwise return false
		return false;
		
	}
	
	private boolean addRemoveConflictWithRU(Location loc, AggregationHelper pluginAgg)
	{
		// updates for this location
		UpdateList locUpdates = pluginAgg.getLocUpdates(loc);
		
		// the set regular update value
		SetElement ruValue = null; 
		
		// for all updates
		for (Update u : locUpdates)
			// if this update is a regular update
			if (u.action.equals(Update.UPDATE_ACTION))
			{
				// store it
				ruValue = (SetElement)u.value;
				break;
			}
		
		// enumerable view of the RU value 
		Collection<? extends Element> enumerableViewRUValue = ((Enumerable)ruValue).enumerate();
		
		// for all updates
		for (Update u : locUpdates)
			// if there is a set add of a value but RU set does not contain it, there is a conflict
			if (u.action.equals(SETADD_ACTION) && !enumerableViewRUValue.contains(u.value))
				return true;
			// else if there is a set remove of a value but RU set does contain it, there is a conflict
			else if (u.action.equals(SETREMOVE_ACTION) && enumerableViewRUValue.contains(u.value))
				return true;

		// otherwise return false
		return false;
		
	}
	
	private Update getRegularUpdate(Location loc, AggregationHelper pluginAgg)
	{
		// updates for this location
		UpdateList locUpdates = pluginAgg.getLocUpdates(loc);
		
		// regular update to be returned
		Update regularUpdate = null;
		
		
		// all updates added successfully, so flag them
		for (Update u : locUpdates)
		{
			
			// if this update is a regular update and no regular update found to return yet
			if (regularUpdate == null && u.action.equals(Update.UPDATE_ACTION))
				// store it for return to the plugin
				regularUpdate = u;
		
			// flag update aggregation as successful for this update
			pluginAgg.flagUpdate(u,Flag.SUCCESSFUL,this);
		}
		
		// return resultant set
		return regularUpdate;
	}
	
	private boolean addRemoveConflict(Location loc, AggregationHelper pluginAgg)
	{
		// updates for this location
		UpdateList locUpdates = pluginAgg.getLocUpdates(loc);
		
		// get all values of update instruction present in the multiset
		HashSet<Element> updateValues = new HashSet<Element>();
		for (Update u: locUpdates)
			updateValues.add(u.value);
		
		// for each value
		for (Element v : updateValues)
		{
			// if a setAddAction and setRemoveAction to same location and value occur, then conflict
			if (locUpdates.contains(new Update(loc,v,SETADD_ACTION, (Rule)null, null)) 
					&& locUpdates.contains(new Update(loc,v,SETREMOVE_ACTION, (Rule)null, null)))
				return true;
		}
		
		// if we reach this point, that means there's no conflict
		return false;
		
	}
	
	private boolean setNotInLocation(Location loc)
	{
		// get contents of location in question
		Element e;
		try 
		{
			e = RuntimeProvider.getRuntime().getStorage().getValue(loc);
		} catch (InvalidLocationException ex) 
		{
			// Should never happen
			throw new EngineError("Cannot perform  set-add/set-remove actions on a non-set location.");
		}
				
		// if location contains a set, return false
		if (e instanceof SetElement)
			return false;
		// else return true
		else
			return true;	
		
	}
	
	private Update buildResultantUpdate(Location loc, AggregationHelper pluginAgg)
	{
		// updates for this location
		UpdateList locUpdates = pluginAgg.getLocUpdates(loc);
		
		// get set element at current location
		SetElement existingSet;
		try 
		{
			existingSet = (SetElement)RuntimeProvider.getRuntime().getStorage().getValue(loc);
		} catch (InvalidLocationException ex) 
		{
			// Should never happen
			throw new EngineError("Location to which set incremental update has been made is invalid!");
		}
		Enumerable enumerableViewExistingSet = (Enumerable)existingSet;
		
		// resultant set element 
		Set<Element> resultantSet = new HashSet<Element>();
		List<Element> contributingAgents = new ArrayList<Element>();
		//Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();
		//SetElement resultantSet = (SetElement)setBackground.getNewValue();
		
		// add all existing elements less those removed with setRemoveAction
		for (Element e : enumerableViewExistingSet.enumerate()) {
			Update update = new Update(loc, e, SETREMOVE_ACTION, (Rule)null, null);
			if (!locUpdates.contains(update)) 
				resultantSet.add(e);
		}
		
		// add all values resulting from setAddAction
		for (Update u : locUpdates)
			if (u.action.equals(SETADD_ACTION))
				resultantSet.add(u.value);
		
		// all updates added successfully, so flag them
		// and add their agents to the contributing agent set
		for (Update u : locUpdates) {
			pluginAgg.flagUpdate(u,Flag.SUCCESSFUL,this);
			contributingAgents.addAll(u.agents);
			//contributingNodes.addAll(u.sources);
		}
	
		// return resultant set
		return new Update(loc, new SetElement(resultantSet), Update.UPDATE_ACTION, new HashSet<Element>(contributingAgents), null);
	}
	

	@Override
	public void compose(PluginCompositionAPI compAPI) {

		for (Location l: compAPI.getAffectedLocations()) {
			
			boolean isLocUpdatedWithAddRemove_Set1 = 
				compAPI.isLocUpdatedWithActions(1, l, SETADD_ACTION, SETREMOVE_ACTION);
			boolean isLocUpdatedWithAddRemove_Set2 = 
				compAPI.isLocUpdatedWithActions(2, l, SETADD_ACTION, SETREMOVE_ACTION);
			
			// Case 1a
			if (isLocUpdatedWithAddRemove_Set1 && !compAPI.isLocationUpdated(2, l)) {
				for (Update ui: compAPI.getLocUpdates(1, l))
					compAPI.addComposedUpdate(ui, "SetPlugin");
			} else 

				// Case 1b
				if (isLocUpdatedWithAddRemove_Set2 && !compAPI.isLocationUpdated(1, l)) {
					for (Update ui: compAPI.getLocUpdates(2, l))
						compAPI.addComposedUpdate(ui, "SetPlugin");
				} else
					
					// Case 2
					if (isLocUpdatedWithAddRemove_Set2 && compAPI.isLocUpdatedWithActions(2, l, Update.UPDATE_ACTION)) {
						for (Update ui: compAPI.getLocUpdates(2, l))
							compAPI.addComposedUpdate(ui, "SetPlugin");
					} else
						
						// Case 3a
						if (isLocUpdatedWithAddRemove_Set2 &&
								compAPI.isLocUpdatedWithActions(1, l, Update.UPDATE_ACTION)) {
							compAPI.addComposedUpdate(aggregateLocationForComposition(l, compAPI), "SetPlugin");
						} else 
							
							// Case 3b
							if (isLocUpdatedWithAddRemove_Set1 && isLocUpdatedWithAddRemove_Set2) {
								for (Update ui: eradicateConflictingIncrementalUpdates(l, compAPI))
									compAPI.addComposedUpdate(ui, "SetPlugin");
							}
		}
	}
	
	private Update aggregateLocationForComposition(Location l, PluginCompositionAPI compAPI) {
		Element value = null;
		UpdateList uMset1 = compAPI.getLocUpdates(1, l);
		UpdateList uMset2 = compAPI.getLocUpdates(2, l);
		List<Element> contributingAgents = new ArrayList<Element>();
		
		// get the value of the basic update on location 'l'
		// TODO what if there are more than two such updates?
		for (Update ui: uMset1)
			if (ui.action.equals(Update.UPDATE_ACTION)) {
				value = ui.value;
				contributingAgents.addAll(ui.agents);
				break;
			}

		// value should be a set
		if (value instanceof SetElement) {
			Set<Element> resultSet = new HashSet<Element>(((SetElement)value).enumerate());
			
			for (Element e: resultSet) {
				Update removeUpdate = new Update(l, e, SETREMOVE_ACTION, (Rule)null, null);
				
				if (!uMset2.contains(removeUpdate)) 
					resultSet.add(e);
			}
			
			for (Update u: uMset2) {
				if (u.action.equals(SETADD_ACTION)) 
					resultSet.add(u.value);
				contributingAgents.addAll(u.agents);
			}
			
			return new Update(l, new SetElement(resultSet), Update.UPDATE_ACTION, new HashSet<Element>(contributingAgents), null);
		} else
			//logger.error("Value is not a set (in SetPlugin Composition).");
		
		return null;
	}
	
	private UpdateList eradicateConflictingIncrementalUpdates(Location l, PluginCompositionAPI compAPI) {
		UpdateList remainingUpdates = new UpdateList();
		Set<Element> locationValues = new HashSet<Element>();
		UpdateList uMset1 = compAPI.getLocUpdates(1, l);
		UpdateList uMset2 = compAPI.getLocUpdates(2, l);
		
		// Preparing the locationValues set
		for (Update u: uMset1)
			locationValues.add(u.value);
		for (Update u: uMset2)
			locationValues.add(u.value);
			
		for (Element e: locationValues) {
			Update updateAdd = new Update(l, e, SETADD_ACTION, (Rule)null, null);
			Update updateRemove = new Update(l, e, SETREMOVE_ACTION, (Rule)null, null);
			
			// Case 3(b)i
			if (uMset1.contains(updateAdd) && uMset2.contains(updateRemove))
				; // skip
			else
				
				// Case 3(b)ii
				if (uMset1.contains(updateRemove) && uMset2.contains(updateAdd)) 
					// deviating from the spec as there is no need for forall here
					remainingUpdates.add(updateAdd);
				else {
					
					// Case 3(b)iii
					for (Update ui: uMset1)
						if (ui.value.equals(e))
							remainingUpdates.add(ui);
					for (Update ui: uMset2)
						if (ui.value.equals(e))
							remainingUpdates.add(ui);
				}
		}

		return remainingUpdates;
	}

}
