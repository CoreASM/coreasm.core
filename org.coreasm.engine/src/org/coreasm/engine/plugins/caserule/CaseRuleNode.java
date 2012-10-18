/*	
 * CaseRuleNode.java 	1.0 	$Revision: 243 $
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Copyright (C) 2009 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.caserule;

import java.util.HashMap;
import java.util.Map;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	A parse node for case rules.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class CaseRuleNode extends ASTNode {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new CaseRuleNode
     */
    public CaseRuleNode(ScannerInfo info) {
        super(CaseRulePlugin.PLUGIN_NAME, 
        		ASTNode.RULE_CLASS,
        		"CaseRule",
        		null,
        		info);
    }

    public CaseRuleNode(CaseRuleNode node) {
    	super(node);
    }
    
    public ASTNode getCaseTerm() {
    	return (ASTNode)getChildNode("alpha");
    }
    
    /**
     * Returns a map of case guards to their corresponding rules
     * 
     * @throws Exception 
     */
    public Map<ASTNode, ASTNode> getCaseMap() {
    	Map<ASTNode, ASTNode> caseMap = new HashMap<ASTNode, ASTNode>();
         
        ASTNode current = (ASTNode)getChildNode("beta");
        
        while (current != null) {
        	caseMap.put(current,current.getNext());
            current = current.getNext().getNext();
        }
        return caseMap;
    }

}
