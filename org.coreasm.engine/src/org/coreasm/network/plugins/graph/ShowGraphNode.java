/*	
 * ShowGraphNode.java 
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
 
package org.coreasm.network.plugins.graph;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	A node for showgraph rules.
 *   
 *  @author  Roozbeh Farahbod
 */

public class ShowGraphNode extends ASTNode {

    private static final long serialVersionUID = 1L;
    
    public ShowGraphNode(ShowGraphNode node) {
		super(node);
	}

	public ShowGraphNode(ScannerInfo scannerInfo) {
		super(GraphPlugin.PLUGIN_NAME, 
				ASTNode.RULE_CLASS, 
				GraphPlugin.SHOW_GRAPH_RULE_NAME, 
				null, 
				scannerInfo);
	}

    /**
     * Returns the node representing the graph
     */
    public ASTNode getGraphNode() {
        return getFirst();
    }

    public boolean isLocationValue() {
    	final String token = getChildNodes().get(2).getToken(); 
    	return (token != null && token.equals("at"));
    }
}
