/*	
 * TreeBackgroundElement.java 	1.0 	$Revision: 1 $
 * 
 * Copyright (C) 2010 Dipartimento di Informatica, Universita` di Pisa, Italia
 *
 * Last modified by $Author: Franco Alberto Cardillo $ on $Date: 2010-01-10 $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.tree;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/** 
 * Background of Tree elements.
 *   
 * @author  Franco Alberto Cardillo
 * 
 */
public class TreeBackgroundElement extends BackgroundElement {

	public static final String TREE_BACKGROUND_NAME = "TREE";
	
	
	public TreeBackgroundElement() {
		super();
	} // constructor

	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 */
	
	@Override
	public Element getNewValue() {
		return new TreeNodeElement();
	} // getNewValue(void)
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(org.coreasm.engine.absstorage.Element)
	 */
	@Override
	protected Element getValue(Element e) {
		return BooleanElement.valueOf(e instanceof TreeNodeElement);
	} // getValue(Element)

} // TreeBackgroundElement.java
