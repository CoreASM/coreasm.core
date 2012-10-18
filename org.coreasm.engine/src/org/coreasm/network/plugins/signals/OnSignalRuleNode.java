/*	
 * OnSignalRuleNode.java 
 * 
 * Copyright (c) 2010 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.network.plugins.signals;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	OnSignalRuleNode is a Node for onsignal rule nodes.
 *   
 *  @author  Roozbeh Farahbod
 */

public class OnSignalRuleNode extends ASTNode {

    private static final long serialVersionUID = 1L;
    
    public OnSignalRuleNode(OnSignalRuleNode node) {
		super(node);
	}

	public OnSignalRuleNode(ScannerInfo scannerInfo) {
		super(SignalsPlugin.PLUGIN_NAME, 
				ASTNode.RULE_CLASS, 
				"OnSignalRule", 
				null, 
				scannerInfo);
	}

    /**
     * Returns the node representing the variable
     */
    public ASTNode getVariable() {
        return getFirst();
    }
    
    /**
     * Returns the node representing the domain of the signal
     */
    public ASTNode getType() {
        return getVariable().getNext();
    }

    /**
     * Returns the node representing the rule
     */
    public ASTNode getDoRule() {
        return getType().getNext();
    }
}
