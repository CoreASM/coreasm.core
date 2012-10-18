/*	
 * ExistsExpNode.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 George Ma
 * Copyright (C) 2007 Roozbeh Farahbod
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.predicatelogic;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	ExistsExpNode is a Node for exists expressions.
 *   
 *  @author  George Ma and Roozbeh Farahbod
 *  
 */

public class ExistsExpNode extends ASTNode {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new ExistsExpNode
     */
    public ExistsExpNode(ScannerInfo info) {
        super(
        		PredicateLogicPlugin.PLUGIN_NAME,
        		ASTNode.EXPRESSION_CLASS,
        		"ExistsExp",
        		null,
        		info);
    }

    public ExistsExpNode(ExistsExpNode node) {
    	super(node);
    }
    
    /**
     * Returns the node representing the bound variable of the Exists expression
     */
    public ASTNode getVariable() {
        return getFirst();
    }
    
    /**
     * Returns the node representing the domain of the Exists expression
     */
    public ASTNode getDomain() {
        return getVariable().getNext();
    }
    
    /**
     * Returns the node representing the condition of the Exists expression.
     */
    public ASTNode getCondition() {
        return getDomain().getNext();
    }
}
