/*	
 * ConditionalRuleNode.java 	1.0 	$Revision: 243 $
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Copyright (C) 2006 George Ma
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.conditionalrule;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	CondtionalRuleNode is a node for conditional (ifThen) nodes.
 *   
 * @author  George Ma
 * 
 */
public class ConditionalRuleNode extends ASTNode {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new ConditionalRuleNode
     */
    public ConditionalRuleNode(ScannerInfo info) {
        super(ConditionalRulePlugin.PLUGIN_NAME, 
        		ASTNode.RULE_CLASS,
        		"ConditionalRule",
        		null,
        		info);
    }

    public ConditionalRuleNode(ConditionalRuleNode node) {
    	super(node);
    }
    
    /**
     * Returns the node representing the guard of the conditional rule
     */
    public ASTNode getGuard() {
        return getFirst();
    }
    
    /**
     * Returns the node representing the consequent of the conditional rule
     * (i.e. rule to execute if the guard is true)
     */
    public ASTNode getIfRule() {
        return getGuard().getNext();
    }
    
    /**
     * Returns the node representing the 'else' part the conditional rule.
     * (i.e. rule to execute if the guard is false)
     * This value may be null.
     */
    public ASTNode getElseRule() {
        return getIfRule().getNext();
    }
}
