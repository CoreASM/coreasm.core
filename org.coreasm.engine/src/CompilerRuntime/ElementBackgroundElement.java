/*	
 * ElementBackgroundElement.java  	$Revision: 243 $
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
 
package CompilerRuntime;

/** 
 *	This is the background of all the elements in CoreASM.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public final class ElementBackgroundElement extends BackgroundElement {

	/**
	 * Name of the Element background
	 */
	public static final String ELEMENT_BACKGROUND_NAME = "ELEMENT";

	/**
	 * Creates a new Element background.
	 */
	public ElementBackgroundElement() {
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
