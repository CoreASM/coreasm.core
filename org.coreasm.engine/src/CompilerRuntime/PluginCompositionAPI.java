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
 
package CompilerRuntime;

import java.util.Set;

public interface PluginCompositionAPI {
	public Set<Location> getAffectedLocations();
	
	public boolean isLocUpdatedWithActions(int setIndex, Location l, String ... action);
	
	public boolean isLocationUpdated(int setIndex, Location l);
	
	public UpdateList getLocUpdates(int setIndex, Location l);
	
	public UpdateList getAllUpdates(int setIndex);
	
	public void addComposedUpdate(Update update, String plugin);
}
