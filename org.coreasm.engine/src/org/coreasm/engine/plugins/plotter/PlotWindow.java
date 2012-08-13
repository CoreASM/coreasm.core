/*	
 * PlotWindow.java 	1.0 	$Revision: 243 $
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.coreasm.engine.absstorage.FunctionElement;

/** 
 * Opens a JFrame window that plots functions of the form <i>f: X -> Y</i>.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class PlotWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private PlotPanel plotPanel;
	private InfoPanel infoPanel;
	private JPanel buttonsPanel;
	private boolean killed = false;
	
	public PlotWindow() {
		super("Plot Window");
		initComponents();
	}
	
	private void initComponents() {
		
		plotPanel = new PlotPanel();
		infoPanel = new InfoPanel();
		
		JButton closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.setMnemonic(KeyEvent.VK_C);
		closeButton.addActionListener(this);

		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BorderLayout());
		buttonsPanel.add(closeButton, BorderLayout.EAST);
		
		setLayout(new BorderLayout());
		add(plotPanel, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);
		add(infoPanel, BorderLayout.EAST);
		
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		
		addWindowStateListener(null);
		
		pack();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("close")) {
			setVisible(false);
			//this.dispose();
		} else
			System.out.println(e.getActionCommand());
	}

	/**
	 * Adds a new function to be plotted in this window
	 *  
	 * @param f function element
	 * @param name name of the function
	 */
	public void addFunction(FunctionElement f, String name) {
		if (plotPanel != null) {
			plotPanel.addFunction(f);
			infoPanel.addFunction(f, plotPanel.getColorMap().get(f), name);
		}
	}
	
	/**
	 * This is overriden so as to dispose the window 
	 * if it already has alread received a <i>kill</i> signal.
	 * If not, it normally hides the window.
	 * 
	 * @see JFrame#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (killed && !isVisible())
			this.dispose();
	}

	/**
	 * Sets the <i>killed</i> flag. If the window
	 * is not visible, it will be disposed. Otherwise,
	 * it will be disposed when its visibility is set
	 * to false.
	 *
	 * @see #setVisible(boolean)
	 */
	public void setKilled(boolean b) {
		killed = b;
		if (killed && !isVisible())
			this.dispose();
	}

}
