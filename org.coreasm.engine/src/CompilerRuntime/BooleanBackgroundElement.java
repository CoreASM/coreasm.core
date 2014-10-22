/*	
 * BooleanBackgroundElement.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package CompilerRuntime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** 
 *	Class of Boolean Background Element. There should only be
 *  one instance of this class in each state.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class BooleanBackgroundElement extends BackgroundElement 
    implements Enumerable {

	/**
	 * Name of the boolean background
	 */
	public static final String BOOLEAN_BACKGROUND_NAME = "BOOLEAN";

	private final List<Element> enumeration;
	
	/**
	 * Creates a new Boolean background.
	 * 
	 * @see #BOOLEAN_BACKGROUND_NAME 
	 */
	public BooleanBackgroundElement() {
		super();
        List<Element> e = new ArrayList<Element>();
        e.add(BooleanElement.TRUE);
        e.add(BooleanElement.FALSE);
        enumeration = Collections.unmodifiableList(e);
	}

	/**
	 * Returns a <code>FALSE</code> Boolean Element.
	 * 
	 * @see org.coreasm.engine.absstorage.BackgroundElement#getNewValue()
	 */
	@Override
	public Element getNewValue() {
		return BooleanElement.FALSE;
	}

	/** 
	 * Returns a <code>TRUE</code> boolean for 
	 * Boolean Elements.
	 * 
	 * @see org.coreasm.engine.absstorage.AbstractUniverse#getValue(Element)
	 * @see BooleanElement
	 */
	@Override
	protected BooleanElement getValue(Element e) {
		return (e instanceof BooleanElement)?BooleanElement.TRUE:BooleanElement.FALSE;
	}

    public Collection<Element> enumerate() {
    	return enumeration;
    }

	public boolean contains(Element e) {
		return (e.equals(BooleanElement.TRUE) || e.equals(BooleanElement.FALSE));
	}

	public List<Element> getIndexedView() throws UnsupportedOperationException {
		return enumeration;
	}

	public boolean supportsIndexedView() {
		return true;
	}

	public int size() {
		return 2;
	}

}
