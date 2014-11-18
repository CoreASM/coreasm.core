/*  
 * EnumerationBackgroundElement.java    1.0     04-Apr-2006
 * 
 *
 * Copyright (C) 2006 George Ma
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.compiler.plugins.signature.include;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.plugins.signature.EnumerationElement;

/** 
 * This is the class of Enumeration Background elements. If an enumeration is
 * defined as:
 * <p>
 * <code><b>enum</b> MotorStates = {on, off}</code>
 * <p>
 * then an instnace of this class is created with the name "MotorStates" and
 * <i>on</i> and <i>off</i> as its members.
 *
 * @author  George Ma
 * 
 */
public class EnumerationBackgroundElement extends BackgroundElement 
    implements Enumerable {

    private List<EnumerationElement> members;
    private List<Element> enumCache = null;
    
    public EnumerationBackgroundElement() {
    }
    
    public void setMembers(List<EnumerationElement> members){
        this.members = members;
        enumCache =  Collections.unmodifiableList(new ArrayList<Element>(members));
    }

    @Override
    public Element getNewValue() {
        return members.get(0);
    }

    @Override
    protected Element getValue(Element e) {
        return (members.contains(e)?BooleanElement.TRUE:BooleanElement.FALSE);
    }

    public Collection<Element> enumerate() {
    	return getIndexedView();
    }
    
    public boolean contains(Element e) {
        return enumerate().contains(e);
    }

	public List<Element> getIndexedView() throws UnsupportedOperationException {
		return enumCache;
	}

	public boolean supportsIndexedView() {
		return true;
	}

	public int size() {
		return enumCache.size();
	}

}
