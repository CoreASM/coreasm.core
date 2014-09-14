/*	
 * BagAdvancedCompNode.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.bag;

import java.util.HashMap;
import java.util.Map;

import org.coreasm.engine.EngineException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Advanced bag composition node. 
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class BagCompNode extends ASTNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ASTNode dummyGuard = null;
	private Map<String,ASTNode> varMapCache = null;
	
	public BagCompNode(ScannerInfo info) {
		super(
				BagPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"BagComprehension",
				null,
				info);
	}

	public BagCompNode(BagCompNode node) {
		super(node);
	}
	
	/**
	 * @return the specifier function
	 */
	public ASTNode getSetFunction() {
		return this.getFirst();
	}
	
	public Map<String,ASTNode> getVarBindings() throws EngineException {
		if (varMapCache == null) {
			ASTNode curVar = getSetFunction().getNext();
			ASTNode curDomain = curVar.getNext();
			varMapCache = new HashMap<String,ASTNode>();
			
			while (curDomain != null) {
				if (varMapCache.containsKey(curVar)) 
					throw new EngineException("No two constrainer variables may have the same name.");
				
				varMapCache.put(curVar.getToken(), curDomain);
				curVar = curDomain.getNext();
				if (curVar == null)
					curDomain = null;
				else
					curDomain = curVar.getNext();
			}
		}
		
		return varMapCache;
	}
	
	/**
	 * @return the guard node
	 */
	public ASTNode getGuard() {
		// starting from the fist variable binding
		ASTNode guard = getSetFunction().getNext();
		
		while (guard != null && guard.getNext() != null) {
			// bypassing variable bindings couples
			guard = guard.getNext().getNext();
		}
		
		// guard is optional, so it may be null
		if (guard != null)
			return guard;
		else {
			if (dummyGuard == null)
				dummyGuard = new TrueGuardNode(this);
	    	return dummyGuard;
		}
	}

}
