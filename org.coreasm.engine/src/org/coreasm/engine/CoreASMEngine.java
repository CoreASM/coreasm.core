/*
 * CoreASMEngine.java 
 *
 * Copyright (C) 2005-2010 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-01-07 02:19:25 +0100 (Fr, 07 Jan 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.State;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.Specification;
import org.coreasm.engine.plugin.PluginServiceInterface;
import org.coreasm.engine.scheduler.Scheduler;

import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Defines the interface of a CoreASM Engine to its outside environment 
 * (e.g., GUI and test tools). 
 * <p>
 * Most of the methods of this interface are intended
 * to be implemented in a non-blocking fashion. Engine drivers (such as GUIs) are 
 * recommended to call {@link CoreASMEngine#waitWhileBusy()} if they want to wait 
 * for the engine to complete its tasks.
 *  
 * @author Roozbeh Farahbod
 */
public interface CoreASMEngine extends VersionInfoProvider {
	
	public static String[] KERNEL_PLUGINS = {"Kernel"};
	
	/**
	 * CoreASM engine modes.
	 * 
	 * @author Roozbeh Farahbod
	 */
	public static enum EngineMode {
		emIdle, 
		emInitKernel, 
		emLoadingCatalog,
		emLoadingCorePlugins,
		emParsingHeader,
		emLoadingPlugins,
		emParsingSpec,
		emInitializingState,
		@Deprecated emLoadingInitialState,
		emPreparingInitialState,
		emStartingStep, 
		emSelectingAgents, 
//		emChoosingAgents,
		emRunningAgents,
		emStepSucceeded, 
		emStepFailed, 
		emUpdateFailed, 
		emAggregation,
//		emInitializingSelf, 
//		emInitiatingExecution,
//		emProgramExecution, 
//		emChoosingNextAgent,
		emTerminating,
		emTerminated,
		emError
	};

		 
    /**
     * Initializes the engine.
     */
	public void initialize();
	
	/**
	 * Terminates the execution of the engine. 
	 * The engine finishes its current task before termination. 
	 * 
	 */
	public void terminate();
    
	/**
	 * Recovers from an error (if the engine is in error mode). 
	 *
	 */
	public void recover();
    
    /**
	 * Loads a new specification into the engine for execution.
	 * This will load the specification, parse its header,
	 * loads the necessary plugins, parse the whole specification
	 * and then initializes the abstract storage.
	 * 
	 * @param specFileName the specification file name
	 */
	public void loadSpecification(String specFileName);
	
    /**
	 * Loads a new specification into the engine for execution.
	 * This will load the specification, parse its header,
	 * loads the necessary plugins, parse the whole specification
	 * and then initializes the abstract storage.
	 * 
	 * @param src a reader which provide the spec
	 */
	public void loadSpecification(Reader src);

    /**
	 * Loads a new specification into the engine for execution.
	 * This will load the specification, parse its header,
	 * loads the necessary plugins, parse the whole specification
	 * and then initializes the abstract storage.
	 * 
	 * @param name name of the specification
	 * @param src a reader which provide the spec
	 */
	public void loadSpecification(String name, Reader src);

    /**
	 * Parses a new specification.
	 * This will load the specification, parse its header,
	 * loads the necessary plugins, and parse the whole specification.
	 * 
	 * <b>Note:</b> This method will not prepare the engine for execution 
	 * 				of the specification. To load a specification for 
	 * 				execution, use {@link #loadSpecification(String)}.
	 * 
	 * @param specFileName the specification file name
	 */
	public void parseSpecification(String specFileName);
	
    /**
	 * Parses a new specification.
	 * This will load the specification, parse its header,
	 * loads the necessary plugins, and parse the whole specification.
	 * 
	 * <b>Note:</b> This method will not prepare the engine for execution 
	 * 				of the specification. To load a specification for 
	 * 				execution, use {@link #loadSpecification(Reader)}.
	 * 
	 * @param src a reader which provide the spec
	 */
	public void parseSpecification(Reader src);

    /**
	 * Parses a new specification.
	 * This will load the specification, parse its header,
	 * loads the necessary plugins, and parse the whole specification.
	 * 
	 * <b>Note:</b> This method will not prepare the engine for execution 
	 * 				of the specification. To load a specification for 
	 * 				execution, use {@link #loadSpecification(String, Reader)}.
	 * 
	 * @param name name of the specification
	 * @param src a reader which provide the spec
	 */
	public void parseSpecification(String name, Reader src);

    /**
	 * Parses the header section of a new specification.
	 * This will load the specification, parse its header, and
	 * loads the necessary plugins.
	 * 
	 * <b>Note:</b> This method will not parse the specification nor 
	 * 				prepare the engine for execution 
	 * 				of the specification. To load a specification for 
	 * 				execution, use {@link #loadSpecification(String)}.
	 * 
	 * @param specFileName the specification file name
	 * @deprecated This method is deprecated. Please use {@link #parseSpecificationHeader(String, boolean)} instead.
	 */
	@Deprecated
	public void parseSpecificationHeader(String specFileName);
	
	/**
	 * Parses the header section of a new specification.
	 * This will load the specification, parse its header, and
	 * if <code>loadPlugins</code> is true, loads the necessary plugins.
	 * 
	 * <b>Note:</b> This method will not parse the specification nor 
	 * 				prepare the engine for execution 
	 * 				of the specification. To load a specification for 
	 * 				execution, use {@link #loadSpecification(String)}.
	 * 
	 * @param specFileName the specification file name
	 * @param loadPlugins if true, this method will load and initialize the plugins as well.
	 */
	public void parseSpecificationHeader(String specFileName, boolean loadPlugins);
	
    /**
	 * Parses the header section of a new specification.
	 * This will load the specification, parse its header, and
	 * loads the necessary plugins.
	 * 
	 * <b>Note:</b> This method will not parse the specification nor 
	 * 				prepare the engine for execution 
	 * 				of the specification. To load a specification for 
	 * 				execution, use {@link #loadSpecification(Reader)}.
	 * 
	 * @param src a reader which provide the spec
	 * @deprecated This method is deprecated. Please use {@link #parseSpecificationHeader(Reader, boolean)} instead.
	 */
	@Deprecated
	public void parseSpecificationHeader(Reader src);

    /**
	 * Parses the header section of a new specification.
	 * This will load the specification, parse its header, and
	 * if <code>loadPlugins</code> is true, loads the necessary plugins.
	 * 
	 * <b>Note:</b> This method will not parse the specification nor 
	 * 				prepare the engine for execution 
	 * 				of the specification. To load a specification for 
	 * 				execution, use {@link #loadSpecification(Reader)}.
	 * 
	 * @param src a reader which provide the spec
	 * @param loadPlugins if true, this method will load and initialize the plugins as well.
	 */
	public void parseSpecificationHeader(Reader src, boolean loadPlugins);

	/**
	 * Parses the header section of a new specification.
	 * This will load the specification, parse its header, and
	 * loads the necessary plugins.
	 * 
	 * <b>Note:</b> This method will not parse the specification nor 
	 * 				prepare the engine for execution 
	 * 				of the specification. To load a specification for 
	 * 				execution, use {@link #loadSpecification(String, Reader)}.
	 * 
	 * @param name name of the specification
	 * @param src a reader which provide the spec
	 * @deprecated
	 */
	@Deprecated
	public void parseSpecificationHeader(String name, Reader src);

	/**
	 * Parses the header section of a new specification.
	 * This will load the specification, parse its header, and
	 * if <code>loadPlugins</code> is true, loads the necessary plugins.
	 * 
	 * <b>Note:</b> This method will not parse the specification nor 
	 * 				prepare the engine for execution 
	 * 				of the specification. To load a specification for 
	 * 				execution, use {@link #loadSpecification(String, Reader)}.
	 * 
	 * @param name name of the specification
	 * @param src a reader which provide the spec
	 * @param loadPlugins if true, this method will load and initialize the plugins as well.
	 */
	public void parseSpecificationHeader(String name, Reader src, boolean loadPlugins);

	/**
	 * Returns the last specification loaded into the engine.
	 * 
	 * @return the last specification loaded into the engine. 
	 *         Returns <code>null</code> if there is no specification loaded.
	 */
	public Specification getSpec();
	
	/**
	 * Returns the current state of the engine (after the last 
	 * computation step).
	 * 
	 * @return the current state of the engine.
	 */
	public State getState();
	
	/**
	 * Returns the last i'th state of the engine. For i equal to zero, 
	 * this function returns the current state. For i equal to one, 
	 * it returns the previous state. 
	 * 
	 * @param i index of the state back in time (zero to be the current one).
	 * @return the last i'th state of the engine. 
	 *         Returns <code>null</code> if the state is not available.
	 */
	public State getPrevState(int i);

	/**
	 * Returns the last i'th update set (set of regular updates) of the engine. 
	 * For i equal to zero,
	 * it will return the current update set which is empty if the engine 
	 * is not currently computing a step. For i equal to one, this function 
	 * returns the last update set that generated the current state. 
	 *  
	 * @param i index of the update set back in time (zero to be the current one).
	 * @return the last i'th update set of the engine.
	 *         Returns <code>null</code> if the update set is not available.
	 * @see org.coreasm.engine.absstorage.Update
	 */
	public Set<Update> getUpdateSet(int i);
	
	/**
	 * Returns the collection of accumulating update instructions in 
	 * one compuation step.
	 * 
	 * @return collection of <code>Update</code> 
	 */
	public UpdateMultiset getUpdateInstructions();
	
//	/**
//	 * Assigns the specified state as the current state of the engine.
//	 * After a successful execution of this method, <code>getState</code> 
//	 * returns this specified state.
//	 * 
//	 * @param newState new state of the engine.
//	 * @see org.coreasm.engine.absstorage.State
//	 */
//	public void setState(State newState); 
//	
	/**
	 * Updates the current state by applying the specified set of regular updates.
	 * 
	 * @param update an update set to be applied to the current state.
	 * @throws InconsistentUpdateSetException if the update set is inconsistent.
	 * @see org.coreasm.engine.absstorage.Update
	 */
	public void updateState(Set<Update> update) 
			throws InconsistentUpdateSetException, InvalidLocationException;
	
	/**
	 * Returns the current set of active agents. This methods 
	 * returns the outcome of {@link Scheduler#getAgentSet()}.
	 * 
	 * @return set of active agents
	 */
	public Set<? extends Element> getAgentSet();
	
	/**
	 * Returns a copy of the properties of this engine.
	 * 
	 * @return engine properties
	 */
	public Properties getProperties();
	
	/**
	 * Sets the engine properties.
	 * 
	 * @param newProperties new properties
	 */
	public void setProperties(Properties newProperties);
	
	/**
	 * Returns the value of the given engine property.
	 */
	public String getProperty(String property);
	
	/**
	 * Returns the value of the given engine property.
	 * If the value of the property is <code>null</code> 
	 * it returns the <code>default</code> value.
	 * 
	 * @see Properties#getProperty(String, String)
	 */
	public String getProperty(String property, String defaultValue);
	
	/**
	 * @return <code>true</code> if the value of the
	 * given property is <i>yes</i>.
	 */
	public boolean propertyHolds(String property);
	
	/**
	 * Sets the value of a given engine property.
	 */ 
	public void setProperty(String property, String value);
	
	/**
	 * Returns the current execution mode of the engine.
	 * 
	 * @return the current execution mode of the engine as an integer.
	 */
	public CoreASMEngine.EngineMode getEngineMode();
	
	/**
	 * Returns an interface to the requested plugin.
	 * 
	 * @param pName Plugin name
	 * @return interface to the plugin; it will be <code>null</code> if no such 
	 * interface is provided by the plugin, or if no such plugin is available in 
	 * the engine.
	 */
	public PluginServiceInterface getPluginInterface(String pName);
	
	/**
	 * Sends a hard interrupt signal to the engine (like changing a flag). If the 
	 * engine is computing a step, it will try to interrupt the current computation.
	 * The engine goes in the <code>INTERRUPTED</code> mode.
	 * 
	 * If the engine is not running (not computing a step), this method does nothing.
	 * 
	 * @see #softInterrupt()    
	 */
	public void hardInterrupt();
	
	/**
	 * Sends a soft interrupt signal to the engine (like changing a flag). If the 
	 * engine is computing a series of steps, it will stop after the computation of the
	 * current step. The engine goes in the <code>IDLE</code> mode. This method 
	 * is meant to stop a series of computations triggered by <code>run</code>.
	 * 
	 * If the engine is not running (not computing a step), this method does nothing.
	 * 
	 * @see #hardInterrupt()
	 * @see #run(int)
	 */
	public void softInterrupt();
	
	/**
	 * Performs one computation step. As a result of this, 
	 * the return values of <code>getState</code>, <code>getPrevState</code>, 
	 * <code>getUpdateSet</code>, and <code>getEngineMode</code> may be changed.
	 * 
	 */
	public void step();
	
	/**
	 * Performs a specified number of computation steps. For i equal to zero, the 
	 * engine runs until it is interrupted or an error occurs. As a result of this, 
	 * the return values of <code>getState</code>, <code>getPrevState</code>, 
	 * <code>getUpdateSet</code>, and <code>getEngineMode</code> may be changed.
	 * 
	 * @param i the requested number of steps.
	 * @see #step()
	 */
	public void run(int i);
	
	/** 
	 * Adds the specified observer to receive engine events.
	 * If <code>observer</code> is null, no exception is thrown
	 * and no action is performed.
	 *  
	 * @param observer the engine observer.
	 * @see org.coreasm.engine.EngineObserver
	 * @see org.coreasm.engine.EngineEvent
	 * @see #removeObserver(EngineObserver)
	 * @see #getObservers()
 	 */
	public void addObserver(EngineObserver observer);
	
	/**
	 * Removes the specified observer so that it no longer receives 
	 * engine events. If <code>observer</code> is null or it was not 
	 * previously added to this engine, no exception is thrown
	 * and no action is performed.
	 *  
	 * @param observer the engine observer.
	 * @see org.coreasm.engine.EngineObserver
	 * @see #addObserver(EngineObserver)
	 * @see #getObservers()
	 */
	public void removeObserver(EngineObserver observer);
	
	/**
	 * Returns a collection of all observers registered in this engine.
	 * 
	 * @return all of this engine's registered observers or an empty array
	 * 	       if no observer is currently registered.
	 * @see org.coreasm.engine.EngineObserver
	 * @see #addObserver(EngineObserver)
	 * @see #removeObserver(EngineObserver)
	 */
	public Collection<EngineObserver> getObservers();
	
	/** 
	 * Waits for the engine to go to the idle or error mode. This 
	 * method should periodically put the current thread in 
	 * a sleep mode to avoid taking CPU time.
	 * 
	 * @deprecated Use {@link #waitWhileBusy()} instead.
	 */
	@Deprecated
	public void waitForIdleOrError();

	/** 
	 * Waits for the engine to go to the idle/error mode. This 
	 * method should periodically put the current thread in 
	 * a sleep mode to avoid taking CPU time.
	 * 
	 * @see #isBusy()
	 */
	public void waitWhileBusy();
	
	/** 
	 * Returns <code>true</code> if the engine is
	 * busy performing some operation. This condition is
	 * used in {@link #waitForIdleOrError()} to wait 
	 * until the engine finishes its work.
	 *   
	 * @see #waitForIdleOrError()
	 */
	public boolean isBusy();

	/**
	 * Returns the set of selected agents that contributed to the 
	 * latest computation step. If the current step is already started,
	 * it would return the current set of selected agents.
	 * 
	 * @return <code>Set</code> of <code>Element</code>
	 * 
	 * @see Scheduler#getLastSelectedAgents()
	 */
	public Set<? extends Element> getLastSelectedAgents();
	
	/**
	 * @return customized class loader that is used 
	 * by the engine to load plugins. A <code>null</code>
	 * indicates the default class loader.
	 */
	public ClassLoader getClassLoader();

	/**
	 * Sets a customized class loader for the engine 
	 * (used in loading plugins. If this value is set 
	 * to <code>null</code>, the engine will use the 
	 * default class loader.
	 */
	public void setClassLoader(ClassLoader classLoader);

	/**
	 * Returns a set of plugin version information (map of plugin name
	 * to version information).
	 */
	public Map<String,VersionInfo> getPluginsVersionInfo();
    
    /**
     * Returns the number of successfully completed steps in the current
     * run of the engine.
     */    
	public int getStepCount();

	/**
	 * Returns the list of warnings occurred during the last/current step.
	 * 
	 * @return a list of {@link CoreASMWarning} instances.
	 */
	public List<CoreASMWarning> getWarnings();

}

