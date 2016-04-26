/*
 * AbstractStorage.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.absstorage;

import java.util.Collection;
import java.util.Set;

/**
 * Interface to the Abstract Storage component inside any CoreASM engine.
 * 
 * @author Roozbeh Farahbod
 * 
 */
public interface AbstractStorage extends State {

	/** 'program' function name */
	public static final String PROGRAM_FUNCTION_NAME = "program";
	
	/** 'Agents' universe name */
	public static final String AGENTS_UNIVERSE_NAME = "Agents";
	
	/** 'functionElement' function name */
	public static final String FUNCTION_ELEMENT_FUNCTION_NAME = "functionElement";
	
	/** 'ruleElement' function name */
	public static final String RULE_ELEMENT_FUNCTION_NAME = "ruleElement";

	/** 'universeElement' function name */
	public static final String UNIVERSE_ELEMENT_FUNCTION_NAME = "universeElement";
	
	/**
	 * Initializes abstract storage and cleans the state.
	 */
	public void initAbstractStorage();
	
	/**
	 * Fires an update set; i.e., applies the updates in the specified
	 * update set to the current state.
	 * 
	 * This method does NOT check for consistency and 
	 * assumes that the given update set is consistent.
	 * 
	 * @param updateSet updates to be applied to the current state.
	 */
	public void fireUpdateSet(Set<Update> updateSet) throws InvalidLocationException;
	
	/**
	 * Performs aggregation on a given collection of update instructions.
	 * 
	 * @param updateInsts collection of update instructions 
	 * @return returns the result of aggregation as a set of updates
	 */
	public Set<Update> performAggregation(UpdateMultiset updateInsts);

	/*
	 * Not Supported Anymore.
	 * See Interpreter.setSelf(Element) and Interpreter.getSelf()
	 * 
	 * 
	 * Sets the specified agent as the value of <i>self</i> in the
	 * current state.  
	 * 
	 * @param agent the chosen agent.
	 *
	public void setSelf(Element agent);
	
	/*
	 * @return the current value of <i>self</i> in the state.
	 * 
	 *  @see #setSelf(Element)
	 *
	public Element getSelf();
	*/
	
	/**
	 * Returns the value of 'program(agent)' for the given agent.
	 */
	public Element getChosenProgram(Element agent);
	
//	/**
//	 * Returns the current state of the simulated machine.
//	 * 
//	 * @return the current state of the simulated machine.
//	 * @see org.coreasm.engine.absstorage.State
//	 */
//	public State getState();
	
//	/**
//	 * Assigns the specified state as the current state of the engine.
//	 * After a successful execution of this method, <code>getState</code> 
//	 * returns this specified state.
//	 * 
//	 * There are rare cases in which this method needs to be called.
//	 * Almost all changes to the state should be performed through updates.
//	 * 
//	 * @param newState new state of the engine.
//	 * @see org.coreasm.engine.absstorage.State
//	 */
//	public void setState(State newState);
	
//	/**
//	 * Retrieves the value of the given location from the current state.
//	 */
//	public Element getValue(Location l) throws InvalidLocationException;
//
//	/**
//	 * Sets a new value for a location in the state.
//	 */
//	public void setValue(Location l, Element v) throws InvalidLocationException;
	
	/**
	 * Aggregates update instructions to compute regular updates.
	 */
	public void aggregateUpdates();
	
	/**
	 * Computes the sequential composition of two update multisets.
	 * 
	 * @param updateSet1 first update multiset
	 * @param updateSet2 second update multiset
	 * @return a composed update multiset which may still have special updates.
	 */
	public UpdateMultiset compose(UpdateMultiset updateSet1, UpdateMultiset updateSet2);
	
	/**
	 * Returns <code>true</code> if the given update set is consistent.
	 */
	public boolean isConsistent(Collection<Update> updateSet);

	/**
	 * Imports a new element in to the state.
	 * 
	 * @return a new element
	 */
	public Element getNewElement();
	
	/*
	 * Imports a new element of a background or a universe into the state.
	 * 
	 * @param bkg an <code>AbstractUniverse</code> object
	 * @return a new element of the given background/universe
	 * 
	 * @deprecated This method is deprecated
	 */
//	public Element getNewElementFrom(AbstractUniverse bkg);
	
	/**
	 * Pushes the current state in the stack.
	 * @param pluginName The name of the plugin that wants to push the state to the stack
	 */
	public void pushState(String pluginName);

	/**
	 * Retrieves the state from the top of the stack 
	 * (thus discarding the current state). 
	 * @param pluginName The name of the plugin that wants to pop the state from the stack
	 */
	public void popState(String pluginName);
	
	/**
	 * Applies the updates in the given update set to the current state.
	 * This method should only be called when there is a state in the stack.
	 * 
	 * @param u the update multiset
	 * @see #pushState()
	 */
	public void apply(Set<Update> u);
	
	/**
	 * Clears the state to an empty state.
	 */
	public void clearState();

	/**
	 * Return <code>true</code> if the given name is the name
	 * of a function in the state.
	 */
	public boolean isFunctionName(String token);

	/**
	 * Return <code>true</code> if the given name is the name
	 * of a universe in the state.
	 */
	public boolean isUniverseName(String token);

	/**
	 * Return <code>true<code> if the given name is the name
	 * of a rule in the state.
	 */
	public boolean isRuleName(String token);

	/**
	 * @return the last inconsistent updates. Returns <code>null</code>
	 * if the last call to {@link #isConsistent(Collection)} returned 
	 * <code>true</code>.
	 */
	public Set<Update> getLastInconsistentUpdate();
}

