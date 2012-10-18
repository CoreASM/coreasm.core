/*	
 * ExtendedFunctionRuleTermNode.java
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.kernelextensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	This is an {@link ASTNode} for extended rule call nodes.
 *   
 *  @author  Roozbeh Farahbod
 */

public class ExtendedRuleCallNode extends ASTNode {

    private static final long serialVersionUID = 1L;
	private List<ASTNode> argsList = null;

    public ExtendedRuleCallNode(ScannerInfo info) {
        super(
        		KernelExtensionsPlugin.PLUGIN_NAME,
        		ASTNode.RULE_CLASS,
        		KernelExtensionsPlugin.EXTENDED_RULE_CALL_NAME,
        		null,
        		info);
    }

    public ExtendedRuleCallNode(ExtendedRuleCallNode node) {
    	super(node);
    }
    
    /**
     * Returns the node representing the basic function-rule term
     */
    public ASTNode getTerm() {
        return getFirst();
    }
    
    /**
	 * Returns the list of arguments in a <code>List</code> object.
	 * This method caches the result of its first call, assuming that
	 * the node structure does not change.
	 */
	public List<ASTNode> getArguments() {
		if (argsList == null) {
			List<Node> args = this.getChildNodes("lambda");
			if (args.size() == 0)
				argsList = Collections.emptyList();
			else {
				argsList = new ArrayList<ASTNode>();
				for (Node n: args) 
					if (n instanceof ASTNode)
						argsList.add((ASTNode)n);
			}
		}

		return argsList;
	}
	
}
