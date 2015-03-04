/*	
 * ScannerInfo.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.interpreter;

import java.io.Serializable;

import org.codehaus.jparsec.Token;
import org.coreasm.engine.Specification;
import org.coreasm.engine.parser.CharacterPosition;
import org.coreasm.engine.parser.Parser;
import org.coreasm.engine.parser.PositionMap;

/** 
 * TODO At this time it is not clear if this class would be helpful.
 * 
 * Represents the information an observer receives from the scanner.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ScannerInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/** representing 'Not Available' scanner info */
	public static final ScannerInfo NO_INFO = new ScannerInfo();
	
	/** invalid position */
	public static final int INVALID_POSITION = -1;
	
	/** character position */
	public final int charPosition;
	
	/**
	 * Creates a scanner information object with
	 * an invalid position. Can be used
	 * when there is no information available.
	 *
	 * @see #hasValidPosition()
	 */
	public ScannerInfo() {
		charPosition = INVALID_POSITION;
	}
	
	/**
	 * Creates a scanner information object with
	 * the given character position. If the given
	 * position is negative, it creates an invalid
	 * scanner info.
	 */
	public ScannerInfo(int cpos) {
		if (cpos >= 0)
			charPosition = cpos;
		else
			charPosition = INVALID_POSITION;
	}
	
	/**
	 * Creates a scanner information object
	 * from the given token.
	 * 
	 * @param data a {@link Tok} object
	 */
	public ScannerInfo(Token data) {
		charPosition = data.index();
	}
	
	/**
	 * @return <code>true</code> if the line number 
	 * and character position are both available. 
	 */
	public boolean hasValidPosition() {
		return charPosition != INVALID_POSITION;
	}
	
	/**
	 * Returns the position information as an string. If 
	 * this object has no valid position information,
	 * returns an empty string.
	 */
	public String getPos() {
		if (hasValidPosition())
			return " at #" + charPosition;
		else
			return "";
	}

	/**
	 * Returns the position information in the specification
	 * file. It uses the given position map
	 * to convert from the recorded linear position to a 
	 * line-column position. If this object has no valid position 
	 * information, returns {@link CharacterPosition#NO_POSITION}.
	 */
	public CharacterPosition getPos(PositionMap map) {
		if (hasValidPosition()) 
			return map.getPosition(charPosition);
		else
			return CharacterPosition.NO_POSITION;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ScannerInfo) {
			return this.charPosition == ((ScannerInfo)obj).charPosition;
		} else
			return false;
	}
	
	public int hashCode() {
		return charPosition;
	}

	/**
	 * Returns the a string representation of the context of this scanner info 
	 * with respect to the given parser and specification.
	 * 
	 * @param parser the Parser component of the engine
	 * @param spec the specification
	 * 
	 * @return an instance of CharacterPosition
	 */
	public String getContext(Parser parser, Specification spec) {
		if (parser == null || spec == null)
			return "";
		else {
			CharacterPosition cp = getPos(parser.getPositionMap());
			return cp.toString(spec) + ":" + spec.getLine(cp.line).text;
		}
	}
	
	public String toString() {
		return "@" + charPosition;
	}

}
