/*	
 * UniverseElement.java 	1.0 	$Revision: 80 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package CompilerRuntime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 
 *	The element representing a Universe in the state.
 *   
 *  @author  Roozbeh Farahbod
 */
public class UniverseElement extends AbstractUniverse implements Enumerable {

	/* Underlying function *
	protected MapFunction universeFunction;
	*/
	
	protected final Set<Element> elements;
	
	protected List<Element> enumerationCache = null; 
	
	/**
	 * Creates a new Universe. 
	 * The default value will be <code>BooleanElement.FALSE</code>.
	 * 
	 */
	public UniverseElement() {
		elements = new HashSet<Element>();
	}

	/** 
	 * Provides a set of all the Elements in this universe.
	 * 
	 * @see org.coreasm.engine.absstorage.Enumerable#enumerate()
	 */
	public Collection<? extends Element> enumerate() {
		return getIndexedView();
	}

	/** 
	 * Adds/Removes an Element into/from this universe. If the given
	 * list of arguments is not of size one, or the given 
	 * value is not instance of <code>BOOLEAN</code> Element, this
	 * method does nothing.
	 * 
	 */
	public void setValue(List<? extends Element> args, Element value) {
		if (args.size() == 1 && value instanceof BooleanElement)
			setValue(args.get(0), (BooleanElement)value);
	}

	public void setValue(Element e, BooleanElement v) {
		enumerationCache = null;
		if (v.getValue())
			elements.add(e);
		else
			elements.remove(e);
	}

	@Override
	public Element getValue(Element e) {
		return elements.contains(e)?BooleanElement.TRUE:BooleanElement.FALSE;
	}

	public Set<Location> getLocations(String name) {
		Set<Location> locSet = new HashSet<Location>();
		Location loc = null;
		for (Element e : elements) {
			loc = new Location(name, ElementList.create(e));
			locSet.add(loc);
		}
		return locSet;
	}

	/**
	 * Sets the value of the membership function 
	 * for this universe.
	 *  
	 * @param value a given Element
	 * @param b if <code>true</code>, the given value is
	 * added to this universe; if <code>false</code>,
	 * given value is removed from the universe.
	 */
	public void member(Element value, boolean b) {
		setValue(value, BooleanElement.valueOf(b));
	}

    public boolean contains(Element e) {
        return elements.contains(e);
    }

	public List<Element> getIndexedView()
			throws UnsupportedOperationException {
		if (enumerationCache == null) {
			enumerationCache = new ArrayList<Element>(elements);
		} 
		return enumerationCache;
	}

	public boolean supportsIndexedView() {
		return true;
	}

	public int size() {
		return elements.size();
	}

}
