/*	
 * ForallExpNode.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 George Ma
 * Copyright (C) 2007 Roozbeh Farahbod
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.predicatelogic;

import java.util.Map;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	ForallExpNode is a Node for forall expressions.
 *   
 *  @author  George Ma and Roozbeh Farahbod
 *  
 */

public class ForallExpNode extends ASTNode {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private VariableMap variableMap;

    /**
     * Creates a new ForallExpNode
     */
    public ForallExpNode(ScannerInfo info) {
        super(
        		PredicateLogicPlugin.PLUGIN_NAME,
        		ASTNode.EXPRESSION_CLASS,
        		"ForallExp",
        		null,
        		info);
    }
    
    public ForallExpNode(ForallExpNode node) {
    	super(node);
    }
    
    @Override
	public void addChild(String name, Node node) {
		if (node instanceof ASTNode) {
			ASTNode astNode = (ASTNode)node;
			if (ASTNode.ID_CLASS.equals(astNode.getGrammarClass())) {
				for (ASTNode current = getFirst(); current != null && current.getNext() != null && ASTNode.ID_CLASS.equals(current.getGrammarClass()); current = current.getNext().getNext()) {
		            if (astNode.getToken().equals(current.getToken())) {
		            	super.addChild(name, node);
		            	throw new CoreASMError("Variable \""+current.getToken()+"\" already defined in forall expression.", node);
		            }
		        }
			}
		}
		super.addChild(name, node);
	}
    
    /**
     * Returns a map of the variable names to the nodes which
     * represent the domains that variable should be taken from
     */
    public Map<String,ASTNode> getVariableMap() {
    	if (variableMap != null)
    		return variableMap;
    	return variableMap = new VariableMap(this);
    }

    /**
     * Returns the node representing the condition of the forall expression.
     */
    public ASTNode getCondition() {
    	ASTNode current = getFirst();
    	while (current.getNext() != null && current.getNext().getNext() != null && ASTNode.ID_CLASS.equals(current.getGrammarClass()))
    		current = current.getNext().getNext();
    	return current;
    }

}
