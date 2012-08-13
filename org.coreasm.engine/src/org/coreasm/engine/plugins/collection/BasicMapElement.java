/*
 * BasicMapElement.java 		$Revision: 243 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;

/**
 * This class provides the foundation for enumerable elements that are 
 * essentially a map.
 *   
 * @author Roozbeh Farahbod
 * 
 * @deprecated Should not be used.
 */
@Deprecated
public abstract class BasicMapElement extends AbstractMapElement {

    /**
     * Location-value table of this map.
     * 
     */
    protected HashMap<Element, Element> table = new HashMap<Element, Element>();
        
	/**
	 * Creates a new abstract map element. 
	 */
	public BasicMapElement() {
		super();
	}

    public int size() {
    	return table.size();
    }

    /**
     * Size of this map as an integer.
     */
    public int intSize() {
    	return table.size();
    }
    
    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
    	return table.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.  
     * 
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *         key.
     * 
     * @see Map#containsKey(Object)
     */
    public boolean containsKey(Element key) {
    	return table.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.  
     * 
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     * @see Map#containsValue(Object)
     */
    public boolean containsValue(Element value) {
    	return table.containsValue(value);
    }

    /**
     * Returns the value to which this map maps the specified key. 
     * Returns <code>undef</code> if there is no such value.  
     * 
     * @see Element#UNDEF
     * @see Map#get(Object)
     */
    public Element get(Element key) {
		Element result = table.get(key);
		if (result == null) 
			result = defaultValue;
		return result;
    }

    // Modification Operations

    /**
     * Associates the specified value with the specified key in this map
     * only if this map is modifiable.
     * Returns <code>undef</code> if there is no such value.  
     * 
     * @throws UnmodifiableFunctionException if this map is not modifiable. 
     *
     * @see #get(Element)
     * @see Map#put(Object, Object)
     * @see Element#UNDEF
     */
    public Element put(Element key, Element value) throws UnmodifiableFunctionException {
    	if (!this.isModifiable())
    		throw new UnmodifiableFunctionException("Map is not modifiable.");
    	
		Element result = get(key);
		table.put(key, value);
		return result;
    }

    /**
     * Removes the mapping for this key from this map if it is present
     * and if this map is modifiable.
     * 
     * <p>Returns the value to which the map previously associated the key, or
     * <tt>undef</tt> if the map contained no mapping for this key.
     * 
     * @throws UnmodifiableFunctionException if this map is not modifiable. 
     *
     * @see Map#remove(Object)
     * @see Element#UNDEF
     */
    public Element removeKey(Element key) throws UnmodifiableFunctionException {
    	if (!this.isModifiable())
    		throw new UnmodifiableFunctionException("Map is not modifiable.");
    	
		Element result = get(key);
    	table.remove(key);
		return result;
    }

    /**
     * Removes all mappings from this map if this map is modifiable.
     *
     * @throws UnmodifiableFunctionException if this map is not modifiable. 
     *
     * @throws UnsupportedOperationException clear is not supported by this
     * 		  map.
     */
    public void clear() throws UnmodifiableFunctionException {
    	if (!this.isModifiable())
    		throw new UnmodifiableFunctionException("Map is not modifiable.");
    	
    	table.clear();
    }

    // Views

    /**
     * Returns a set view of the keys contained in this map.  The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa.  
     * 
     * @return a set view of the keys contained in this map.
     * 
     * @see Map#keySet()
     */
    public Set<? extends Element> keySet() {
    	return table.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection<? extends Element> values() {
    	return table.values();
    }

    @Override
	public String toString() {
		return super.toString() + ": " + table;
	}

}
