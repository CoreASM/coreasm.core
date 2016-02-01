/*	
 * LocalRuleNode.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.turboasm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Wrapper for Local rule nodes.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class LocalRuleNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	private Set<String> functionNames = null;
	
	/**
	 */
	public LocalRuleNode(ScannerInfo info) {
		super(
				TurboASMPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"LocalRule",
				null,
				info);
	}

	public LocalRuleNode(LocalRuleNode node) {
		super(node);
	}
	
	/**
	 * Returns the set local function names. This method
	 * assumes that the node structure does not change after 
	 * the first call to this method, so it caches the result.
	 * 
	 * @return a node
	 */
	public Collection<String> getFunctionNames() {
		if (functionNames == null) {
			try {
				functionNames = new HashSet<String>(getFunctionMap().keySet());
			} catch (CoreASMError e) {
				functionNames = Collections.emptySet();
			}
		}
		return functionNames;
	}
	
	/**
     * Returns a map of the function names to the nodes which
     * represent the terms that will be evaluated
     */
    public Map<String,ASTNode> getFunctionMap() throws CoreASMError {
    	Map<String,ASTNode> functionMap = new HashMap<String,ASTNode>();
        
        ASTNode current = getFirst();
        
        while (current != null && current.getNextCSTNode() != null) {
        	if (TurboASMPlugin.LOCAL_INIT_OPERATOR.equals(current.getNextCSTNode().getToken())) {
        		if (functionMap.put(current.getToken(),current.getNext()) != null)
        			throw new CoreASMError("There must not be multiple initializations for the same function.", current);
        		current = current.getNext().getNext();
        	}
        	else {
        		functionMap.put(current.getToken(), null);
        		current = current.getNext();
        	}
        }
        return functionMap;
    }
	
	/** 
	 * Returns the sub-rule part of this rule
	 * 
	 * @return a node
	 */
	public ASTNode getRuleNode() {
		return (ASTNode)getChildNode("alpha");
	}

}
