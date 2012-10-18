/*
 * UpdateExplorer.java 		$Revision: 9 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.jasmine.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.jasmine.plugin.JasmineUpdateElement.Type;
import org.coreasm.util.HashMultiset;
import org.coreasm.util.Multiset;

/**
 * A utility class to help working on Jasmine update multisets.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class UpdateExplorer {
	
	private final UpdateMultiset baseSet;
	
	public final List<JasmineUpdateElement> updates = new ArrayList<JasmineUpdateElement>();
	public final HashMap<Location, Multiset<JasmineUpdateElement>> createLocations = new HashMap<Location, Multiset<JasmineUpdateElement>>();  
	public final HashMap<Location, Multiset<JasmineUpdateElement>> invokeLocations = new HashMap<Location, Multiset<JasmineUpdateElement>>();  
	public final HashMap<JObjectElement, Multiset<JasmineUpdateElement>> storeLocations = new HashMap<JObjectElement, Multiset<JasmineUpdateElement>>();  

	/**
	 * Creates a new Update Explorer based on the given
	 * multiset of updates on the jasmine channel. 
	 * 
	 * @param baseSet
	 */
	public UpdateExplorer(UpdateMultiset baseSet) {
		this.baseSet = baseSet;
		expandUpdates();
		createViews();
	}
	
	/*
	 * expanding jasmine updates into a list 
	 * which is one realization of the partial order between
	 * the updates
	 */
	private void expandUpdates() {
		for (Update u: baseSet) 
			if (u.action.equals(JasminePlugin.JASMINE_UPDATE_ACTION)) 
				if (u.value instanceof JasmineAbstractUpdateElement)
					addToUpdateList((JasmineAbstractUpdateElement)u.value);
	}

	/*
	 * recursively adds the updates to the list
	 */
	private void addToUpdateList(JasmineAbstractUpdateElement update) {
		// if it's a collection of updates
		if (update instanceof JasmineUpdateContainer) {
			JasmineUpdateContainer juc = (JasmineUpdateContainer) update;
			for (JasmineAbstractUpdateElement jaue: juc.updateElements)
				addToUpdateList(jaue);
		}
		if (update instanceof JasmineUpdateElement)
			updates.add((JasmineUpdateElement)update);
		// there should be no other form
	}
	
	/*
	 * create different views of the updates
	 */
	private void createViews() {
		for (JasmineUpdateElement jue: updates) { 
			if (jue.type == Type.Create)
				addLocUpdatePair(createLocations, jue.getCoreASMLocation(), jue);
			else
			if (jue.type == Type.Invoke)
				addLocUpdatePair(invokeLocations, jue.getCoreASMLocation(), jue);
			else
			if (jue.type == Type.Store)
				addJObjUpdatePair(storeLocations, jue.getStoreObject(), jue);
		}
	}

	private void addLocUpdatePair(HashMap<Location, Multiset<JasmineUpdateElement>> map, Location loc, JasmineUpdateElement u) {
		Multiset<JasmineUpdateElement> set = map.get(loc);
		if (set == null) {
			set = new HashMultiset<JasmineUpdateElement>();
			map.put(loc, set);
		}
		set.add(u);
	}

	private void addJObjUpdatePair(HashMap<JObjectElement, Multiset<JasmineUpdateElement>> map, JObjectElement jobj, JasmineUpdateElement u) {
		Multiset<JasmineUpdateElement> set = map.get(jobj);
		if (set == null) {
			set = new HashMultiset<JasmineUpdateElement>();
			map.put(jobj, set);
		}
		set.add(u);
	}
}
