/*	
 * EnumerationElement.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 George Ma
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.compiler.plugins.signature.include;

import org.coreasm.engine.absstorage.Element;

/** 
 *	Class for elements of Enumeration backgrounds
 *   
 *  @author  George Ma, Roozbeh Farahbod
 *  
 */
public class EnumerationElement extends Element {

	/** name of this element */
    private String name;
    
    private String backgroundName = null;
    
    /**
     * Initializes the enumeration element
     * @param name The name of the element
     */
    public EnumerationElement(String name) {
        super();
        this.name = name;
    }

    /**
     * @return the name of the element
     */
    public String getName() {
        return name;
    }
    
    public String toString() {
    	return name;
    }
    
    public String getBackground() {
    	if (backgroundName == null)
    		return super.getBackground();
    	else
    		return backgroundName;
    }
    
    /**
     * Sets the background of this element.
     * Unfortunately needs to be public, but no plugin
     * other than the signature plugin should ever generate code modifying the background
     * @param name The name of the background
     */
    public void setBackground(String name) {
    	this.backgroundName = name;
    }
}
