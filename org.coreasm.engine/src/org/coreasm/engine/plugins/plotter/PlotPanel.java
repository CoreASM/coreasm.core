/*	
 * PlotPanel.java 	1.0 	$Revision: 243 $
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

/** 
 * A JPanel that can draw a function element. 
 *   
 * @author  Roozbeh Farahbod
 * 
 */
@SuppressWarnings("serial")
public class PlotPanel extends JPanel {

	/* keeps the mapping of functions to colors */
	private Map<FunctionElement,Color> fs;
	
	private static final int MINIMUM_WIDTH = 500;
	private static final int MINIMUM_HEIGHT = 500;
	
	/* array of colors */
	private static final Color[] COLORS = {
		Color.BLUE, Color.RED, Color.GREEN, Color.CYAN,
		Color.BLACK, Color.MAGENTA, Color.ORANGE,
		Color.PINK
	};
	private int colorIndex = 0;
	
	
	public PlotPanel() {
		super();
		initComponent();
		fs = new HashMap<FunctionElement,Color>();
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
		
		Map<Collection<DPoint>,Color> points = new HashMap<Collection<DPoint>,Color>();
		
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		
		for (FunctionElement f: fs.keySet()) {
			Color color = fs.get(f);
			
			/* we can only draw a function that provides us with 
			 * the set of all its defined locations
			 */
			if (f != null && f.getLocations("f").size() != 0) {
				ArrayList<DPoint> list = new ArrayList<DPoint>();

				for (Location l: f.getLocations("f")) {
					DPoint p = null;
					
					if (l.args.size() == 1) {
						Element arg = l.args.get(0);
						if (arg instanceof NumberElement) {
							Element v = f.getValue(l.args);
							if (v instanceof NumberElement) {
								p = new DPoint();
								p.x = ((NumberElement)arg).getNumber();
								p.y = ((NumberElement)v).getNumber();
								if (p.x < minX) minX = p.x;
								if (p.x > maxX) maxX = p.x;
								if (p.y < minY) minY = p.y;
								if (p.y > maxY) maxY = p.y;
								list.add(p);
							}
						}
					}
				}
				points.put(list, color);
			}
		}

		if (points.keySet().size() > 0) {
			double scaleX = wp / (maxX - minX);
			double scaleY = hp / (maxY - minY);
	
			g.setColor(Color.GRAY);
			int mx = delta + (int)Math.round((-minX) * scaleX);
			int my = h - delta - (int)Math.round((-minY) * scaleY);
			g.drawLine(mx, delta, mx, h - delta);
			g.drawLine(delta, my, w-delta, my);
			drawStringAt(g, Tools.dFormat(maxY, 3), mx, delta, 't');
			drawStringAt(g, Tools.dFormat(minY, 3), mx, h - delta, 'b');
			drawStringAt(g, Tools.dFormat(maxX, 3), w - delta, my, 'r');
			drawStringAt(g, Tools.dFormat(minX, 3), delta, my, 'l');
	
			for (Collection<DPoint> l: points.keySet()) {
				g.setColor(points.get(l));
				for (DPoint p: l) {
					int x = delta + (int)Math.round((p.x - minX) * scaleX);
					int y = h - delta - (int)Math.round((p.y - minY) * scaleY);
					g.drawOval(x - 1, y - 1, 2, 2);
				}
			}
		}

	}
	
	/*
	 * Draws a string at a given point on a certain direction.
	 */
	private void drawStringAt(Graphics g, String str, int x, int y, char d) {
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D textBox = fm.getStringBounds(str, g);
		g.drawOval(x - 1, y - 1, 2, 2);
		switch (d) {
		case 'c':
			g.drawString(str, x - (int)textBox.getWidth() / 2, y + (int)textBox.getHeight() / 2);
			break;
			
		case 'l':
			g.drawString(str, x, y + (int)textBox.getHeight());
			break;
			
		case 't':
			g.drawString(str, x - (int)textBox.getWidth()/2, y - 5);
			break;
			
		case 'b':
			g.drawString(str, x - (int)textBox.getWidth()/2, y + (int)textBox.getHeight() + 5);
			break;
			
		case 'r':
			g.drawString(str, x - (int)textBox.getWidth(), y + (int)textBox.getHeight());
			break;
			
		}
	}

	/**
	 * Adds a new function element to its set of functions 
	 * to be drawn.
	 */
	public void addFunction(FunctionElement f) {
		if (!fs.keySet().contains(f)) {
			fs.put(f, COLORS[colorIndex++]);
			if (colorIndex >= COLORS.length)
				colorIndex = 0;
		}
	}
	
	public Map<FunctionElement,Color> getColorMap(){
		return fs;
	}
	
	/* to keep a point */
	private class DPoint {
		public double x, y;
	}
}
