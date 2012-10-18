/*  
 * UniverseNode.java    1.0     $Revision: 243 $
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Copyright (C) 2006 George Ma
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.signature;


import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	Node for Universe definitions
 *   
 *  @author  George Ma
 *  
 */
public class UniverseNode extends ASTNode {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new EnumerationNode
     */
    public UniverseNode(ScannerInfo info) {
        super(
        		SignaturePlugin.PLUGIN_NAME,
        		ASTNode.DECLARATION_CLASS,
        		"UniverseDefinition",
        		null,
        		info
        		);
    }

    public UniverseNode(UniverseNode node) {
    	super(node);
    }
    
    /**
     * Returns the name of the universe
     * @return the name of the universe
     */
    public String getName() {
        return getFirst().getToken();
    }
}
