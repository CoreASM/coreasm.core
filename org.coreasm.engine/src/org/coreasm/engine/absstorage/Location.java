/*	
 * Location.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005-2008 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import java.util.List;

/** 
 *	Implements LOCATION elements
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class Location {

	/**
	 * The name of the location function
	 */
	public final String name;
	
	/**
	 * Arguments; list of Elements
	 */
	public final ElementList args;
	
	/** if not null, indicates whether this location is modifiable or not. */
	public final Boolean isModifiable;
	
	/**
	 * Creates a new location with the given
	 * function and agruments.
	 * 
	 * @param name the name of the function element the new location
	 * @param args list of abstract object values as arguments
	 */
	public Location(String name, List<? extends Element> args) {
		if (name == null)
			throw new NullPointerException("Name of a location cannot be null.");
		if (args == null)
			throw new NullPointerException("Arguments of a location cannot be null.");
		this.args = ElementList.create(args);
		this.name = name;
		this.isModifiable = null;
	}

	/**
	 * Creates a new location with the given
	 * function and agruments.
	 * 
	 * @param name the name of the function element the new location
	 * @param args list of abstract object values as arguments
	 * @param isModifiable indicates whether this location is modifiable.
	 */
	public Location(String name, List<? extends Element> args, boolean isModifiable) {
		if (name == null)
			throw new NullPointerException("Name of a location cannot be null.");
		if (args == null)
			throw new NullPointerException("Arguments of a location cannot be null.");
		this.args = ElementList.create(args);
		this.name = name;
		this.isModifiable = isModifiable;
	}

	/**
	 * Provides a <code>String</code> representation of this 
	 * location.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
//		return "(" + name + ", " + args + ")";
		String result = args.toString();
		result = "(" + result.substring(1, result.length() -1) + ")";
		return name + result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		return true;
	}
}
