/*	
 * NewEdgeNode.java 
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
 *	NewEdgeNode is a Node for new edge term.
 *   
 *  @author  Roozbeh Farahbod
 */

public class NewEdgeNode extends ASTNode {

    private static final long serialVersionUID = 1L;
    
    public NewEdgeNode(NewEdgeNode node) {
		super(node);
	}

	public NewEdgeNode(ScannerInfo scannerInfo) {
		super(GraphPlugin.PLUGIN_NAME, 
				ASTNode.EXPRESSION_CLASS, 
				GraphPlugin.NEW_EDGE_TERM_NAME, 
				null, 
				scannerInfo);
	}

    /**
     * Returns the node representing the vertices
     */
    public ASTNode getVertices() {
        return getFirst();
    }
    
}
