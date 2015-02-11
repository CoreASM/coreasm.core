/*	
 * JObjectElement.java  	$Revision: 9 $
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

import org.coreasm.engine.absstorage.Element;

/** 
 * JObject element introduced by the JASMine plugin.
 * JObjects hold a reference to a Java object in the JVM.
 *   
 * @author Roozbeh Farahbod
 * @version $Revision: 9 $, Last modified: $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $
 */
public class JObjectElement extends Element {

	public final Object object;
	
	/**
	 * Create a new JObject that points 
	 * to the given Java object.
	 * 
	 * @param object the Java object this JObject refers to
	 */
	public JObjectElement(Object object) {
		this.object = object;
	}
	
	@Override
	public String getBackground() {
		return JObjectBackgroundElement.JOBJECT_BACKGROUND_NAME;
	}
	
	/**
	 * If the other object is an instance of JObjectElement,
	 * it compares the Java object they refer to using 
	 * the its object's equality method.
	 * 
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof JObjectElement) {
			//return this.object.equals(((JObjectElement)other).object);
			return this.object == ((JObjectElement)other).object;
		} else
			return false;
	}

	/**
	 * Returns the hash code of the object it refers to.
	 * 
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return object.hashCode();
	}

	@Override
	public String toString() {
		return object.toString();
	}

	@Override
	public String denotation() {
		return "JObject:" + object;
	}
	
	/**
	 * Returns the class of the referred Java object.
	 */
	public Class<?> jType() {
		return object.getClass();
	}
	
}
