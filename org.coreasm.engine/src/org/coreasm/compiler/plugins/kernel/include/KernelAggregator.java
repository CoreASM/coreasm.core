package org.coreasm.compiler.plugins.kernel.include;

import java.util.Set;


import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Update;

import CompilerRuntime.AggregationHelper;
import CompilerRuntime.PluginCompositionAPI;
import CompilerRuntime.UpdateList;

/**
 * Default kernel aggregation
 * @author Spellmaker
 *
 */
public class KernelAggregator implements CompilerRuntime.UpdateAggregator {

	@Override
	public void aggregateUpdates(AggregationHelper ah) {
		// all locations on which basic updates occur
				Set<Location> basicUpdateLocations = ah.getLocsWithActionOnly(Update.UPDATE_ACTION);
				
				for (Location l : basicUpdateLocations) {
					// get all updates on the location
					CompilerRuntime.UpdateList updatesOnLoc = ah.getLocUpdates(l);
					
					// for all updates on the location
					for (Update u : updatesOnLoc)
					{
						// flag the update as successful
						ah.flagUpdate(u,CompilerRuntime.AggregationHelper.Flag.SUCCESSFUL,this);
						
						// resultant update is this update
						ah.addResultantUpdate(u,this);
					}
				}
	}

	@Override
	public void compose(PluginCompositionAPI compAPI) {
		UpdateList updateSet1 = compAPI.getAllUpdates(1);
		UpdateList updateSet2 = compAPI.getAllUpdates(2);
		
		for (Update ui1: updateSet1) {
			if (!locUpdated(updateSet2, ui1.loc) && isBasicUpdate(updateSet1, ui1))
				compAPI.addComposedUpdate(ui1, "KernelAggregator");
		}
		
		for (Update ui2: updateSet2) {
			if (isBasicUpdate(updateSet2, ui2))
				compAPI.addComposedUpdate(ui2, "KernelAggregator");
		}
	}

	private boolean locUpdated(UpdateList uMset, Location l) {
		for (Update u: uMset) 
			if (u.loc.equals(l))
				return true;
		return false;
	}

	private boolean isBasicUpdate(UpdateList uMset, Update u) {
		for (Update update: uMset) 
			if (update.loc.equals(u.loc) && !update.action.equals(Update.UPDATE_ACTION))
				return false;
		return true;
	}
}
