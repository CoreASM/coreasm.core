/*	
 * ForallRuleNode.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 George Ma
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.forallrule;

import java.util.Map;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	ForallRuleNode is a NodeWrapper for forall rule nodes.
 *   
 *  @author  George Ma, Michael Stegmaier
 *  
 */

public class ForallRuleNode extends ASTNode {

    /**
     * Creates a new ForallRuleNode
     */
    public ForallRuleNode(ScannerInfo info) {
        super(
        		ForallRulePlugin.PLUGIN_NAME,
        		ASTNode.RULE_CLASS,
        		"ForallRule",
        		null,
        		info);
    }

    public ForallRuleNode(ForallRuleNode node) {
    	super(node);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private VariableMap variableMap;
    
    @Override
	public void addChild(String name, Node node) {
		if (node instanceof ASTNode) {
			ASTNode astNode = (ASTNode)node;
			if (ASTNode.ID_CLASS.equals(astNode.getGrammarClass())) {
				for (ASTNode current = getFirst(); current != null && current.getNext() != null && ASTNode.ID_CLASS.equals(current.getGrammarClass()); current = current.getNext().getNext()) {
		            if (astNode.getToken().equals(current.getToken())) {
		            	super.addChild(name, node);
		            	throw new CoreASMError("Variable \""+current.getToken()+"\" already defined in forall rule.", node);
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
     * Returns the node representing the 'do' part of the forall rule.
     */
    public ASTNode getDoRule() {
        return (ASTNode)getChildNode("rule");   
    }
    
    /**
     * Returns the node representing the 'ifnone' part of the forall rule.
     */
    public ASTNode getIfnoneRule() {
        return (ASTNode)getChildNode("ifnone");
    }
    
    /**
     * Returns the node representing the 'with' part of the forall rule.
     * If there is no 'with' condition specified, null is returned.
     */
    public ASTNode getCondition() {
    	return (ASTNode)getChildNode("guard"); 
    }

}
