/*	
 * NameElement.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package CompilerRuntime;

/** 
 * An element that has a name. 
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class NameElement extends Element {

	/** name of this element */
	public final String name;
	
	/**
	 * Creates a new element with the given name.
	 */
	public NameElement(String name) {
		this.name = name;
	}

	/**
	 * @return name of this element
	 */
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object anElement) {
		if (anElement instanceof NameElement) {
			return this.name.equals(((NameElement)anElement).name);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
}
