/*
 * State.java 	1.0 	$Revision: 243 $
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

import java.util.Map;
import java.util.Set;

/**
 * Defines an interface to CoreASM state.
 * 
 * @author Roozbeh Farahbod
 * 
 */
public interface State {

	/**
	 * Returns a set of all the defined 
	 * universes in the state.
	 * 
	 * @return set of universes
	 */
	public Map<String,AbstractUniverse> getUniverses();
	
	/**
	 * Finds a universe (or background) element in the state with the given name.
	 * 
	 * @return the found function element; <code>null</code> if no 
	 * such function exists.
	 */
	public AbstractUniverse getUniverse(String name);
	
	/**
	 * Adds a new universe to the state with the given name.
	 * 
	 * @param name name of the universe
	 * @param universe universe to be added.
	 * @throws NameConflictException if another
	 * identifier with the same name already exists in 
	 * the state.
	 */
	public void addUniverse(String name, AbstractUniverse universe) throws NameConflictException;
	
//	/**
//	 * Removes a universe from the state.
//	 * 
//	 * @param universe universe to be removed
//	 * @throws IdentifierNotFoundException if no such universe
//	 * is found in the state
//	 */
//	public void removeUniverse(UniverseElement universe) 
//			throws IdentifierNotFoundException;
	
	/**
	 * Set of all the functions (including universes) defined in the state.
	 * 
	 * @return set of functions
	 */
	public Map<String,FunctionElement> getFunctions();
	
	/**
	 * Finds a function element in the state with the given name.
	 * 
	 * @return the found function element; <code>null</code> if no 
	 * such function exists.
	 */
	public FunctionElement getFunction(String name);
    
    /**
     * Returns the name of the given function element.
     * 
     * @return the name of the function element; <code>null</code> if no 
     * such function exists.
     */
    public String getFunctionName(FunctionElement function);
	
	/**
	 * Adds a new function to the state.
	 * 
	 * @param name name of the function
	 * @param function function to be added
	 * @throws NameConflictException if another identifier 
	 * with the same name already exists in the state.
	 */
	public void addFunction(String name, FunctionElement function) throws NameConflictException;
	
//	/**
//	 * Removes a function from the state.
//	 * 
//	 * @param function function to be removed.
//	 * @throws IdentifierNotFoundException if there is no
//	 * such function in the state.
//	 */
//	public void removeFunction(FunctionElement function) 
//			throws IdentifierNotFoundException;
		
	/**
	 * Set of all the defined locations in the state
	 * that have a value other than <code>undef</code>
	 *  
	 * @return set of defined locations
	 */
	public Set<Location> getLocations();
	
	/**
	 * @return returns the function element that holds 
	 * the mapping between names and functions. 
	 */
	public FunctionElement getFunctionElementFunction();

	/**
	 * @return returns the function element that holds 
	 * the mapping between names and universes. 
	 */
	public FunctionElement getUniverseElementFunction();
	
	/**
	 * Returns the value of a location in the state
	 * 
	 * @param loc location
	 * @return value of the given location; <code>undef</code>
	 * if there is no value for such location.
	 * @throws InvalidLocationException if the location is not valid.
	 */
	public Element getValue(Location loc) throws InvalidLocationException;
	
	/**
	 * Sets a new value for a location in the state.
	 * If the location does not exist, it adds a new location to the
	 * state and then sets its value.
	 * 
	 * @param loc
	 * @param value
	 * 
	 * @throws InvalidLocationException if the location is not modifiable. 
	 */
	public void setValue(Location loc, Element value) throws InvalidLocationException;
}

