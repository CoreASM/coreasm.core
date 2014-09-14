/*	
 * ForallRuleNode.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 George Ma
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.forallrule;

import java.util.HashMap;
import java.util.Map;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	ForallRuleNode is a NodeWrapper for forall rule nodes.
 *   
 *  @author  George Ma
 *  
 */

public class ForallRuleNode extends ASTNode {

    /**
     * Creates a new ForallRuleNode
     */
    public ForallRuleNode(ScannerInfo info) {
        super(
        		ForallRulePlugin.PLUGIN_NAME,
        		ASTNode.RULE_CLASS,
        		"ForallRule",
        		null,
        		info);
    }

    public ForallRuleNode(ForallRuleNode node) {
    	super(node);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Returns a map of the variable names to the nodes which
     * represent the domains that variable should be taken from
     * @throws CoreASMError 
     */
    public Map<String,ASTNode> getVariableMap() throws CoreASMError {
    	Map<String,ASTNode> variableMap = new HashMap<String,ASTNode>();
        
        for (ASTNode current = getFirst(); current.getNext() != null && current.getNext().getNext() != null && ASTNode.ID_CLASS.equals(current.getGrammarClass()); current = current.getNext().getNext()) {
            if (variableMap.put(current.getToken(),current.getNext()) != null)
            	throw new CoreASMError("Variable \""+current.getToken()+"\" already defined in forall rule.", this);
        }
        
        return variableMap;
    }
    
    /**
     * Returns the node representing the 'do' part of the forall rule.
     */
    public ASTNode getDoRule() {
        return (ASTNode)getChildNode("rule");   
    }
    
    /**
     * Returns the node representing the 'ifnone' part of the forall rule.
     */
    public ASTNode getIfnoneRule() {
        return (ASTNode)getChildNode("ifnone");
    }
    
    /**
     * Returns the node representing the 'with' part of the forall rule.
     * If there is no 'with' condition specified, null is returned.
     */
    public ASTNode getCondition() {
    	return (ASTNode)getChildNode("guard"); 
    }

}
