/*	
 * StepRuleNode.java
 * 
 * Last modified on $Date: 2010-04-30 01:05:27 +0200 (Fr, 30 Apr 2010) $ by $Author: rfarahbod $
 *
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.step;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	Step Rule node.
 *   
 * @author  Roozbeh Farahbod
 */
public class StepRuleNode extends ASTNode {

    private static final long serialVersionUID = 1L;

    public StepRuleNode(ScannerInfo info) {
        super(StepPlugin.PLUGIN_NAME, 
        		ASTNode.RULE_CLASS,
        		"StepRule",
        		null,
        		info);
    }

    public StepRuleNode(StepRuleNode node) {
    	super(node);
    }
    
    /**
     * Returns the first rule 
     */
    public ASTNode getFirstRule() {
        return (ASTNode)getChildNode("alpha");
    }
    
    /**
     * Returns the second rule
     */
    public ASTNode getSecondRule() {
        return (ASTNode)getChildNode("beta");
    }
    
}
