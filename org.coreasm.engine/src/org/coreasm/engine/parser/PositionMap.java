/*	
 * PositionMap.java  	$Revision: 243 $
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
 
package org.coreasm.engine.parser;

import org.codehaus.jparsec.DefaultSourceLocator;
import org.codehaus.jparsec.error.Location;

/** 
 * A wrapper around {@link DefaultPositionMap}. 
 *   
 * @see DefaultPositionMap
 * 
 * @author  Roozbeh Farahbod
 * 
 */
public class PositionMap extends DefaultSourceLocator { //DefaultPositionMap {

	/**
	 * @see DefaultPositionMap#DefaultPositionMap(CharSequence, int, int)
	 */
	public PositionMap(CharSequence src, int lno, int cno) {
		super(src, lno, cno);
	}

	/**
	 * @see DefaultPositionMap#DefaultPositionMap(CharSequence, int, int, char)
	 */
	public PositionMap(CharSequence src, int lno, int cno, char line_break) {
		super(src, lno, cno, line_break);
	}

	/**
	 * Return the given position in form of line and column number
	 * in a {@link CharacterPosition} object.
	 * 
	 * @param index index in the character sequence
	 */
	public CharacterPosition getPosition(int index) {
		Location pos = null;
		try {
			pos = locate(index);
			return new CharacterPosition(pos.line, pos.column);
		} catch (Exception e) {
			return CharacterPosition.NO_POSITION;
		}
	}
}
