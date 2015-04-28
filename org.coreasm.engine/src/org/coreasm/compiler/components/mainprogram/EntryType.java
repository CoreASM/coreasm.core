package org.coreasm.compiler.components.mainprogram;

/**
 * EntryType Enum for MainFile Entries.
 * Regulates how the class is included in the main code.
 * @author Markus Brenner
 *
 */
public enum EntryType {
	/**
	 * A function type entry will be imported as a function into the abstract storage.
	 * Requires a name, under which the entry will be accessible.
	 * The class needs to have a default constructor.
	 */
	FUNCTION, 
	/**
	 * A function type entry will be imported as a function into the abstract storage.
	 * This function entry is used for functions, which require passing a capi to the constructor
	 * Requires a name, under which the entry will be accessible.
	 * The class needs to have a default constructor.
	 */
	FUNCTION_CAPI, 
	/**
	 * A universe type entry will be imported as a universe into the abstract storage.
	 * Requires a name, under which the entry will be accessible.
	 * The class needs to have a default constructor.
	 */
	UNIVERSE, 
	/**
	 * A background type entry will be imported as a (special) universe into the abstract storage.
	 * Requires a name, under which the entry will be accessible.
	 * The class needs to have a default constructor.
	 */
	BACKGROUND, 
	/**
	 * Only here for completeness purposes, currently not used.
	 */
	RULE, 
	/**
	 * Only here for completeness purposes, currently not used.
	 */
	AGENT, 
	/**
	 * A Scheduler type entry will replace the default scheduler.
	 * Only one scheduler can be active as a time.
	 * Needs to have a default constructor
	 */
	SCHEDULER,
	/**
	 * An aggregator type entry will be added to the list of aggregators.
	 * Needs to have a default constructor
	 */
	AGGREGATOR, 
	/**
	 * An includeonly type entry will only be included.
	 * It will not appear in the main file, but can be used in other
	 * code.
	 */
	INCLUDEONLY
}

