/*	
 * SignalElement.java 
 * 
 * Last modified on $Date: 2009-07-24 10:25:41 -0400 (Fri, 24 Jul 2009) $ by $Author: rfarahbod $
 * 
 * Copyright (c) 2010 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.network.plugins.signals;

import org.coreasm.engine.absstorage.Element;

/**
 * Represents a signal.
 * 
 * @author Roozbeh Farahbod
 */
public class SignalElement extends Element {

	/** type of this signal */
	public final Element type;
	
	/** source of this signal */
	public Element src = Element.UNDEF; 
	
	/** target of this signal */
	public Element target = Element.UNDEF;
	
	/**
	 * Creates a new signal with a defined type. 
	 * If type is not defined (<code>null</code> it will be UNDEF.
	 * 
	 * @param type type of the signal, an instance of {@link Element}
	 */
	public SignalElement(Element type) {
		this(type, Element.UNDEF, Element.UNDEF);
	}
	
	/**
	 * Creates a new signal with a defined type and the given source 
	 * and targets.
	 * Any parameters that is <code>null</code> will be UNDEF.
	 * 
	 * @param type type of the signal, an instance of {@link Element}
	 * @param src source agent of the signal
	 * @param trg target agent receiving the signal
	 */
	public SignalElement(Element type, Element src, Element trg) {
		if (type == null)
			this.type = Element.UNDEF;
		else
			this.type = type;
		this.src = src;
		this.target = trg;
		if (this.src == null)
			this.src = Element.UNDEF;
		if (this.target == null)
			this.target = Element.UNDEF;
	}
	
	@Override
	public String getBackground() {
		return SignalBackgroundElement.SIGNAL_BACKGROUND_NAME;
	}

	@Override
	public String toString() {
		return "Signal:" + this.type + "(" + this.src + ", " + this.target + ")";
	}

}
