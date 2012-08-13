/*	
 * InfoPanel.java 	1.0 	$Revision: 80 $
 * 
 * Copyright (C) 2008 Mona Vajihollahi
 * 					  Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.plotter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.plugins.number.NumberElement;
import org.coreasm.util.Tools;

public class InfoPanel extends JPanel {
	/* keeps the mapping of functions to colors */
	
	private Map<FunctionElement, Color> fColors;
	private Map<FunctionElement, String> fNames; 
	private static final int MINIMUM_WIDTH = 100;
	private static final int MINIMUM_HEIGHT = 500;
	

	public InfoPanel() {
		super();
		initComponent();
		fColors = new HashMap<FunctionElement,Color>();
		fNames = new HashMap<FunctionElement, String>();
	}
	
	/**
	 * Initializes this panel
	 */
	private void initComponent() {
		setBackground(Color.WHITE);
		setBorder(new javax.swing.border.EtchedBorder());
		//setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
		setPreferredSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
	}
	
	public void paint(Graphics g) {
		super.paint(g);

		int w = this.getWidth();
		int h = this.getHeight();
		int delta = 25;
		int hp = h - 2 * delta;
		int wp = w - 2 * delta;
		
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		
		int x = 10;
		int y = 30;

		// Draw "Legend"
		g.setColor(Color.black);
		g.drawString("Legend", x, y - 10);
		g.drawLine(x, y, MINIMUM_WIDTH - x, y);
		y += 25;
		x += 10;
		
		for (FunctionElement f: fColors.keySet()) {
			Color color = fColors.get(f);
			g.setColor(color);
			g.drawOval(x - 1, y - 6, 2, 2);
			g.setColor(Color.black);
			g.drawString(fNames.get(f), x+10, y);

			y +=15;
			
		}

	}
	
	public void addFunction(FunctionElement f,Color c, String name) {
		if (!fColors.keySet().contains(f)) {
			fColors.put(f,c);
			fNames.put(f, name);
		}
	}
	

}
