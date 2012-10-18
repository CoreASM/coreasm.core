/*	
 * TestObject.java  	$Revision: 9 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.jasmine.plugin;

import java.util.Date;
import java.util.List;

/** 
 * A test object for jasmine.
 *   
 * @author Roozbeh Farahbod
 * @version $Revision: 9 $, Last modified: $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $
 */
public class TestObject {

	public Date date;
	public String str;
	public Double dbl;
	public List<? extends Object> list;
	
	public TestObject() {
		date = new Date();
	}
	
	public TestObject(String s) {
		str = s;
	}
	
	public TestObject(String s, Double d) {
		str = s;
		dbl = d;
	}
	
	public String toString() {
		return "TestObject created (" + getStr() + ")";
	}
	
	public void doubleDouble() {
		dbl = dbl * 2;
	}
	
	public String upperCase() {
		return str.toUpperCase();
	}
	
	public String concat(String c) {
		str = str + c;
		return str;
	}
	
	public void assignDoubleToString() {
		str = dbl.toString();
	}
	
	private String getStr() {
		String result = "";
		if (date != null)
			result += "created at " + date;
		
		if (str != null)
			result += ", '" + str + "'";
		
		if (dbl != null)
			result += ", " + dbl;
		
		if (result.charAt(0) == ',')
			return result.substring(1);
		else
			return result;
	}
	
	
}
