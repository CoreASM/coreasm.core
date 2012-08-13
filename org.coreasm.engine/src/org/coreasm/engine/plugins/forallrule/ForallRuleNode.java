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
     * Returns the node representing the bound variable of the forall rule
     */
    public ASTNode getVariable() {
        return getFirst();
    }
    
    /**
     * Returns the node representing the domain of the forall rule
     */
    public ASTNode getDomain() {
        return getVariable().getNext();
    }
    
    /**
     * Returns the node representing the 'do' part of the forall rule.
     */
    public ASTNode getDoRule() {
        return (ASTNode)getChildNode("rule");   
    }
    
    
    /**
     * Returns the node representing the 'with' part of the forall rule.
     * If there is no 'with' condition specified, null is returned.
     */
    public ASTNode getCondition() {
    	return (ASTNode)getChildNode("guard"); 
    }

}
