/*	
 * MapFunction.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package CompilerRuntime;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/** 
 * This class implements of {@link FunctionElement} with <code>HashMap<code> and
 * provides the basic function structure of an ASM state.  
 *   
 *  @author  Roozbeh Farahbod
 *  
 *  @see HashMap
 */
public class MapFunction extends FunctionElement {

    /**
     * Location-value table of this function.
     * 
     */
    protected HashMap<ElementList,Element> table = new HashMap<ElementList,Element>();
        
    @Override
	public String toString() {
		return super.toString() + ": " + table;
	}

	/**
	 * Creates a new map function. 
	 */
	public MapFunction() {
		super();
	}
	
    /**
	 * Creates a new Function with the default value. 
	 */
	public MapFunction(Element defaultValue) {
		super(defaultValue);
	}
	
	/**
	 * Returns the value of this function for the given
	 * list of arguments. 
	 * 
	 * @param args list of arguments
	 * @return the assigned value to the arguments, or 
	 * <code>defaultValue</code> if there is no value
	 * assigned to the given arugments.
	 * @see #defaultValue
	 */
	public Element getValue(List<? extends Element> args) {
		ElementList el;
		if (args instanceof ElementList)
			el = (ElementList)args;
		else
			el = ElementList.create(args);
		Element temp = table.get(el);
		if (temp == null) 
			return defaultValue;
		else
			return temp;
	}

	/**
	 * @see FunctionElement#setValue(List, Element)
	 */
	public void setValue(List<? extends Element> args, Element value) throws UnmodifiableFunctionException {
		super.setValue(args, value);
		
		ElementList el;
		if (args instanceof ElementList)
			el = (ElementList)args;
		else
			el = ElementList.create(args);

		if (value.equals(defaultValue))
			table.remove(el);
		else
			table.put(el, value);
	}
	
	/**
	 * @see FunctionElement#getLocations(String)
	 */
	public Set<Location> getLocations(String name) {
		Set<Location> locSet = new HashSet<Location>();
		Set<ElementList> argSet = table.keySet();
		Location loc = null;
		for (ElementList l : argSet) {
			if (!table.get(l).equals(defaultValue)) {
				loc = new Location(name, l);
				locSet.add(loc);
			}
		}
		return locSet;
	}

	/**
	 * Returns the range of this function as a set of elements.
	 * 
	 * @see FunctionElement#getRange()
	 */
	public Set<? extends Element> getRange() {
		return new HashSet<Element>(table.values());
	}
	
	/**
	 * Removes all the values from this function.
	 */
	public void clear() {
		table.clear();
	}
	
	@SuppressWarnings("unchecked")
	public Map<ElementList, Element> getTable() {
		return (Map<ElementList, Element>)table.clone();
	}
}

