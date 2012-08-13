/*	
 * Signature.java 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005-2007 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 
 *	Holds the signature of a function.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class Signature {
    
    private List<String> domain;
    private String range;
    
    /**
     * Creates a new signature of the form:
     * <p>
     * nil -> ELEMENT
     *
     */
	public Signature() {
       domain = Collections.emptyList();
       range = ElementBackgroundElement.ELEMENT_BACKGROUND_NAME;
    }
	
	/**
	 * Creates a new signature of the form:
	 * <p>
	 * signature[0] * signature[1] * ... * signature[n-1] -> signature[n]
	 * 
	 * @param signature an array of domain names ended with the range name
	 */
	public Signature(String ... signature) {
		range = signature[signature.length - 1];
		if (signature.length > 1) {
			List<String> list = new ArrayList<String>();
			for (int i=0; i < signature.length -1; i++) {
				list.add(signature[i]);
			}
			domain = Collections.unmodifiableList(list);
		} else
			domain = Collections.emptyList();
	}

    /**
     * Creates a new signature of the form:
     * <p>
     * ELEMENT * ELEMENT * ... * ELEMENT -> ELEMENT
     *
     * @param arity the number of elements in the domain
     */
	public Signature(int arity) {
		if (arity == 0)
			domain = Collections.emptyList();
		else {
			domain = new ArrayList<String>();
			for (int i=0; i < arity; i++)
				domain.add(ElementBackgroundElement.ELEMENT_BACKGROUND_NAME);
			domain = Collections.unmodifiableList(domain);
		}
       range = ElementBackgroundElement.ELEMENT_BACKGROUND_NAME;
    }

	/**
	 * Returns the domain of this signature
	 * as a list of universe names.
	 */
    public List<String> getDomain() {
        return domain;
    }

    /**
     * Sets the domain of this signature.
     */
    public void setDomain(List<String> domain) {
        if (domain != null)
        	this.domain = Collections.unmodifiableList(domain);
        else 
        	this.domain = Collections.emptyList();
    }

    /**
     * Sets the domain of this signature.
     */
    public void setDomain(String ... domain) {
    	if (domain.length == 0) 
    		this.domain = Collections.emptyList();
    	else {
    		this.domain = new ArrayList<String>();
    		for (String s: domain)
    			this.domain.add(s);
    		this.domain = Collections.unmodifiableList(this.domain);
    	}
    }
    
    /**
     * Returns the range of this signature as
     * a name of a universe.
     */
    public String getRange() {
        return range;
    }

    /**
     * Sets the range of this signature.
     *  
     * @param range the name of the range universe
     */
    public void setRange(String range) {
        this.range = range;
    }
 
    /**
     * Returns the arity of this signature.
     */
    public int getArity() {
    	return domain.size();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String ret = "";
        
        if (domain != null) {
            for (int i = 0; i < domain.size(); i++) {
                if (i == 0) {
                    ret += domain.get(i);
                }
                else {
                    ret += " x " + domain.get(i);
                }
            }
        }
        
        ret += " -> " + range;
        
        return ret;
    }
}
