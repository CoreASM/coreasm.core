/*	
 * CharacterPosition.java  	$Revision: 243 $
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

import org.coreasm.engine.SpecLine;
import org.coreasm.engine.Specification;

/** 
 * Represents a character position in the text.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class CharacterPosition {
	
	/** represents an unknown position information */
	public static final CharacterPosition NO_POSITION = new CharacterPosition(-1, -1);
	
	public final int line;
	public final int column;
  
	/** 
	 * Creates a character position.
	 * 
	 * @param line
	 * @param column
	 */
	public CharacterPosition(int line, int column) {
		this.line = line;
		this.column = column;
	}

	/**
	 * @return the line number indicated by this object
	 */
	public int getLineNumber() {
		return line;
	}
	
	/**
	 * @return the column number indicated by this object
	 */
	public int getColumnNumber() {
		return column;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof CharacterPosition) {
			CharacterPosition pos = (CharacterPosition)obj;
			return (pos.line == line) && (pos.column == column);
		} else
			return false;
	}

	public int hashCode() {
		return line * 80 + column;
	}

	public String toString() {
		if (line != -1) 
			return "line " + line + ", column " + column;
		else
			return "";
	}
	
	/**
	 * Provides a string representation of this position, 
	 * taking into account the given specification object.
	 */
	public String toString(Specification spec) {
		if (this == NO_POSITION) {
			return "";
		} else {
			if (spec == null)
				return toString();
			SpecLine l = spec.getLine(line);
			if (l == null)
				return toString();
			else
				return l.fileName + ":" + l.line + "," + column;
		}
	}

}
