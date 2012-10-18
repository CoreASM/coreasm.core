/*	
 * NumberRangeNode.java 	1.0 	$Revision: 243 $
 * 
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
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	Number term nodes are literal nodes holding a number literal.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class NumberTermNode extends ASTNode {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new Number Term Node
     */
    public NumberTermNode(ScannerInfo info, String token) {
        super(
        		NumberPlugin.PLUGIN_NAME,
        		ASTNode.EXPRESSION_CLASS,
        		"NUMBER",
        		token,
        		info,
        		Node.LITERAL_NODE);
    }

    public NumberTermNode(NumberTermNode node) {
    	super(node);
    }

}
