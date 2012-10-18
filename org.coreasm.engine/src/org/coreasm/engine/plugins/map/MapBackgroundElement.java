/*	
 * MapBackgroundElement.java  	$Revision: 243 $
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

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/** 
 * Background of MapElements.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class MapBackgroundElement extends BackgroundElement {

	public static final String NAME = "MAP";
	
	protected static final MapElement NEW_INSTANCE = new MapElement();
	
	/* 
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 */
	@Override
	public Element getNewValue() {
		return NEW_INSTANCE;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(org.coreasm.engine.absstorage.Element)
	 */
	@Override
	protected Element getValue(Element e) {
		if (e instanceof MapElement)
			return BooleanElement.TRUE;
		else
			return BooleanElement.FALSE;
	}

}
