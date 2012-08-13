/*	
 * StepBlockRuleNode.java
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
 *	Step Block Rule node.
 *   
 * @author  Roozbeh Farahbod
 */
public class StepBlockRuleNode extends ASTNode {

    private static final long serialVersionUID = 1L;

    public StepBlockRuleNode(ScannerInfo info) {
        super(StepPlugin.PLUGIN_NAME, 
        		ASTNode.RULE_CLASS,
        		"StepBlockRule",
        		null,
        		info);
    }

    public StepBlockRuleNode(StepBlockRuleNode node) {
    	super(node);
    }
    
}
