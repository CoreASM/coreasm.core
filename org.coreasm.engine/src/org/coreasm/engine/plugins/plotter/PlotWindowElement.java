/*	
 * PlotWindowElement.java 	1.0 	$Revision: 243 $
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

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;

/** 
 * This is the Plot Window element in the CoreASM state. 
 * This element can be used through the 'plot' rule to 
 * plot various functions in one window.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class PlotWindowElement extends Element {

	/* reference to a Plot Window frame */
	private final PlotWindow window;
	
	public PlotWindowElement() {
		window = new PlotWindow();
	}

	public String getBackground() {
		return PlotWindowBackground.NAME;
	}
	
	/**
	 * Adds another function to the plot window 
	 */
	public void addFunction(FunctionElement f, String name) {
		window.addFunction(f, name);
	}
	
	/**
	 * Sets the visibility of its plot window
	 * 
	 * @see PlotWindow#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		window.setVisible(b);
		if (b) 
			window.repaint();
	}

	/**
	 * Sends a kill signal to its plot window.
	 * 
	 * @see PlotWindow#setKilled(boolean)
	 *
	 */
	public void killWindow() {
		window.setKilled(true);
	}

}
