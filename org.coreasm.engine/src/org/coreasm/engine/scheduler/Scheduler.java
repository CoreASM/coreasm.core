/*	
 * Scheduler.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.scheduler;

import java.util.Set;

import org.coreasm.engine.EngineException;
import org.coreasm.engine.InvalidSpecificationException;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;

/** 
 *	Defines the interface of the scheduler module.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public interface Scheduler {
	
	// This JavaDoc comment is modified by Roozbeh Farahbod, 09-Jan-2006
	/**
	 * Initializes the engine according to the specification. 
	 * It may set up rules and functions depending on the specification.
	 * Finally, it runs the initialization method of the specification.
	 *
	 * @deprecated see {@link #prepareInitialState()}
	 */
	@Deprecated
	public void executeInitialization() throws InvalidSpecificationException;

	/**
	 * Prepares the state for the first step of the simulation. 
	 * This involves ensuring that there is at least one agent with the 
	 * 'init' rule as its program.
	 * 
	 * @throws InvalidSpecificationException if the specification is invalid
	 */
	public void prepareInitialState()  throws InvalidSpecificationException;
	
	/**
	 * Returns the last computed update set of the engine.
	 * 
	 */
	public Set<Update> getUpdateSet();
	
	/**
	 * Returns the multiset of accumulating update instructions in 
	 * one compuation step.
	 * 
	 * @return collection of <code>Update</code> 
	 */
	public UpdateMultiset getUpdateInstructions();
	
	/**
	 * Returns the set of all the available agents in the  current
	 * state retrieved from the abstract storage at the beginning of
	 * every computation step
	 * 
	 * @return <code>Set</code> of <code>Element</code>
	 */
	public Set<? extends Element> getAgentSet();
	
	/**
	 * Returns the current set of selected agents to contribute to the 
	 * computation of the current step.
	 * 
	 * @return <code>Set</code> of <code>Element</code>
	 */
	public Set<? extends Element> getSelectedAgentSet();
	
	/**
	 * Returns the set of selected agents that contributed to the 
	 * latest computation step. If the current step is already started,
	 * it would return the current set of selected agents.
	 * 
	 * @return <code>Set</code> of <code>Element</code>
	 */
	public Set<? extends Element> getLastSelectedAgents();
	
    /*
     * Removed from the concurrent version of the engine
     * 
	 * Returns the current running (or to be run) agent.
	 * 
	 * @return <code>Element</code> corresponding to the current agent.
	 *
	public Element getChosenAgent();
	 */
	
	/**
     * Starts a computation step.
     */
    public void startStep();
   
    /**
     * Gets the set of agents.  Updates the agent set.
     *
     */
    public void retrieveAgents();
   
    /**
     * Selects the subset of agents to contribute to the current computation step.
     *
     * @return <code>true</code> if a subset of agents could be selected, <code>false</code> otherwise.
     */
    public boolean selectAgents();

    /*
 	 * Removed from the concurrent version of the engine.
 	 *  
	 * Chooses an agent from the selected agents set.
     *
     *
    public void chooseAgent();
    */

	/*
	 * removed from the concurrent version of the Engine
	 * 
     * Accumulates the computed updates.
     *
     *
    public void accumulateUpdates();
     */

    /*
     * Removed from the concurrent version of the engine.
     * 
     * Initiates the execution of the selected agent and invokes the Control API
     * to possibly notify the external environment.
     *
     *
    public void initiateExecution();
    */

    /**
     * If there are other possible combinations of agents, selects a different
     * set of agents and repeats the computation step; otherwise, handles a failed

     * If there are other possible combinations of agents, selects a different
     * set of agents and repeats the computation step; otherwise, handles a failed
     * update and invokes the Control API to possibly notify the external environment.
     *
     */
    public void handleFailedUpdate();
    
    /**
     * Checks if the latest inconsistency in the updates is produced by 
     * a single agent. 
     */
	public boolean isSingleAgentInconsistent();

	/**
     * Returns <code>true</code> if there exists any possible combination of agents that 
     * can contribute to one single step. This is also used when an update set fails, to
     * decide if a failure should be reported or there are other options available.
     * 
     */
    public boolean agentsCombinationExists();
    
    /*
     * Removed from the concurrent version of the engine
     * 
     * Returns the current value of <i>chosenProgram</i> in the scheduler.
     *
    public RuleElement getChosenProgram();
     /**/

    /*
     * Removed from the concurrent version of the engine
     * 
     * Sets the value of <i>chosenProgram</i> in the scheduler.
     *
    public void setChosenProgram(RuleElement Program);
     */

    /**
     * Executes programs of the set of selected agents.
     * 
     * @throws EngineException if any error occurs
     */
	public void executeAgentPrograms() throws EngineException;

    /**
     * Sets the value of <i>initAgent</i> in the scheduler.
     */
    public void setInitAgent(Element agent);
        
    /**
     * Returns the value of <i>initAgent</i> in the scheduler.
     */    
    public Element getInitAgent();
    
    /**
     * Sets <i>stepCount</i> to the specified value.
     */    
    public void setStepCount(int count);
    
    /**
     * Returns <i>stepCount</i>, the number of successfully completed 
     * steps in the current run of the engine.
     */    
    public int getStepCount();
    
    /**
     * Increments <i>stepCount</i> by 1.
     */       
    public void incrementStepCount();
    
    public void dispose();
}
