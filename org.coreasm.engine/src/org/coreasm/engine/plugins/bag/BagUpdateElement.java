/*	
 * BagUpdateElement.java  	$Revision: 243 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.bag;

import org.coreasm.engine.absstorage.Element;

/** 
 * Basic update values for bags. A bag update value can be
 * either an addition of a value, a removal of a value, or an
 * absolute bag value.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class BagUpdateElement extends BagAbstractUpdateElement {

	public enum BagUpdateType {ADD, REMOVE};
	
	public final BagUpdateType type;
	public final Element value;
	
	/**
	 * Creates a new bag update element. 
	 * 
	 * @param type type of the update
	 * @param value value 
	 */
	public BagUpdateElement(BagUpdateType type, Element value) {
		if (type == null || value == null)
			throw new NullPointerException("Cannot create a bag update with null values.");
		this.type = type;
		this.value = value;
	}
	
	public boolean equals(Object o) {
		if (o instanceof BagUpdateElement) {
			BagUpdateElement theOther = (BagUpdateElement)o;
			return (this.type.equals(theOther.type) && this.value.equals(theOther.value));
		} else
			return false;
	}
	
	public int hashCode() {
		return value.hashCode();
	}
	
	public String toString() {
		return type.toString() + ":" + value.toString();
	}
}
