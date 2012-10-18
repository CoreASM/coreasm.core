/*	
 * OperatorRegistry.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Mashaal Memon
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.engine.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.util.Logger;

/** 
 *	The registry of operators and their contributors.
 *
 *  Contains static methods for decifering which contributors (plug-ins) have an implementation
 *  for a given operator.
 *   
 *  @author  Roozbeh Farahbod, Mashaal Memon
 *  
 */
public final class OperatorRegistry {
	
	private static Map<ControlAPI, OperatorRegistry> instances = null;
	
	// Maps of the form: Operator -> (PluginName -> OperatorRule) 
	public final Map<String, Map<String, OperatorRule>> binOps;
	public final Map<String, Map<String, OperatorRule>> unOps;
	public final Map<String, Map<String, OperatorRule>> indexOps;

	/**
	 * Private constructor.
	 *
	 */
	private OperatorRegistry() {
		binOps = new HashMap<String, Map<String,OperatorRule>>();
		unOps = new HashMap<String, Map<String,OperatorRule>>();
		indexOps = new HashMap<String, Map<String,OperatorRule>>();
	}
	
	/**
	 * Create a new OperatorRegistry singleton for the current thread.
	 *  
	 * @return an instance of OperatorRegistry
	 */
	public static synchronized OperatorRegistry getInstance(ControlAPI capi) {
		if (instances == null) 
			instances = new HashMap<ControlAPI, OperatorRegistry>();
		
		OperatorRegistry instance = instances.get(capi);
		
		if (instances.get(capi) == null) {
			instance = new OperatorRegistry();
			instances.put(capi, instance);
		}
		
		return instance;
	}
	
	/**
	 * Returns a set of the names of all the plugins that contribute an operator
	 * with the given token and the grammar class.
	 *  
	 * @param token
	 * @param grammarClass
	 */
	public Set<String> getOperatorContributors(String token, String grammarClass) {
		Set<String> names = new HashSet<String>();
		Map<String, Map<String, OperatorRule>> oprs = null;
		
		if (grammarClass.equals(ASTNode.BINARY_OPERATOR_CLASS))
			oprs = binOps;
		if (grammarClass.equals(ASTNode.UNARY_OPERATOR_CLASS))
			oprs = unOps;
		if (grammarClass.equals(ASTNode.INDEX_OPERATOR_CLASS))
			oprs = indexOps;
		
		if (oprs != null) {
			Map<String, OperatorRule> mapping = oprs.get(token);
			if (mapping != null) 
				names = mapping.keySet();
		} else {
			Logger.log(Logger.ERROR, Logger.parser, "\"" + grammarClass + "\" is not a supported class of operators (for operator \"" + token + "\").");
		}
		
		return names;
	}
	
}
