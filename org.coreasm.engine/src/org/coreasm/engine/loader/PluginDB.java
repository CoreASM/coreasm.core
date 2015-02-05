/*	
 * PluginDB.java 	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.Plugin;

/** 
 * A structure to hold CoreASM plugins. This is created to 
 * improve performance.
 *   
 * @author Roozbeh Farahbod
 * 
 */
@SuppressWarnings("serial")
public class PluginDB extends HashSet<Plugin> {

	public final Set<ExtensionPointPlugin> extensionPointPlugins;
	
	public final Map<EngineMode, List<Entry<ExtensionPointPlugin, Integer>>> srcModeMap;
	public final Map<EngineMode, List<Entry<ExtensionPointPlugin, Integer>>> trgModeMap;
	public final Map<EngineMode, Map<EngineMode, Set<ExtensionPointPlugin>>> modePairMap;
	
	private final EntryComparator comparator = new EntryComparator();
	
	/**
	 * 
	 */
	public PluginDB() {
		extensionPointPlugins = new HashSet<ExtensionPointPlugin>();
		srcModeMap = new HashMap<EngineMode, List<Entry<ExtensionPointPlugin, Integer>>>();
		trgModeMap = new HashMap<EngineMode, List<Entry<ExtensionPointPlugin, Integer>>>();
		modePairMap = new HashMap<EngineMode, Map<EngineMode,Set<ExtensionPointPlugin>>>();
	}

	/**
	 * @param c
	 */
	public PluginDB(Collection<Plugin> c) {
		super(c);
		extensionPointPlugins = new HashSet<ExtensionPointPlugin>();
		srcModeMap = new HashMap<EngineMode, List<Entry<ExtensionPointPlugin, Integer>>>();
		trgModeMap = new HashMap<EngineMode, List<Entry<ExtensionPointPlugin, Integer>>>();
		modePairMap = new HashMap<EngineMode, Map<EngineMode,Set<ExtensionPointPlugin>>>();
		for (Plugin p: c) {
			if (p instanceof ExtensionPointPlugin)
				addEPPlugin((ExtensionPointPlugin)p);
		}
	}

	/**
	 * Returns a subset of this set that only includes the 
	 * extension points plugins.
	 */
	public Set<ExtensionPointPlugin> getExtensionPointPlugins() {
		return extensionPointPlugins;
	}

	/**
	 * Returns a list of (plugin, priority) pairs that are registered for
	 * the given source mode. The list is ordered according to 
	 * the call priorities. 
	 * 
	 * The return value can be <code>null</code>.
	 */
	public List<Entry<ExtensionPointPlugin, Integer>> getSrcModePlugins(EngineMode mode) {
		List<Entry<ExtensionPointPlugin, Integer>> result = srcModeMap.get(mode);
		if (result == null)
			return Collections.emptyList();
		else {
			Collections.sort(result, comparator);
			return result;
		}
	}
	
	/**
	 * Returns a list of (plugin, priority) pairs that are registered for
	 * the given target mode. The list is ordered according to 
	 * the call priorities. 
	 * 
	 * The return value can be <code>null</code>.
	 */
	public List<Entry<ExtensionPointPlugin, Integer>> getTrgModePlugins(EngineMode mode) {
		List<Entry<ExtensionPointPlugin, Integer>> result = trgModeMap.get(mode);
		if (result == null)
			return Collections.emptyList();
		else {
				Collections.sort(result, comparator);
				return result;
			}
	}
	
	/*
	 * Returns the x of ExtensionPointPlugins that are registered for 
	 * the given src and trg mode. It caches the results;
	 *
	public Set<ExtensionPointPlugin> getExtensionPointPlugins(EngineMode src, EngineMode trg) {
		//FIXME problamatic! This is not dynamic anymore!
		
		Map<EngineMode, Set<ExtensionPointPlugin>> map = modePairMap.get(src);
		if (map == null) {
			map = new HashMap<EngineMode, Set<ExtensionPointPlugin>>();
			modePairMap.put(src, map);
		}
		Set<ExtensionPointPlugin> set = map.get(trg);
		if (set == null) {
			set = new HashSet<ExtensionPointPlugin>();
			set.addAll(getSrcModePlugins(src));
			set.addAll(getTrgModePlugins(trg));
			map.put(trg, set);
			return set;
		}
		
		return set;
	}
	*/
	
	// ------- Internal Methods ---------
	
	/*
	 * Adds the given ExtensionPointPlugin to the internal 
	 * cache of extension point plugins.
	 */
	private boolean addEPPlugin(ExtensionPointPlugin p) {
		putInModeMap(p);
		return extensionPointPlugins.add(p);
	}
	
	/*
	 * removes the given ExtensionPointPlugin from the internal 
	 * cache of extension point plugins.
	 */
	private boolean removeEPPlugin(ExtensionPointPlugin p) {
		throw new UnsupportedOperationException("This operation is not currently supported.");
		/*
		removeFromModeMap(p);
		return extensionPointPlugins.remove(p);
		*/
	}
	
	/*
	 * Clears the set of ExtensionPoint plugins
	 */	
	private void clearEPs() {
		srcModeMap.clear();
		trgModeMap.clear();
		modePairMap.clear();
		extensionPointPlugins.clear();
	}
	
	/*
	 * Removes all the given plugins from the internal
	 * cache of EP plugins.
	 */
	private boolean removeAllEPs(Collection<?> c) {
		throw new UnsupportedOperationException("This operation is not currently supported.");
		/*
		for (Object o: c) {
			if (o instanceof ExtensionPointPlugin) {
				ExtensionPointPlugin p = (ExtensionPointPlugin)o;
				removeFromModeMap(p);
			}
		}
		return extensionPointPlugins.removeAll(c);
		*/
	}

	/*
	 * Modifies the mode map cache according to the new
	 * plugin 'p'.
	 */
	private void putInModeMap(ExtensionPointPlugin p) {
		// source modes
		if (p.getSourceModes() != null)
			for (Entry<EngineMode, Integer> pair: p.getSourceModes().entrySet()) {
				List<Entry<ExtensionPointPlugin, Integer>> list = srcModeMap.get(pair.getKey());
				// if set is not there, create it
				if (list == null) {
					list = new ArrayList<Entry<ExtensionPointPlugin, Integer>>();
					srcModeMap.put(pair.getKey(), list);
				}
				list.add(new PluginEntry(p, pair.getValue()));
			}

		// target modes
		if (p.getTargetModes() != null)
			for (Entry<EngineMode, Integer> pair: p.getTargetModes().entrySet()) {
				List<Entry<ExtensionPointPlugin, Integer>> list = trgModeMap.get(pair.getKey());
				// if set is not there, create it
				if (list == null) {
					list = new ArrayList<Entry<ExtensionPointPlugin, Integer>>();
					trgModeMap.put(pair.getKey(), list);
				}
				list.add(new PluginEntry(p, pair.getValue()));
			}
		
	}
	
	/*
	 * Removes the modes of p from mode maps.
	 *
	private void removeFromModeMap(ExtensionPointPlugin p) {
		// source modes
		if (p.getSourceModes() != null)
			for (Entry<EngineMode, Integer> pair: p.getSourceModes().entrySet()) {
				List<Entry<ExtensionPointPlugin, Integer>> list = srcModeMap.get(pair.getKey());
				if (list != null)
					list.remove(new PluginEntry(p, pair.getValue()));
			}

		// target modes
		if (p.getTargetModes() != null)
			for (Entry<EngineMode, Integer> pair: p.getTargetModes().entrySet()) {
				List<Entry<ExtensionPointPlugin, Integer>> list = trgModeMap.get(pair.getKey());
				if (list != null) 
					list.remove(new PluginEntry(p, pair.getValue()));
			}
	}
	*/

	// ------- HashSet Methods ---------
	
	@Override
	public boolean add(Plugin o) {
		if (o instanceof ExtensionPointPlugin)
			addEPPlugin((ExtensionPointPlugin)o);
		return super.add(o);
	}

	@Override
	public void clear() {
		super.clear();
		clearEPs();
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof ExtensionPointPlugin)
			removeEPPlugin((ExtensionPointPlugin)o);
		return super.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		removeAllEPs(c);
		return super.removeAll(c);
	}

}

class ModePair {
	final EngineMode src;
	final EngineMode trg;
	
	public ModePair(EngineMode src, EngineMode trg) {
		this.src = src;
		this.trg = trg;
	}
}

class EntryComparator implements Comparator<Entry<ExtensionPointPlugin, Integer>> {

	/**
	 * @see Comparator#compare(Object, Object)
	 * 
	 * Note: this comparator
     * imposes orderings that are inconsistent with equals.
	 */
	public int compare(Entry<ExtensionPointPlugin, Integer> o1,
			Entry<ExtensionPointPlugin, Integer> o2) {
		return o2.getValue() - o1.getValue();
	}
	
}

class PluginEntry implements Entry<ExtensionPointPlugin, Integer> {

	public final ExtensionPointPlugin key;
	public final Integer value;
	
	public PluginEntry(ExtensionPointPlugin p, Integer i) {
		this.key = p;
		this.value = i;
	}
	
	public ExtensionPointPlugin getKey() {
		return key;
	}

	public Integer getValue() {
		return value;
	}

	public Integer setValue(Integer value) {
		throw new UnsupportedOperationException();
	}
	
}
