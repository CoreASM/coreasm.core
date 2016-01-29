package org.coreasm.engine.absstorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coreasm.engine.plugin.Plugin;

/** 
 *	Provide composition related services to the engine and to the plugins, but
 *  encapsulate all composition and datastructure specific information in this object.
 *   
 * @author Roozbeh Farahbod, Michael Stegmaier
 * 
 */
public class CompositionAPIImp implements EngineCompositionAPI,
		PluginCompositionAPI {

	protected UpdateMultiset[] updates = new UpdateMultiset[3];
	protected List<UpdatePluginPair> composedUpdates = new ArrayList<UpdatePluginPair>();
	protected boolean affectedLocationsComputed;
	protected Map<String, Set<Location>> actionLocations1;
	protected Map<String, Set<Location>> actionLocations2;
	protected Map<Location, UpdateMultiset> locUpdates1;
	protected Map<Location, UpdateMultiset> locUpdates2;
	
	public void setUpdateInstructions(UpdateMultiset updates1, UpdateMultiset updates2) {
		this.updates[1] = new UpdateMultiset(updates1);
		this.updates[2] = new UpdateMultiset(updates2);
		actionLocations1 = new HashMap<String, Set<Location>>();
		actionLocations2 = new HashMap<String, Set<Location>>();
		locUpdates1 = new HashMap<Location, UpdateMultiset>();
		locUpdates2 = new HashMap<Location, UpdateMultiset>();
		affectedLocationsComputed = false;
	}

	public UpdateMultiset getComposedUpdates() {
		UpdateMultiset result = new UpdateMultiset();
		
		for (UpdatePluginPair pair: composedUpdates)
			result.add(pair.update);
		
		return result;
	}

	public Set<Location> getAffectedLocations() {
		if (!affectedLocationsComputed) {
			locUpdates1 = new HashMap<Location, UpdateMultiset>();
			for (Update u: updates[1]) {
				UpdateMultiset locUpdates = locUpdates1.get(u.loc);
				if (locUpdates == null) {
					locUpdates = new UpdateMultiset();
					locUpdates1.put(u.loc, locUpdates);
				}
				locUpdates.add(u);
				if (!Update.UPDATE_ACTION.equals(u.action)) {
					Set<Location> locations = actionLocations1.get(u.action);
					if (locations == null) {
						locations = new HashSet<Location>();
						actionLocations1.put(u.action, locations);
					}
					locations.add(u.loc);
				}
			}
			locUpdates2 = new HashMap<Location, UpdateMultiset>();
			for (Update u: updates[2]) {
				UpdateMultiset locUpdates = locUpdates2.get(u.loc);
				if (locUpdates == null) {
					locUpdates = new UpdateMultiset();
					locUpdates2.put(u.loc, locUpdates);
				}
				locUpdates.add(u);
				if (!Update.UPDATE_ACTION.equals(u.action)) {
					Set<Location> locations = actionLocations2.get(u.action);
					if (locations == null) {
						locations = new HashSet<Location>();
						actionLocations2.put(u.action, locations);
					}
					locations.add(u.loc);
				}
			}
			affectedLocationsComputed = true;
		}
		Set<Location> affectedLocations = new HashSet<Location>(locUpdates1.keySet());
		affectedLocations.addAll(locUpdates2.keySet());

		return affectedLocations;
	}

	public UpdateMultiset getLocUpdates(int setIndex, Location l) {
		UpdateMultiset locUpdates;
		Map<Location, UpdateMultiset> locUpdateMap;
		
		if (setIndex == 1)
			locUpdateMap = locUpdates1;
		else if (setIndex == 2)
			locUpdateMap = locUpdates2;
		else
			return null;
		
		locUpdates = locUpdateMap.get(l);
		if (locUpdates == null) {
			locUpdates = new UpdateMultiset();
			locUpdateMap.put(l, locUpdates);
			for (Update u: updates[setIndex]) 
				if (u.loc.equals(l))
					locUpdates.add(u);
		}
		
		return locUpdates;
	}
	
	private Set<Location> getActionLocations(int setIndex, String action) {
		Set<Location> locations;
		Map<String, Set<Location>> actionLocationsMap;
		
		if (setIndex == 1)
			actionLocationsMap = actionLocations1;
		else if (setIndex == 2)
			actionLocationsMap = actionLocations2;
		else
			return null;
		
		locations = actionLocationsMap.get(action);
		if (locations == null) {
			locations = new HashSet<Location>();
			actionLocationsMap.put(action, locations);
			for (Update u : updates[setIndex]) {
				if (u.action.equals(action))
					locations.add(u.loc);
			}
		}
		
		return locations;
	}

	public boolean isLocUpdatedWithActions(int setIndex, Location l, String... action) {
		for (String act: action) {
			if (getActionLocations(setIndex, act).contains(l))
				return true;
		}
		
		return false;
	}

	public boolean isLocationUpdated(int setIndex, Location l) {
		if (!affectedLocationsComputed)
			getAffectedLocations();
		return setIndex == 1 && locUpdates1.containsKey(l) || setIndex == 2 && locUpdates2.containsKey(l);
	}

	public UpdateMultiset getAllUpdates(int setIndex) {
		return updates[setIndex];
	}

	public void addComposedUpdate(Update update, Plugin plugin) {
		composedUpdates.add(new UpdatePluginPair(update, plugin));
	}

	/**
	 * A container class to hold a pair of update and plugin.
	 * 
	 * @author Roozbeh Farahbod
	 */
	private class UpdatePluginPair {
		public final Update update;
		//private final Plugin plugin;
		
		public UpdatePluginPair(Update u, Plugin p) {
			this.update = u;
			//this.plugin = p;
		}
	}
}
