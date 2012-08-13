/*	
 * EngineAggregationAPI.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Mashaal Memon
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
 *	Interface providing all aggregation actions/services required by the engine
 *  to perform and get information about aggregation performed by plugins.
 *   
 *  @author  Mashaal Memon
 *  
 */
public interface EngineAggregationAPI {
	
	/**
	 * Set the multiset of update instructions which have been collected throughout the step
	 * and are to be aggregated.
	 * 
	 * @param updates The multiset of updates collected through the current step.
	 */
	void setUpdateInstructions(UpdateMultiset updates);
	
	/**
	 * Is aggregation consistent?
	 * 
	 * @return a <code>boolean</code> representing aggregation consistency. "true" is returned
	 * if aggregation was deemed consistent.
	 */
	boolean isConsistent();
	
	/**
	 * Return a collection of failed update instructions.
	 * 
	 * @return a <code>Collection</code> of <code>Update</code> representing all update instructions
	 * which could not be aggregated.
	 */
	Collection<Update> getFailedInstructions();
	
	/**
	 * Return a list of unprocessed update instructions.
	 * 
	 * @return a <code>Collection</code> of <code>Update</code> representing all update instructions
	 * which could not be processed.
	 */
	Collection<Update> getUnprocessedInstructions();
	
	/**
	 * Return the set of resultant updates from aggregation.
	 * 
	 * @return a <code>Set</code> of <code>Update</code> representing resultant updates from
	 * aggregation.
	 */
	Set<Update> getResultantUpdates();

}
