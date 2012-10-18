/*
 * SignalBackgroundElement.java 
 * 
 * Copyright (c) 2010 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.network.plugins.signals;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.NameElement;

/**
 * Signal background element.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class SignalBackgroundElement extends BackgroundElement {

	/** Name of the Signal background */
	public static final String SIGNAL_BACKGROUND_NAME = "SIGNAL";
	
	/** A default type */
	public static final NameElement DEFAULT_SIGNAL_TYPE = new NameElement("Generic");

	/**
	 * Creates a new Signal background.
	 * 
	 * @see #SIGNAL_BACKGROUND_NAME 
	 */
	public SignalBackgroundElement() {
	}
	
	@Override
	public Element getNewValue() {
		return new SignalElement(DEFAULT_SIGNAL_TYPE);
	}
	
	/** 
	 * Returns a <code>TRUE</code> boolean for 
	 * Signal Elements. Otherwise <code>FALSE<code> is returned.
	 * 
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(Element)
	 * @see BooleanElement
	 */
	@Override
	protected BooleanElement getValue(Element e) {
		return (e instanceof SignalElement)?BooleanElement.TRUE:BooleanElement.FALSE;
	}
}
