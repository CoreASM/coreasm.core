/*	
 * PickExpNode.java 	1.0 	$Revision: 80 $
 * 
 *
 * Copyright (C) 2008 Roozbeh Farahbod
 * 
 * Last modified on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $ by $Author: rfarahbod $
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.chooserule;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	This is an {@link ASTNode} for pick-from expressions.
 *   
 *  @author  Roozbeh Farahbod
 */

public class PickExpNode extends ASTNode {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PickExpNode(ScannerInfo info) {
        super(
        		ChooseRulePlugin.PLUGIN_NAME,
        		ASTNode.EXPRESSION_CLASS,
        		"PickExp",
        		null,
        		info);
    }

    public PickExpNode(PickExpNode node) {
    	super(node);
    }
    
    /**
     * Returns the node representing the bound variable of the 'pick' expression
     */
    public ASTNode getVariable() {
        return getFirst();
    }
    
    /**
     * Returns the node representing the domain of the 'pick' expression
     */
    public ASTNode getDomain() {
        return getVariable().getNext();
    }
    
    /**
     * Returns the node representing the condition of the 'pick' expression.
     */
    public ASTNode getCondition() {
        return getDomain().getNext();
    }
}
