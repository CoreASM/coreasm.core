/*  
 * EnumerationNode.java    1.0     $Revision: 243 $
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

import java.util.ArrayList;
import java.util.List;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	Node for Enumeration definitions
 *   
 *  @author  George Ma
 *  
 */
public class EnumerationNode extends ASTNode {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new EnumerationNode with the given
     * scanner information.
     */
    public EnumerationNode(ScannerInfo info) {
        super(
        		SignaturePlugin.class.getSimpleName(),
        		ASTNode.DECLARATION_CLASS,
        		"EnumerationDefinition",
        		null,
        		info);
    }

    public EnumerationNode(EnumerationNode node) {
    	super(node);
    }

    
    /**
     * Returns the name of the enumeration
     * @return the name of the enumeration
     */
    public String getName() {
        return getFirst().getToken();
    }
    
    /**
     * Returns a List of the members of the enumeration
     * @return a List of the members of the enumeration
     */
    public List<EnumerationElement> getMembers() {
        List<EnumerationElement> members = new ArrayList<EnumerationElement>();
        
        ASTNode member = getFirst().getNext();        
        
        while (member != null) {
            //members.add(new EnumerationElement(member.getFirst().getToken()));
            members.add(new EnumerationElement(member.getToken()));
            member = member.getNext();
        }
        
        return members;
    }
}
