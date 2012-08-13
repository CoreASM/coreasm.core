/*	
 * BagEnumerateNode.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.bag;

import java.util.ArrayList;
import java.util.Collection;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Bag enumeration AST node.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class BagEnumerateNode extends ASTNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private Collection<ASTNode> memberNodes = null;

	public BagEnumerateNode(ScannerInfo info) {
		super(
				BagPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"BagEnumerate",
				null,
				info);
	}
	
	/**
	 * @param node
	 */
	public BagEnumerateNode(BagEnumerateNode node) {
		super(node);
	}

	/**
     * Returns an unevaluated child node if any. If none exists, <code>null<code> is returned
     * 
     * @return N<code>ASTNode</code> representing a child node that has not been evaluated. If no such child exists, null is returned. 
     */
    public ASTNode getUnevaluatedMember() {
        
    		// get first child
    		ASTNode child = this.getFirst();
    		
    		// while the current child exists and has been evaluated, cycle to the next child
    		while (child != null && child.isEvaluated())
    		{
    			child = child.getNext();
    		}
    		
    		// null will be returned when no children are left unevaluated, otherwise and unevaluated
    		// child node will be returned.
    		return child;
    }
    
    /**
     * Returns the node representing the consequent of the conditional rule
     * (i.e. rule to execute if the guard is true)
     * This method caches its output and does not recompute it on second call.
     * 
     * @return <code>Collection\<ASTNode\></code> of all set member nodes.
     */
    public Collection<ASTNode> getAllMembers() {

    	if (memberNodes == null) {
            memberNodes = new ArrayList<ASTNode>();
            
	        // get first child
			ASTNode child = this.getFirst();
			
			// while the current child exists
			while (child != null)
			{
				// add to list of member nodes
				memberNodes.add(child);
				
				// get next child
				child = child.getNext();
			}
    	}
	        
        return memberNodes;
    }
}
