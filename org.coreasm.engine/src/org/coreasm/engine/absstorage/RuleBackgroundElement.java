/*	
 * RuleBackgroundElement.java 	1.0 	$Revision: 243 $
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

/** 
 *	Class of Rule Background Element. There should only be
 *  one instance of this class in each state.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class RuleBackgroundElement extends BackgroundElement {

	/**
	 * Name of the Rule background
	 */
	public static final String RULE_BACKGROUND_NAME = "RULE";

	/**
	 * Creates a new Rule background.
	 * 
	 * @see #RULE_BACKGROUND_NAME 
	 */
	public RuleBackgroundElement() {
		super();
	}

	/**
	 * Overriden to prevent creation of any new rule.
	 * 
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public Element getNewValue() {
		throw new UnsupportedOperationException("Cannot create new rule.");
	}

	/** 
	 * Returns a <code>TRUE</code> boolean for 
	 * Rule Elements.
	 * 
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(java.util.List)
	 * @see BooleanElement
	 */
	@Override
	protected BooleanElement getValue(Element e) {
		return (e instanceof RuleElement)?BooleanElement.TRUE:BooleanElement.FALSE;
	}

}
