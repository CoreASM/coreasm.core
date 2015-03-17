/*	
 * MapElement.java 	$Revision: 243 $
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
 
package org.coreasm.compiler.plugins.map.include;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Map.Entry;

import org.coreasm.engine.plugins.collection.AbstractListElement;
import org.coreasm.engine.plugins.collection.AbstractMapElement;
import org.coreasm.compiler.plugins.collection.include.ModifiableCollection;
import org.coreasm.engine.plugins.list.ListElement;
import org.coreasm.engine.plugins.map.MapBackgroundElement;

import CompilerRuntime.CoreASMCException;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.Location;

import CompilerRuntime.Rule;

import org.coreasm.engine.absstorage.Update;

import CompilerRuntime.UpdateList;

/** 
 * Map elements
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class MapElement extends AbstractMapElement implements ModifiableCollection {

	protected final Map<Element, Element> map;
	protected Set<Element> keySet = null;
	protected Set<Element> valueSet = null;
	protected Collection<Element> valueCollection = null;
	protected Set<Element> enumeration = null;
	protected List<Element> enumListCache = null;
	
	/**
	 * Initializes an empty map
	 */
	public MapElement() {
		this.map = Collections.unmodifiableMap(new HashMap<Element, Element>());
	}
	
	/**
	 * Initializes a new map with the given elements
	 * @param map A map containing elements
	 */
	public MapElement(Map<? extends Element, ? extends Element> map) {
		this.map = Collections.unmodifiableMap(new HashMap<Element, Element>(map));
	}
	
	/**
	 * Initializes a new map with the given elements
	 * @param anotherMap A map containing elements
	 */
	public MapElement(MapElement anotherMap) {
		this(anotherMap.map);
	}
	
	
	@Override
	public String getBackground() {
		return MapBackgroundElement.NAME;
	}

	@Override
	public boolean containsKey(Element key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Element value) {
		return map.containsValue(value);
	}

	@Override
	public Element get(Element key) {
		Element result = map.get(key);
		if (result == null) 
			result = defaultValue;
		return result;
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<Element> keySet() {
		if (keySet == null) {
			keySet = Collections.unmodifiableSet(map.keySet());
		}
		return keySet;
	}

	public int size() {
		return map.size();
	}

	/**
	 * The size of this map in integer.
	 * @return The size of the map
	 */
	public int intSize() {
		return map.size();
	}

	@Override
	public Collection<Element> values() {
		if (valueCollection == null) {
			valueCollection = Collections.unmodifiableCollection(map.values());
		}
		return valueCollection;
	}

	public boolean contains(Element e) {
		if (enumeration == null) 
			enumerate();
		return enumeration.contains(e);
	}

	public Collection<Element> enumerate() {
		if (enumeration == null) {
			enumeration = new HashSet<Element>();
			for (Entry<Element, Element> entry: map.entrySet()) {
				enumeration.add(new ListElement(entry.getKey(), entry.getValue()));
			}
		} 
		return enumeration;
	}

	@Override
	public String denotation() {
		if (intSize() == 0) 
			return "{ -> }";
		else {
			StringBuffer result = new StringBuffer("{");
			
			for (Element k: map.keySet())
				result.append(k.denotation() + "->" + map.get(k).denotation() + ", ");
			
			return result.substring(0, result.length() - 2).toString() + "}";
		}
	}

	@Override
	public String toString() {
		if (intSize() == 0) 
			return "{ -> }";
		else {
			StringBuffer result = new StringBuffer("{");
			
			for (Element k: map.keySet())
				result.append(k.toString() + "->" + map.get(k) + ", ");
			
			return result.substring(0, result.length() - 2).toString() + "}";
		}
	}

	/**
	 * @return returns the values-set of this map.
	 */
	@Override
	public Set<Element> getRange() {
		if (valueSet == null) {
			valueSet = Collections.unmodifiableSet(new HashSet<Element>(this.values()));
		}
		return valueSet;
	}

	@Override
	public AbstractMapElement getNewInstance(Map<? extends Element, ? extends Element> map) {
		return new MapElement(map);
	}

	/**
	 * Creates a new map element with the given collection of 
	 * key-value pairs in form of {@link AbstractListElement AbstractListElements} 
	 * of size 2.
	 * 
	 * @throws IllegalArgumentException if the collection is not as specified above 
	 */
	@Override
	public AbstractMapElement getNewInstance(Collection<? extends Element> collection) {
		Map<Element, Element> map = new HashMap<Element, Element>();
		for (Element e: collection) {
			if (e instanceof AbstractListElement 
					&& ((AbstractListElement)e).size() == 2) {
				final AbstractListElement pair = (AbstractListElement)e;
				map.put(pair.head(), pair.last());
			} else
				throw new IllegalArgumentException("Cannot create a new map from the given collection.");
		}
		return new MapElement(map);
	}

	@Override
	public Map<Element, Element> getMap() {
		return Collections.unmodifiableMap(map);
	}

	public List<Element> getIndexedView() throws UnsupportedOperationException {
		if (enumListCache == null)
			enumListCache = Collections.unmodifiableList(new ArrayList<Element>(enumerate()));
		return enumListCache;
	}

	public boolean supportsIndexedView() {
		return true;
	}

	@Override
	public UpdateList computeAddUpdate(Location loc, Element e, Rule agent) throws CoreASMCException {
		if (e instanceof AbstractMapElement) {
			HashMap<Element, Element> tempMap = new HashMap<Element, Element>(this.map);
			tempMap.putAll(((AbstractMapElement)e).getMap());
			MapElement newMap = new MapElement(tempMap);
			Update u = new Update(loc, newMap, Update.UPDATE_ACTION, agent, null);
			return new UpdateList(u);
		} else 
			throw new CoreASMCException("Cannot add non-map elements to a map."); 
	}

	/**
	 * If<br>
	 * 1) <code>e</code> is an instance of {@link MapElement}, removes the exact 
	 * key-value pairs of <code>e</code> from this map element;<br>
	 * 2) <code>e</code> is an {@link Enumerable}, removes all the keys from this 
	 * map element that are in <code>e</code>;<br>
	 * 3) <code>e</code> is an {@link Element} (none of the above), if it is a key
	 * in this map, removes it from the map.
	 */
	@Override
	public UpdateList computeRemoveUpdate(Location loc, Element e, Rule agent) throws CoreASMCException {
		HashMap<Element, Element> tempMap = new HashMap<Element, Element>(this.map);
		if (e instanceof MapElement) {
			/*
			 * if the element is a MapElement then remove all the key-value 
			 * pairs in this map element that match those of the given map element.
			 */
			for (Entry<Element, Element> me: ((MapElement)e).map.entrySet()) {
				final Element key = me.getKey();
				if (tempMap.get(key) != null && tempMap.get(key).equals(me.getValue()))
					tempMap.remove(key);
			}
		} else
			if (e instanceof Enumerable) {
				for (Element ei: ((Enumerable)e).enumerate()) {
					tempMap.remove(ei);
				}
			} else
				tempMap.remove(e);
		
		MapElement newMap = new MapElement(tempMap);
		Update u = new Update(loc, newMap, Update.UPDATE_ACTION, agent, null);
		return new UpdateList(u);
	}

}
