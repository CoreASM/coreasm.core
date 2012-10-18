/*	
 * SignalRuleNode.java 
 * 
 * Last modified on $Date: 2009-07-24 10:25:41 -0400 (Fri, 24 Jul 2009) $ by $Author: rfarahbod $
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
 *	SignalRuleNode is a Node for signal rule nodes.
 *   
 *  @author  Roozbeh Farahbod
 */

public class SignalRuleNode extends ASTNode {

    private static final long serialVersionUID = 1L;
    
    public SignalRuleNode(SignalRuleNode node) {
		super(node);
	}

	public SignalRuleNode(ScannerInfo scannerInfo) {
		super(SignalsPlugin.PLUGIN_NAME, 
				ASTNode.RULE_CLASS, 
				"SignalRule", 
				null, 
				scannerInfo);
	}

    /**
     * Returns the node representing the target agent
     */
    public ASTNode getTargetAgent() {
        return getFirst();
    }
    
    /**
     * Returns the node representing the type of the signal
     */
    public ASTNode getType() {
        return getTargetAgent().getNext();
    }

    /**
     * Returns the node representing optional variable
     */
    public ASTNode getVariable() {
        return (ASTNode)getChildNode(SignalsPlugin.VARIABLE_NODE_NAME);
    }
    
    /**
     * Returns the node representing the optional rule
     */
    public ASTNode getDoRule() {
        return (ASTNode)getChildNode(SignalsPlugin.RULE_NODE_NAME);
    }
    
}
