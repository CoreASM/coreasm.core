/*	
 * SystemControlState.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-04-30 01:05:27 +0200 (Fr, 30 Apr 2010) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.step;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;

/**
 * @author Roozbeh Farahbod
 *
 */
public class SystemControlState extends Element implements Enumerable {

	protected Set<ControlStateElement> value = new HashSet<ControlStateElement>();
	
	@Override
	public boolean contains(Element e) {
		return value.contains(e);
	}

	@Override
	public Collection<? extends Element> enumerate() {
		return Collections.unmodifiableSet(value);
	}

	@Override
	public List<Element> getIndexedView() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("System control state cannot be viewed as a list.");
	}

	@Override
	public int size() {
		return value.size();
	}

	@Override
	public boolean supportsIndexedView() {
		return false;
	}

	public SystemControlState snapshot() {
		SystemControlState result = new SystemControlState();
		result.value = Collections.unmodifiableSet(this.value);
		return result;
	}

	public String toString() {
		return "SCS:" + value.toString();
	}
}
