/*	
 * EngineProperties.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine;

import java.util.Properties;

/** 
 * CoreASM engine properties.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class EngineProperties extends Properties {

	public static final String YES = "yes";
	public static final String NO = "no";
	public static final String FALSE = "false";
	public static final String TRUE = "true";
	
	/** 'yes': the engine will print the stack trace of errors/exceptions */
	public static final String PRINT_STACK_TRACE = "engine.error.printStackTrace";
	
	/** the maximum number of processors the engine can use for simulation */
	public static final String MAX_PROCESSORS = "engine.limits.maxProcessors";

	/** 'yes': the engine will print some stats on processor utilization after every step */
	public static final String PRINT_PROCESSOR_STATS_PROPERTY = "scheduler.printProcessorStats";

	/** in a multi-threaded simulation, this is the minimum number of agents assigned to every thread */
	public static String AGENT_EXECUTION_THREAD_BATCH_SIZE = "scheduler.threadBatchSize";

	/** a colon-separated list of folders that include additional plugins */
	public static String PLUGIN_FOLDERS_PROPERTY = "engine.pluginFolders";
	
	/** Delimiter string for the list of plugin folders */
	public static String PLGUIN_FOLDERS_DELIM = ":";
	
	/** a comma separated list of plugins to be loaded in addition to the specification plugins */
	public static String PLUGIN_LOAD_REQUEST_PROPERTY = "engine.pluginLoadRequest";
	
	/** Delimiter string for the list of plugins in {@link #PLUGIN_LOAD_REQUEST_PROPERTY} */
	public static String PLUGIN_LOAD_REQUEST_DELIM = ",";
	
	private static final long serialVersionUID = 1L;

	public EngineProperties() {
		super();
		setDefaults();
	}
	
	/**
	 * Load engine properties from a user-defined set of properties.
	 */
	public EngineProperties(Properties props) {
		setDefaults();
		this.putAll(props);
	}
	
	/**
	 * Sets the default engine properties.
	 */
	public void setDefaults() {
		this.setProperty(PRINT_STACK_TRACE, NO);
		this.setProperty(MAX_PROCESSORS, "1");
		this.setProperty(PRINT_PROCESSOR_STATS_PROPERTY, "no");
		this.setProperty(AGENT_EXECUTION_THREAD_BATCH_SIZE, "1");
	}
	
}
