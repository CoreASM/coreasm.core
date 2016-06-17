/*	
 * LetRuleNode.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 George Ma
 * Copyright (C) 2007 Roozbeh Farahbod
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.letrule;

import java.util.HashMap;
import java.util.Map;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.plugins.turboasm.TurboASMPlugin;

/** 
 *	CondtionalRuleNode is a NodeWrapper for conditional (ifThen) nodes.
 *   
 *  @author  George Ma, Roozbeh Farahbod
 *  
 */
public class LetRuleNode extends ASTNode {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new LetRuleNode
     */
    public LetRuleNode(ScannerInfo info) {
        super(
        		LetRulePlugin.PLUGIN_NAME,
        		ASTNode.RULE_CLASS,
        		"LetRule",
        		null,
        		info);
    }

    public LetRuleNode(LetRuleNode node) {
    	super(node);
    }
    
    public boolean isLetResultRule() {
    	return TurboASMPlugin.RETURN_RESULT_TOKEN.equals(getFirst().getNextCSTNode().getToken());
    }

    /**
     * Returns a map of the variable names to the nodes which
     * represent the terms that will be aliased
     * @throws Exception 
     */
    public Map<String,ASTNode> getVariableMap() throws CoreASMError {
    	Map<String,ASTNode> variableMap = new HashMap<String,ASTNode>();
        
        for (ASTNode current = getFirst(); current.getNext() != null && current.getNext().getNext() != null && ASTNode.ID_CLASS.equals(current.getGrammarClass()); current = current.getNext().getNext()) {
            if (variableMap.put(current.getToken(),current.getNext()) != null)
                throw new CoreASMError("Token \""+current.getToken()+"\" already defined in let rule.", this);
        }
        
        return variableMap;
    }
       
    /**
     * Returns the node representing the 'in' part the let rule.
     */
    public ASTNode getInRule() {
        return (ASTNode)getChildNode("gamma");
    }
    
}
