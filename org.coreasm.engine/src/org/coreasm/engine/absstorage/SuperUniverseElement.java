/*	
 * SuperUniverseElement.java 	1.0 	$Revision: 243 $
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
 *	This class implements the super universe background.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
@Deprecated
public final class SuperUniverseElement extends BackgroundElement {

	/**
	 * Name of the super universe background
	 */
	public static final String SUPER_UNIVERSE_NAME = "SUPER_UNIVERSE";

	/**
	 * Creates a new Super Universe background.
	 * The name of this background is defined by
	 * <code>SUPER_UNIVERSE_NAME</code>.
	 * 
	 * @see BackgroundElement#BackgroundElement()
	 */
	public SuperUniverseElement() {
		super();
	}

	/**
	 * Returns a new Element.
	 */
	@Override
	public Element getNewValue() {
		return new Element();
	}

	/** 
	 * Returns <code>TRUE</code> for any Element.
	 * 
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(java.util.List)
	 */
	@Override
	protected BooleanElement getValue(Element e) {
		return BooleanElement.TRUE;
	}

}
