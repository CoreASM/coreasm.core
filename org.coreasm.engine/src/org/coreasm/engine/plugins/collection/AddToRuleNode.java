/*	
 * AddToRuleNode.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Mashaal Memon
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.collection;


import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.util.Logger;

/** 
 *	Wrapper class to help manipulate collection add/to rule subtree.
 *   
 *  @author  Mashaal Memon and Roozbeh Farahbod
 *  
 */
public class AddToRuleNode extends ASTNode {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddToRuleNode(ScannerInfo info) {
		super(
				CollectionPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"AddToCollectionRule",
				null,
				info);
	}
	
	/**
	 * @param node
	 */
	public AddToRuleNode(AddToRuleNode node) {
		super(node);
	}

	/**
     * Returns an unevaluated term node if any. If none exists, <code>null<code> is returned
     * 
     * @return <code>Node</code> representing a term node that has not been evaluated. If no such child exists, null is returned. 
     */
    public ASTNode getUnevaluatedTerm() {
        
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
     * Returns the value of the expression to be added to a collection.
     * 
     * @return <code>Element</code> representing the value to be added to a collection.
     */
	public Element getAddElement()
	{
		return this.getFirst().getValue();
	}
	
	/**
     * Returns the location of the collection collection.
     */
	public Location getToLocation()
	{
		Location loc = getToNode().getLocation();
		
		if (loc == null)
		{
			Logger.parser.log(Logger.WARNING,"Performing collection-add incremental update on non-location!");
		}
		
		return loc;
	}
	
	/**
     * Returns the collection node.
     * 
     */
	public ASTNode getToNode()
	{
		return this.getFirst().getNext();
	}
	

}
