/*	
 * NumberRangeNode.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 George Ma
 * Copyright (c) 2007 Roozbeh Farahbod
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.number;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	
 *   
 *  @author  George Ma and Roozbeh Farahbod
 *  
 */
public class NumberRangeNode extends ASTNode {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new Number Range Node
     */
    public NumberRangeNode(ScannerInfo info) {
        super(
        		NumberPlugin.PLUGIN_NAME,
        		ASTNode.EXPRESSION_CLASS,
        		"NumberRangeTerm",
        		null,
        		info);
    }

    public NumberRangeNode(NumberRangeNode node) {
    	super(node);
    }

    /**
     * Returns the node representing the guard of the conditional rule
     */
    public ASTNode getStart() {
        return getFirst();
    }
    
    /**
     * Returns the node representing the consequent of the conditional rule
     * (i.e. rule to execute if the guard is true)
     */
    public ASTNode getEnd() {
        return getStart().getNext();
    }
    
    /**
     * Returns the node representing the 'else' part the conditional rule.
     * (i.e. rule to execute if the guard is false)
     * This value may be null.
     */
    public ASTNode getStep() {
        return getEnd().getNext();
    }
}
