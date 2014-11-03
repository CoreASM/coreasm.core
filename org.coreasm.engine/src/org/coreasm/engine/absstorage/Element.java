/*	
 * Element.java 	1.0 	$Revision: 243 $
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

import java.io.Serializable;

import org.coreasm.engine.ControlAPI;

/** 
 *	The root class of Abstract Object Values (later changed 
 *  to Elements) in the abstract storage.
 *  
 *  @author  Roozbeh Farahbod
 *  
 */
 public class Element implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
 	 * This value is used to automatically generate
 	 * general Element names. 
 	 */
 	private static long lastElementNo = 1; 
	 
 	/**
 	 * Represents the 'undef' value in ASM.
 	 */
 	public static final Element UNDEF = new Element(-1);
 	
 	/**
 	 * A unique id
 	 */
	public final long id;
	
 	/**
 	 * A private constructor used to create UNDEF.
 	 *  
 	 */
	private Element(long id) {
 		this.id = id;
 	}
	
	/**
	 * Creates a new Element.
	 *
	 */
 	public Element() {
		lastElementNo++;
		this.id = lastElementNo;
	}
	
 	/**
 	 * Returns the name of the background of this element
 	 * (in the state). This method should be overridden 
 	 * by elements from special backgrounds.
 	 */
 	public String getBackground() {
 		return ElementBackgroundElement.ELEMENT_BACKGROUND_NAME;
 	}
 	
 	/*
 	 * Returns the class of the background of this element
 	 * (in the state). This method should be overridden 
 	 * by elements of special backgrounds.
 	 *
 	public Class<? extends BackgroundElement> getBackgroundClass() {
 		return ElementBackgroundElement.class;
 	}
 	*/
 	
 	/**
 	 * If this element has a background (see {@link BackgroundElement}),
 	 * it asks the background (through Control API) to provide a 
 	 * new instance of the elements provided by that background. 
 	 * There is no guarantee that the returned value is from the same 
 	 * type of this element. 
 	 * <p>
 	 * If the element has no background, <code>null</code> is returned.
 	 * 
 	 * @param capi reference to the Control API of the engine
 	 * 
 	 * @see BackgroundElement#getNewValue()
 	 */
 	public final Element getNewInstance(ControlAPI capi) {
 		Element result = null;
 		AbstractUniverse u = capi.getStorage().getUniverse(this.getBackground());
		if (u != null && (u instanceof BackgroundElement)) {
			BackgroundElement bkg = (BackgroundElement)u;
			result = bkg.getNewValue();
		}
		return result;
 	}
 	
 	/**
 	 * Compares this Element to the specified Element. 
 	 * The result is <code>true</code> if the argument 
 	 * is not null and is considered to be equal to this Element.
 	 * 
 	 * @param anElement the Element to compare with.
 	 * @return <code>true</code> if the Elements are equal; <code>false</code> otherwise.
 	 * @throws IllegalArgumentException if <code>anElement</code> is not an instance
 	 * of <code>Element</code>
 	 */
 	public boolean equals(Object anElement) {
 		if (anElement instanceof Element)
 			return (((Element)anElement).id == this.id);
 		else
 			throw new IllegalArgumentException("Cannot compare to non-Elements.");
 	}
 	

 	/** 
 	 * Returns the denotational form of this element. 
 	 * By default, this is the same as <code>toString()</code>
 	 * but elements can override this method to provide
 	 * a more accurate denotation of their value.
 	 */
 	public String denotation() {
 		return this.toString();
 	}
 	
 	/** 
	 * Returns a <code>String</code> representation of 
	 * this Element.
	 *  
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (id == -1)
			return "undef";
		else
			return getClass().getSimpleName() + id;
	}

 }
 
