/*	
 * ListBackgroundElement.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.list;

import java.util.List;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/** 
 * Background of list elements.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ListBackgroundElement extends BackgroundElement {

	public static final String LIST_BACKGROUND_NAME = "LIST";
	
	public ListBackgroundElement() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 */
	@Override
	public Element getNewValue() {
		return new ListElement();
	}

	public Element getNewValue(List<? extends Element> list) {
		return new ListElement(list);
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(org.coreasm.engine.absstorage.Element)
	 */
	@Override
	protected Element getValue(Element e) {
		return BooleanElement.valueOf(e instanceof ListElement);
	}

}
