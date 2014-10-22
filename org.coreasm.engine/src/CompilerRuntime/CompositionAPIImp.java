/*	
 * CompositionAPIImp.java 	1.0 	$Revision: 243 $
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
 
package CompilerRuntime;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

// TODO The performance of this class in general can be improved. 
/** 
 *	Provide composition related services to the engine and to the plugins, but
 *  encapsulate all composition and datastructure specific information in this object.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class CompositionAPIImp implements EngineCompositionAPI,
		PluginCompositionAPI {

	protected UpdateList[] updates = new UpdateList[3];
	protected List<UpdatePluginPair> composedUpdates = new ArrayList<UpdatePluginPair>();
	
	public void setUpdateInstructions(UpdateList updates1, UpdateList updates2) {
		this.updates[1] = new UpdateList(updates1);
		this.updates[2] = new UpdateList(updates2);
	}

	public UpdateList getComposedUpdates() {
		UpdateList result = new UpdateList();
		
		for (UpdatePluginPair pair: composedUpdates)
			result.add(pair.update);
		
		return result;
	}

	public Set<Location> getAffectedLocations() {
		Set<Location> result = new HashSet<Location>();
		
		for (Update u: updates[1]) 
			result.add(u.loc);
		for (Update u: updates[2]) 
			result.add(u.loc);

		return result;
	}

	public UpdateList getLocUpdates(int setIndex, Location l) {
		UpdateList result = new UpdateList();
		
		for (Update u: updates[setIndex]) 
			if (u.loc.equals(l))
				result.add(u);
		
		return result;
	}

	public boolean isLocUpdatedWithActions(int setIndex, Location l, String... action) {
		// getting updates affecting location 'l'
		UpdateList updates = getLocUpdates(setIndex, l);
		
		for (Update u: updates) 
			for (String act: action) 
				if (u.action.equals(act)) 
					return true;
		
		return false;
	}

	public boolean isLocationUpdated(int setIndex, Location l) {
		return !getLocUpdates(setIndex, l).isEmpty();
	}

	public UpdateList getAllUpdates(int setIndex) {
		return updates[setIndex];
	}

	public void addComposedUpdate(Update update, String plugin) {
		composedUpdates.add(new UpdatePluginPair(update, plugin));
	}

	/**
	 * A container class to hold a pair of update and plugin.
	 * 
	 * @author Roozbeh Farahbod
	 */
	private class UpdatePluginPair {
		public final Update update;
		//public final String plugin;
		
		public UpdatePluginPair(Update u, String p) {
			this.update = u;
			//this.plugin = p;
		}
	}
}
