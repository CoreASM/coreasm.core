/*	
 * ChooseRuleNode.java 	1.0 	$Revision: 243 $
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 * 
 * Copyright (C) 2006 George Ma
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.chooserule;

import java.util.HashMap;
import java.util.Map;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	ChooseRuleNode is a Node for choose rule nodes.
 *   
 *  @author  George Ma, Roozbeh Farahbod
 *  
 */

public class ChooseRuleNode extends ASTNode {

    private static final long serialVersionUID = 1L;
    
    public ChooseRuleNode(ChooseRuleNode node) {
		super(node);
	}

	public ChooseRuleNode(ScannerInfo scannerInfo) {
		super(ChooseRulePlugin.PLUGIN_NAME, 
				ASTNode.RULE_CLASS, 
				"ChooseRule", 
				null, 
				scannerInfo);
	}
	
	/**
     * Returns a map of the variable names to the nodes which
     * represent the domains that variable should be taken from
     * @throws CoreASMError 
     */
    public Map<String,ASTNode> getVariableMap() throws CoreASMError {
    	Map<String,ASTNode> variableMap = new HashMap<String,ASTNode>();
        
        for (ASTNode current = getFirst(); current.getNext() != null && current.getNext().getNext() != null && ASTNode.ID_CLASS.equals(current.getGrammarClass()); current = current.getNext().getNext()) {
            if (variableMap.put(current.getToken(),current.getNext()) != null)
            	throw new CoreASMError("Variable \""+current.getToken()+"\" already defined in choose rule.", this);
        }
        
        return variableMap;
    }

    /**
     * Returns the node representing the 'do' part of the choose rule.
     */
    public ASTNode getDoRule() {
        return (ASTNode)getChildNode(ChooseRulePlugin.DO_RULE_NAME);
    }
    
    /**
     * Returns the node representing the 'ifnone' part of the choose rule.
     */
    public ASTNode getIfnoneRule() {
        return (ASTNode)getChildNode(ChooseRulePlugin.IFNONE_RULE_NAME);
    }
    
    /**
     * Returns the node representing the condition ('with' part) of the choose rule.
     * null is returned if the choose rule has no condition.
     */
    public ASTNode getCondition() {
    	return (ASTNode)getChildNode(ChooseRulePlugin.GUARD_NAME);
    }
    
}
