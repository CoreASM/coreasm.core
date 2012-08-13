/*	
 * SetEnumerateNode.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Mashaal Memon
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.set;

import java.util.Collection;
import java.util.Vector;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	Wrapper clas to help manipulate set enumeration node subtree.
 *   
 *  @author  Mashaal Memon, Roozbeh Farahbod
 *  
 */
public class SetEnumerateNode extends ASTNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SetEnumerateNode(ScannerInfo info) {
		super(
				SetPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"SetEnumerate",
				null,
				info);
	}
	
	/**
	 * @param node
	 */
	public SetEnumerateNode(SetEnumerateNode node) {
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
     * 
     * @return <code>Collection\<ASTNode\></code> of all set member nodes.
     */
    public Collection<ASTNode> getAllMembers() {
        Vector<ASTNode> memberNodes = new Vector<ASTNode>();
        
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
        
        return memberNodes;
    }

}
