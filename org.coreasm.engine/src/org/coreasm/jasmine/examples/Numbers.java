/*
 * Numbers.java 		$Revision: 9 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.jasmine.examples;

/**
 * An example class for JASMine.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class Numbers {

	public double d = 0;
	public long l = 0;
	public float f = 0;
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Numbers) {
			Numbers other = (Numbers)obj;
			return other.d == d && other.l == l && other.f == f;
		} else
			return false;
	}

	public Numbers(double d, float f, int l) {
		this.d = d;
		this.l = l;
		this.f = f;
	}
	
	public void setDouble(Double d) {
		this.d = d;
	}
	
	public void setdouble(double d) {
		this.d = d;
	}

	public void setLong(Long l) {
		this.l = l;
	}
	
	public void setlong(long l) {
		this.l = l;
	}
	
	public void setFloat(Float f) {
		this.f = f;
	}
	
	public void setfloat(float f) {
		this.f = f;
	}
	
	public String toString() {
		return "[double: " + d + ", float: " + f + ", long: " + l + "]";
	}
}
