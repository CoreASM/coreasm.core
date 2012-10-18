/*
 * SpecLine.java 		$Revision: 80 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.engine;

/**
 * SpecLine represent a single CoreASM specification line. 
 *   
 * @author Roozbeh Farahbod
 *
 */

public class SpecLine {

	public final String text;
	public final String fileName;
	public final int line;
	
	public SpecLine(String text, String fileName, int line) {
		this.text = text;
		this.fileName = fileName;
		this.line = line;
	}
	
	public String toString() {
		return text + " (" + fileName + ":" + line + ")";
	}
}
