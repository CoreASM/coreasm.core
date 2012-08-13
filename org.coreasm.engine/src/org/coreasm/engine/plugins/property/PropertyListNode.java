/*	
 * PropertyNode.java 	__VERSION__ 	$Revision: 243 $
 * 
 * Copyright (C) 2007 __AUTHOR__
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.property;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * TODO:Comments
 *   
 * @author  __AUTHOR__
 * 
 */
public class PropertyListNode extends ASTNode {

	private int propertyCount = 0;
    private boolean hasCheck = false;
    
    private static final long serialVersionUID = 1L;

    public void setHasCheck(boolean hasCheck) {
        this.hasCheck = hasCheck;
    }

    public PropertyListNode(ScannerInfo info) {
        super(PropertyPlugin.PLUGIN_NAME,
        		ASTNode.DECLARATION_CLASS,
        		"PropertyList",
        		null,
        		info
        		);
    }

    public void incrementPropertyCount() {
        if (!hasCheck) {
            propertyCount++;
        }
    }

    public int getPropertyIndex() {
        if (!hasCheck) {
            return 0;
        }
        
        return propertyCount;
    }
}
