/*	
 * Enumerable.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import java.util.Collection;
import java.util.List;

/** 
 * This interface will be implemented by Elements that 
 * can represent themselves with a multiset (a collection).
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public interface Enumerable {
	
	/**
	 * Provides a <code>Collection</code> representation 
	 * of the object that implements this interface.
	 * 
	 * @return a collection of Elements
	 */
	public Collection<? extends Element> enumerate();
    
    /**
     * Returns true of the enumerable object contains the specified element;
     * false otherwise.
     * 
     * A common implementation of this method may be:
     * <code>
     * public boolean contains(Element e) {
     *   return enumerate().contains(e);
     * }
     * </code>
     * 
     * However, that implementation may be inefficient in some cases.
     * 
     * @param e
     */
    public boolean contains(Element e);

    /**
     * @return the size of this enumerable. 
     */
    public int size();
    
    /** 
     * Returns <code>true</code> if and only if this element can provide
     * an indexed view of itself; i.e., if the {@link #getIndexedView()} 
     * does not throw an {@link UnsupportedOperationException}; otherwise, 
     * it should return <code>false</code>.
     */
    public boolean supportsIndexedView();
    
    /**
     * If supported, gives an indexed view of this enumerable.
     * Such type-specific views can improve performance 
     * in any rule or expression that chooses or iterates over the
     * elements.
     * <p>
     * The output of this method should be consistent with {@link #supportsIndexedView()}.  
     *  
     * @return a list of elements 
     * @throws UnsupportedOperationException if this method is not supported. 
     */
    public List<Element> getIndexedView() throws UnsupportedOperationException;
    
    
}
