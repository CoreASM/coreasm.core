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

import java.util.ArrayList;
import java.util.List;

/** 
 * A wrapper around {@link DefaultPositionMap}. 
 *   
 * @see DefaultPositionMap
 * 
 * @author  Roozbeh Farahbod
 * 
 */
public class PositionMap {

	DefaultSourceLocator dsl;
	
	/**
	 * @see DefaultPositionMap#DefaultPositionMap(CharSequence, int, int)
	 */
	public PositionMap(CharSequence src, int lno, int cno) {
		dsl = new DefaultSourceLocator(src, lno, cno);
	}

	/**
	 * @see DefaultPositionMap#DefaultPositionMap(CharSequence, int, int, char)
	 */
	public PositionMap(CharSequence src, int lno, int cno, char line_break) {
		dsl = new DefaultSourceLocator(src, lno, cno, line_break);
	}

	/**
	 * Return the given position in form of line and column number
	 * in a {@link CharacterPosition} object.
	 * 
	 * @param index index in the character sequence
	 */
	public CharacterPosition getPosition(int index) {
		CharacterPosition pos = null;
		try {
			pos = dsl.locate(index);
			return new CharacterPosition(pos.line, pos.column);
		} catch (Exception e) {
			return CharacterPosition.NO_POSITION;
		}
	}


	/**
	 * This class is a copy of the class DefaultSourceLocator from JParsec 2.0
	 * (org.codehaus.jparsec.DefaultSourceLocator).
	 * 
	 * The class DefaultSourceLocator from JParsec 2.0 was called DefaultPositionMap
	 * in the old JParsec parser. In JParsec 2.0 however the visibility of this class
	 * was reduced to a package scoped visibility, and the class was declared as final.
	 * The class is not accessible from CoreASM any more, and because we want to use
	 * an unmodified JParsec library we needed to copy its code into PositionMap.
	 * 
	 * TODO: JParsed is licensed under the Apache License, Version 2.0,
	 * CoreASM uses the Academic Free License version 3.0, so we have to 
	 * describe properly that we're using this code here. Probably we have to
	 * move the class into its own file.
	 * 
	 * There are several modifications of the original DefaultSourceLocator:
	 * <ul>
	 * <li>The interface SourceLocator was removed, because its visibility was also reduced,
	 * however, we don't need the interface here.</li>
	 * <li>The type of the internal integer list was changed from JParsec's own
	 * IntList class to java.util.ArrayList.</li>
	 * <li>For storing line-column-based positions the class now uses CoreASM's
	 * CharacterPosition class instead of JParsec's Location class, since
	 * both classes do the same and have a very similar interface.</li>
	 * </ul>
	 * 
	 */
	private static class DefaultSourceLocator
	{
		private final CharSequence source;

		/** The line break character. */
		private final char lineBreakChar;

		/** The 0-based indices of the line break characters scanned so far. */
		private final List<Integer> lineBreakIndices = new ArrayList<Integer>(20);

		/** The first line number. */
		private final int startLineNumber;

		/** The first column number. */
		private final int startColumnNumber;

		/** The 0-based index of the next character to be scanned. */
		private int nextIndex = 0;

		/** The 0-based index of the column of the next character to be scanned. */
		private int nextColumnIndex = 0;

		/**
		 * Uses binary search to look up the index of the first element in {@code ascendingInts} that's
		 * greater than or equal to {@code value}. If all elements are smaller than {@code value},
		 * {@code ascendingInts.size()} is returned.
		 */
		private static int binarySearch(List<Integer> ascendingInts, int value) {
			for (int begin = 0, to = ascendingInts.size();;) {
				if (begin == to) return begin;
				int i = (begin + to) / 2;
				int x = ascendingInts.get(i);
				if (x == value) return i;
				else if (x > value) to = i;
				else begin = i + 1;
			}
		}

		/**
		 * Looks up the location identified by {@code ind} using the cached indices of line break
		 * characters. This assumes that all line-break characters before {@code ind} are already scanned.
		 */
		private CharacterPosition lookup(int index) {
			int size = lineBreakIndices.size();
			if (size == 0) return location(0, index);
			int lineNumber = binarySearch(lineBreakIndices, index);
			if (lineNumber == 0) return location(0, index);
			int previousBreak = lineBreakIndices.get(lineNumber - 1);
			return location(lineNumber, index - previousBreak - 1);
		}

		/**
		 * Scans from {@code nextIndex} to {@code ind} and saves all indices of line break characters
		 * into {@code lineBreakIndices} and adjusts the current column number as it goes. The location of
		 * the character on {@code ind} is returned.
		 * 
		 * <p> After this method returns, {@code nextIndex} and {@code nextColumnIndex} will point to the
		 * next character to be scanned or the EOF if the end of input is encountered.
		 */
		private CharacterPosition scanTo(int index) {
			boolean eof = false;
			if (index == source.length()) { // The eof has index size() + 1
				eof = true;
				index--;
			}
			int columnIndex = nextColumnIndex;
			for (int i = nextIndex; i <= index; i++) {
				char c = source.charAt(i);
				if (c == lineBreakChar) {
					lineBreakIndices.add(i);
					columnIndex = 0;
				}
				else columnIndex++;
			}
			this.nextIndex = index + 1;
			this.nextColumnIndex = columnIndex;
			int lines = lineBreakIndices.size();
			if (eof) return location(lines, columnIndex);
			if (columnIndex == 0) return getLineBreakLocation(lines - 1);
			return location(lines, columnIndex - 1);
		}

		/**
		 * Gets the 0-based column number of the line break character for line identified by
		 * {@code lineIndex}.
		 */
		private int getLineBreakColumnIndex(int lineIndex) {
			int lineBreakIndex = lineBreakIndices.get(lineIndex);
			return (lineIndex == 0) ?
					lineBreakIndex : lineBreakIndex - lineBreakIndices.get(lineIndex - 1) - 1;
		}

		private CharacterPosition getLineBreakLocation(int lineIndex) {
			return location(lineIndex, getLineBreakColumnIndex(lineIndex));
		}

		private CharacterPosition location(int l, int c) {
			return new CharacterPosition(startLineNumber + l, (l == 0 ? startColumnNumber : 1) + c);
		}

		public CharacterPosition locate(int index) {
			return (index < nextIndex) ? lookup(index) : scanTo(index);
		}

		/**
		 * Creates a {@link DefaultSourceLocator} object.
		 * 
		 * @param source the source.
		 * @param lineNumber the starting line number.
		 * @param columnNumber the starting column number.
		 * @param lineBreakChar the line break character.
		 */
		public DefaultSourceLocator(
				CharSequence source, int lineNumber, int columnNumber, char lineBreakChar) {
			this.source = source;
			this.lineBreakChar = lineBreakChar;
			this.startLineNumber = lineNumber;
			this.startColumnNumber = columnNumber;
		}

		/**
		 * Creates a {@link DefaultSourceLocator} object.
		 * 
		 * @param source the source.
		 * @param lineNumber the starting line number.
		 * @param columnNumber the starting column number.
		 */
		public DefaultSourceLocator(CharSequence source, int lineNumber, int columnNumber) {
			this(source, lineNumber, columnNumber, '\n');
		}

	}





}
