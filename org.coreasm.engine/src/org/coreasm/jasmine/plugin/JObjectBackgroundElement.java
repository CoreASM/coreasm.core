/*	
 * JObjectBackgroundElement.java  	$Revision: 9 $
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

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/** 
 * The background of JObject elements.
 *   
 * @author Roozbeh Farahbod
 * @version $Revision: 9 $, Last modified: $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $
 */
public class JObjectBackgroundElement extends BackgroundElement {

	/**
	 * Name of the JObject background
	 */
	public static final String JOBJECT_BACKGROUND_NAME = "JOBJECT";

	/**
	 * Returns a new JObject element that refers to a new
	 * Java object.
	 * 
	 * @see BackgroundElement#getNewValue()
	 */
	@Override
	public Element getNewValue() {
		return new JObjectElement(new Object());
	}

	@Override
	protected Element getValue(Element e) {
		return BooleanElement.valueOf(e instanceof JObjectElement);
	}
	

}
