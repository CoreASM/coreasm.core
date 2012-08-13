/*	
 * ServiceRequest.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author$ on $Date$.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugin;

import java.util.HashMap;
import java.util.Map;

import org.coreasm.engine.CoreASMError;

/**
 * Service request container as part of inter-plugin communications.
 * 
 * @author Roozbeh Farahbod
 * @see ServiceRegistry
 *
 */
public class ServiceRequest {

	/** type of service */
	public final String type;
	
	/** parameters */
	public final Map<String, Object> parameters;
	
	public ServiceRequest(String type) {
		if (type == null)
			throw new CoreASMError("Cannot create a service request with an undefied type.");
		this.type = type;
		this.parameters = new HashMap<String, Object>();
	}
	
	/**
	 * returns the value of the given parameter.
	 * @param pname parameter name
	 */
	public Object getParam(String pname) {
		return parameters.get(pname);
	}
}
