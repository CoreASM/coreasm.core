/*	
 * JasmineUpdateContainer.java  	$Revision: 130 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-03-31 01:27:47 +0200 (Mi, 31 Mrz 2010) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.jasmine.plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * A container for JASMine updates. It is useful when we need to
 * define an order over a set of updates.
 *   
 * @author Roozbeh Farahbod
 * @version $Revision: 130 $, Last modified: $Date: 2010-03-31 01:27:47 +0200 (Mi, 31 Mrz 2010) $
 */
public class JasmineUpdateContainer extends JasmineAbstractUpdateElement {

	public Collection<? extends JasmineAbstractUpdateElement> updateElements;
	private final Set<Element> agents;
	private final Set<ScannerInfo> sinfos;

	/**
	 * Creates a new JASMine update container with the given 
	 * collection of elements. The given collection can NOT be null.
	 */
	public JasmineUpdateContainer(Collection<? extends JasmineAbstractUpdateElement> updateElements) {
		if (updateElements == null)
			throw new NullPointerException("Jasmine update elements cannot be null.");
		
		this.updateElements = updateElements;
		Set<Element> set = new HashSet<Element>();
		Set<ScannerInfo> iset = new HashSet<ScannerInfo>();
		for (JasmineAbstractUpdateElement jaue: updateElements) { 
			set.addAll(jaue.getAgents());
			iset.addAll(jaue.getScannerInfos());
		}
		this.agents = Collections.unmodifiableSet(set);
		this.sinfos = Collections.unmodifiableSet(iset);
	}
	
	public boolean equals(Object other) {
		if (other instanceof JasmineUpdateContainer) {
			return this.updateElements.equals(((JasmineUpdateContainer)other).updateElements);
		} else
			return false;
	}
	
	public int hashCode() {
		return updateElements.hashCode() + 1;
	}
	
	/*
	 * Returns a multiset of all the JASMine update elements in this 
	 * container that have the given update type (see {@link Type}) and
	 * are applied on the given location.
	 *
	public Multiset<JasmineUpdateElement> find(Type type, Location loc) {
		
	}
	*/
	
	public String toString() {
		return updateElements.toString();
	}

	@Override
	public Set<Element> getAgents() {
		return this.agents;
	}

	@Override
	public Set<ScannerInfo> getScannerInfos() {
		return this.sinfos;
	}
}
