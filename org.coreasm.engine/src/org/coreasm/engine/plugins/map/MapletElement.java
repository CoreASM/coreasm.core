/*	
 * MapletElement.java 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.map;

import org.coreasm.engine.absstorage.Element;

/** 
 * A placeholder for maplets
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class MapletElement extends Element {

	protected final Element key;
	protected final Element value;
	
	protected MapletElement(Element key, Element value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public boolean equals(Object anElement) {
		if (super.equals(anElement))
			return true;
		else
			if (anElement instanceof MapletElement) {
				MapletElement other = (MapletElement)anElement;
				return this.key.equals(other.key) && this.value.equals(other.value);
			} else
				return false;
	}

	@Override
	public String denotation() {
		return key.denotation() + "->" + value.denotation();
	}

	@Override
	public String toString() {
		return key + "->" + value;
	}

	@Override
	public int hashCode() {
		return key.hashCode() + value.hashCode();
	}
	
}
