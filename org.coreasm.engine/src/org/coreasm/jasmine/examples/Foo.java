/*
 * Foo.java 		$Revision: 9 $
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

import java.util.Date;

/**
 * An example class for JASMine.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class Foo {

	public String msg = "";
	public Date time = new Date();
	public Object memory = null;
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String newMsg) {
		msg = newMsg;
	}

	public Date getTime() {
		return time;
	}
	
	public void setTime(Date newTime) {
		time = newTime;
	}
	
	public String toString() {
		return msg + " at " + time;
	}
}
