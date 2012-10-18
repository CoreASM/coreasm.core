/*
 * AbstractMapElement.java 		$Revision: 80 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;

/**
 * This class provides the foundation for most of the collection
 * elements such as maps, sets, lists, etc. All these elements
 * are unmodifiable by default.
 *   
 * @author Roozbeh Farahbod
 */

public abstract class AbstractMapElement extends FunctionElement implements Enumerable {

	/**
	 * Creates a new abstract map element. 
	 */
	public AbstractMapElement() {
		super(Element.UNDEF);
		setFClass(FunctionClass.fcStatic);
	}

	/**
	 * Creates a new instance of this element loaded with the given
	 * collection of elements. For maps, this collection should contain
	 * key-value pairs in form of AbstractListElements with only two elements.
	 * 
	 * @param collection a non-null collection of elements
	 *  
	 *  @throws IllegalArgumentException if an element cannot be created given 
	 *  the collection provided.
	 */
	public abstract AbstractMapElement getNewInstance(Collection<? extends Element> collection);
	
	/**
	 * Creates a new AbstractMap element loaded with the given map of elements to elements.
	 */
	public abstract AbstractMapElement getNewInstance(Map<? extends Element, ? extends Element> map);
	
	/**
	 * Returns the value of this map for the given 
	 * list of arguments. If the list has no argument 
	 * or if it has more than one argument, this method 
	 * returns 'undef'.
	 * 
	 * @param args list of arguments
	 * @return the assigned value to the arguments, or 
	 * <code>undef</code> if there is no value
	 * assigned to the given arugments.
	 * 
	 * @see Element#UNDEF
	 */
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 1) {
			return this.getValue(args.get(0));
		} else 
			return defaultValue;
	}

	/**
	 * Returns the contents of this map in a java {@link Map}
	 * instance. If this is not possible, this method
	 * should throw an instance of
	 *  {@link UnsupportedOperationException}.
	 *  
	 *  @throws UnsupportedOperationException
	 */
	public abstract Map<Element,Element> getMap();

	// Elements should not be modifiable
	/*
	 * @see FunctionElement#setValue(List, Element)
	public void setValue(List<Element> args, Element value) throws UnmodifiableFunctionException {
		super.setValue(args, value);
		
		if (args.size() == 1) {
			this.setValue(args.get(0), value);
		} else
			throw new EngineError("MapElement: Cannot set value for multiple keys.");
	}
	*/
	
	public Set<Location> getLocations(String name) {
		Set<Location> locSet = new HashSet<Location>();
		Set<ElementList> argSet = new HashSet<ElementList>();
		for (Element value: keySet())
			argSet.add(ElementList.create(value));
		Location loc = null;
		for (ElementList l : argSet) {
			if (!getValue(l).equals(defaultValue)) {
				loc = new Location(name, l);
				locSet.add(loc);
			}
		}
		return locSet;
	}
	
	/**
	 * Returns the value of this map for the given key.
	 * If there is no value assigned to the argument, 
	 * it should return 'undef'.
	 * 
	 * @param key 
	 * @return the assigned value to the arguments, or 
	 * <code>undef</code> if there is no value
	 * assigned to the given arugments.
	 * 
	 * @see #get(Element)
	 * @see Element#UNDEF
	 */
	public Element getValue(Element key) {
		return this.get(key);
	}
	
	// Elements should not be modifiable
	/*
	 * Sets the value of this map for the given 
	 * key only if this map is modifiable.
	 * 
	 * @param key
	 * @param value the return value
	 * 
	 * @return previous value associated with this key or 
	 * <code>undef</code> if there was no such value.
	 * 
	 * @throws UnmodifiableFunctionException if this map is not modifiable.
	 * @see #put(Element, Element)
	 * @see Element#UNDEF
	 *
	public Element setValue(Element key, Element value) throws UnmodifiableFunctionException{
		return this.put(key, value);
	}
	*/
	
    /**
     * Returns the number of key-value mappings in this map as 
     * a positive number element that holds a natural number which 
     * can be {@link NumberElement#POSITIVE_INFINITY}.
     *
     * @return the number of key-value mappings in this map.
     * @see NumberElement
     */
//    public abstract NumberElement size();

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public abstract boolean isEmpty();

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
    public abstract boolean containsKey(Element key);

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.  
     * 
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     * @see Map#containsValue(Object)
     */
    public abstract boolean containsValue(Element value);

    /**
     * Returns the value to which this map maps the specified key. 
     * Returns <code>undef</code> if there is no such value.  
     * 
     * @see Element#UNDEF
     * @see Map#get(Object)
     */
    public abstract Element get(Element key);

    // Modification Operations
	// Elements should not be modifiable

    /*
     * Elements should not be modifiable
     */
    
    /*
     * Associates the specified value with the specified key in this map
     * only if this map is modifiable.
     * Returns <code>undef</code> if there is no such value.  
     * 
     * @throws UnmodifiableFunctionException if this map is not modifiable. 
     *
     * @see #get(Element)
     * @see Map#put(Object, Object)
     * @see Element#UNDEF
    public abstract Element put(Element key, Element value) throws UnmodifiableFunctionException;

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
    public abstract Element removeKey(Element key) throws UnmodifiableFunctionException;

    /**
     * Removes all mappings from this map if this map is modifiable.
     *
     * @throws UnmodifiableFunctionException if this map is not modifiable. 
     *
     * @throws UnsupportedOperationException clear is not supported by this
     * 		  map.
    public abstract void clear() throws UnmodifiableFunctionException;

	*/
    
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
    public abstract Set<? extends Element> keySet();

    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  
     *
     * @return a collection view of the values contained in this map.
     */
    public abstract Collection<? extends Element> values();

	/**
	 * @return returns the values set of this map.
	 */
	@Override
	public Set<? extends Element> getRange() {
		return new HashSet<Element>(this.values());
	}
    
	/** 
	 * Returns <code>true</code> if:
	 * <ul>
	 * <li>{@link Element#equals(Object)} returns <code>true</code>, or </li>
	 * <li><code>anElement</code> is an instance of <code>AbstractMapElement</code>
	 * and it has the same mapping as this objec.</li>
	 * </ul>
	 * <p>
	 * Otherwise, it returns <code>false</code>.
	 */
	@Override
	public boolean equals(Object anElement) {
		boolean result = false;
		if (super.equals(anElement))
			result = true;
		else {
			if (anElement instanceof AbstractMapElement) {
				AbstractMapElement mapElement = (AbstractMapElement)anElement;
				if (mapElement.size() == this.size()) {
					result = true;
					for (Element key: this.keySet()) 
						if (!this.get(key).equals(mapElement.get(key))) {
							result = false;
							break;
						}
				}
			} 
		}
		
		return result;
	}

	/**
	 * Implements a hashcode function according to the 
	 * contract stated in {@link Object#hashCode()}.
	 */
	@Override
	public int hashCode() {
		int hashCode = 0;
		for (Element e: this.keySet()) 
			hashCode += e.hashCode();
		for (Element e: this.values())
			hashCode += e.hashCode();
		return hashCode;
	}

}
