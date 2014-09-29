/*	
 * AbstractUniverse.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import java.util.List;

/** 
 *	Abstract class to define Universe elements or any element that also represents a universe.
 *   
 *  @author  Roozbeh Farahbod
 *  
 *  @see UniverseElement
 *  @see BackgroundElement
 */
public abstract class AbstractUniverse extends FunctionElement {

	/**
	 * Creates a new Universe element. 
	 * The default value will be <code>BooleanElement.FALSE</code>.
	 * 
	 * @see FunctionElement#FunctionElement(Element)
	 * @see BooleanElement#FALSE
	 */
	public AbstractUniverse() {
		super(BooleanElement.FALSE);
	}

//	public Signature defaultSignature() {
//		ArrayList<String> temp = new ArrayList<String>();
//		temp.add(SuperUniverseElement.SUPER_UNIVERSE_NAME);
//		temp.add(BooleanBackgroundElement.BOOLEAN_BACKGROUND_NAME);
//		return temp;
//	}

	/** 
	 * Returns a <code>Boolean</code> Element indicating whether the
	 * given Element is in this Universe or not.
	 * 
	 * @param args A list, with only one Element
	 * @return a <code>TRUE</code> Element if the given Element is in this
	 * univers; a <code>FALSE</code> Element, otherwise. If there is no argument
	 * the universe element is returned instead.
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 1) 
			return getValue(args.get(0));
		else
			return this;
	}

	/**
	 * Returns the value of the membership function 
	 * for this universe.
	 * 
	 * @param value a given Element 
	 * @return <code>true</code> if the given Element is 
	 * in this universe; <code>false</code> otherwise.
	 */
	public boolean member(Element value) {
		Element be = getValue(value);
		if (be instanceof BooleanElement)
			return ((BooleanElement)be).getValue();
		else
			return false;
	}

	/**
	 * Returns a <code>Boolean</code> Element indicating whether the
	 * given Element is in this Universe or not.
	 * 
	 * @param e element
	 */
	protected abstract Element getValue(Element e); 

	public String toString() {
		return "universe-element";
	}
	
}
