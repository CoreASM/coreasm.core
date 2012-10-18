/*	
 * StringElement.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Mashaal Memon
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.util.Tools;

/** 
 *	This represents a string element;
 *   
 *  @author  Mashaal Memon
 *  
 */
public class StringElement extends Element implements Enumerable {
	
	protected final String string;
	protected List<Element> indexedView = null;
	protected String denotationalValue = null;
	
	/*
	 * Instantiate this string element with appropriate string
	 * 
	 * @param string is an <code>String</code>.
	 */
	public StringElement(String string)
	{
		this.string = string;
	}
	
	public String getBackground() {
		return StringBackgroundElement.STRING_BACKGROUND_NAME;
	}
	
	/**
	 * Returns the value of this string element
	 * enclosed in double-quotes. All special characters
	 * like tab and new-line are converted to escape sequences. 
	 */
	public String denotation() {
		if (denotationalValue == null) {
			denotationalValue = "\"" + Tools.convertToEscapeSqeuence(string) + "\"";
		}
		return denotationalValue;
	}

	/**
	 * Returns a <code>String</code> representation of 
	 * this String Element.
	 * 
	 * @see org.coreasm.engine.absstorage.Element#toString()
	 */
	@Override
	public String toString() {
		return string;
	}
	
	/**
	 * Returns the string value of this element 
	 * with no quotation;
	 */
	public String getValue() {
		return string;
	}
	
	//----------------------
	// Equality interface
	//----------------------
	
	/**
 	 * Compares this Element to the specified Element. 
 	 * The result is <code>true</code> if the argument 
 	 * is not null and is considered to be equal to this Element.
 	 * 
 	 * @param anElement the Element to compare with.
 	 * @return <code>true</code> if the Elements are equal; <code>false</code> otherwise.
 	 * @throws IllegalArgumentException if <code>anElement</code> is not an instance
 	 * of <code>Element</code>
 	 */
 	public boolean equals(Object anElement) {
 		
 		boolean equals = false;

 		// if both java objects are idential, no further checks are required
 		if (super.equals(anElement))
 			equals = true;
 		// else both java objects are not identical, have to check that
 		// both are string elements, and both have the same numerical value
 		else
 		{
	 		// both string elements
	 		if (anElement instanceof StringElement)
	 		{
	 			StringElement otherString = (StringElement)anElement;
	 			
	 			// if the current string and the other string equal each other
	 			// then objects are equal
	 			if (string.equals(otherString.string))
	 				return true;
	 		}
 		}
 		
 		return equals;
	}
 	
 	/**
	 * Hashcode for String elements. Must be overridden because equality is overridden. 
	 *  
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return string.hashCode(); 
	}


	//
	// Static methods
	// 
	
	/**
	 * Converts escape sequences to their corresponding characters.
	 *  
	 * @throws IllegalArgumentException if there is an invalid escape character
	 */
	public static String processEscapeCharacters(String str) throws IllegalArgumentException {
		return Tools.convertFromEscapeSequence(str);
	}

	/*
	 * Returns true if the given element represents a single character.
	 */
	private boolean isChar(Element e) {
		return e.toString().length() == 1;
	}
	
	public boolean contains(Element e) {
		if (isChar(e))
			return string.contains(e.toString());
		else
			return false;
	}

	public Collection<? extends Element> enumerate() {
		return getIndexedView();
	}

	public List<Element> getIndexedView() throws UnsupportedOperationException {
		if (indexedView == null) {
			indexedView = new ArrayList<Element>();
			for (Character c: string.toCharArray())
				indexedView.add(new StringElement(c.toString()));
		}
		return indexedView;
	}

	public int size() {
		return string.length();
	}

	public boolean supportsIndexedView() {
		return true;
	}
	
}
