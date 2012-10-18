/*	
 * PlotWindowBackground.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.plotter;

import java.util.HashSet;
import java.util.Set;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/** 
 * This is the background of all plot window elements.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class PlotWindowBackground extends BackgroundElement {

	/** name of the background */
	public static final String NAME = "PLOTWINDOW";
	
	/** set of already created window elements. this set is used to 
	 * kill all windows when the <i>killAll</i> signal is received
	 * from the PlotterPlugin (which is when the enging is being terminated).
	 */
	public Set<PlotWindowElement> createdWindows;
	
	public PlotWindowBackground() {
		createdWindows = new HashSet<PlotWindowElement>();
	}
	
	/**
	 * Returns a new plot window element.
	 * 
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 */
	@Override
	public synchronized Element getNewValue() {
		PlotWindowElement w = new PlotWindowElement();
		createdWindows.add(w);
		return w;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(org.coreasm.engine.absstorage.Element)
	 */
	@Override
	protected Element getValue(Element e) {
		return (e instanceof PlotWindowElement)?BooleanElement.TRUE:BooleanElement.FALSE;
	}

	/**
	 * Sends a kill signal to all window elements, so that 
	 * they will be disposed as soon as they become invisible.
	 * 
	 * @see PlotWindowElement#killWindow()
	 */
	protected void killAll() {
		for (PlotWindowElement w: createdWindows) 
			w.killWindow();
	}
	
}
