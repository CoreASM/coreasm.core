/*	
 * TupleElement.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author$ on $Date$.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.network.plugins.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.collection.AbstractListElement;
import org.coreasm.engine.plugins.number.NumberElement;

/**
 * Temporary class to model a fixed-length list until such elements are added to CoreASM.
 * 
 * @author Roozbeh Farahbod
 *
 */
public abstract class TupleElement extends AbstractListElement {

	private List<Element> tuple = null;

	public abstract List<Element> getFixedTuple();
	
	public List<Element> getTuple() {
		if (tuple == null)
			tuple = Collections.unmodifiableList(getFixedTuple());
		return tuple;
	}
	
	//@Override
	public UpdateMultiset computeAddUpdate(Location loc, NumberElement index,
			Element e, Element agent, Node node) throws InterpreterException {
		throw new InterpreterException("Cannot modify a tuple.");
	}

	//@Override
	public UpdateMultiset computeRemoveUpdate(Location loc,
			NumberElement index, Element agent, Node node)
			throws InterpreterException {
		throw new InterpreterException("Cannot modify a tuple.");
	}

	@Override
	public AbstractListElement concat(AbstractListElement e) {
		// throw new UnsupportedOperationException("Cannot concatenate tuples.");
		// TODO should create a proper tuple
		return this;
	}

	@Override
	public AbstractListElement cons(Element e) {
//		throw new UnsupportedOperationException("Cannot concatenate tuples.");
		return this;
	}

	@Override
	public Element head() {
		return get(NumberElement.getInstance(1));
	}

	@Override
	public boolean containsKey(Element key) {
		return (key instanceof NumberElement)
			&& (isValidIndex((NumberElement)key))
			&& (((NumberElement)key).getValue() <= this.size());
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean supportsIndexedView() {
		return true;
	}

	/*
	 * Returns true if the given number is natural and its 
	 * value is a java integer.
	 */
	protected boolean isValidIndex(NumberElement index) {
		return index.isNatural() && index.getValue() < Integer.MAX_VALUE;
	}

	@Override
	public Element get(Element index) {
		if (containsKey(index)) {
			Element result = null;
			int i = ((NumberElement)index).intValue();
			// note: in CoreASM list indices start from 1
			result = getTuple().get(i - 1);
			if (result == null) 
				result = defaultValue;
			return result;
		} else
			return Element.UNDEF;
	}

	@Override
	public List<? extends Element> getList() {
		return Collections.unmodifiableList(getTuple());
	}

	@Override
	public Collection<NumberElement> indexesOf(Element e) {
		List<NumberElement> result = new ArrayList<NumberElement>();
		int i = 1;
		for (Element ith: getTuple()) {
			if (ith.equals(e))
				result.add(NumberElement.getInstance(i));
			i++;
		}
		return result;
	}

	@Override
	public NumberElement indexOf(Element e) {
		// note: in CoreASM list indices start from 1
		final int i = getTuple().indexOf(e);
		if (i >=0) 
			return NumberElement.getInstance(i+1);
		else
			return null;
	}

	@Override
	public Element last() {
		if (size() > 0) 
			return getTuple().get(getTuple().size()-1);
		else
			return Element.UNDEF;
	}

	@Override
	public AbstractListElement tail() {
//		throw new UnsupportedOperationException("Tail is not supported on tuples.");
		return this;
	}

	@Override
	public boolean containsValue(Element value) {
		return getTuple().contains(value);
	}

	@Override
	public Map<Element, Element> getMap() {
		throw new UnsupportedOperationException("TupleElement does not provide a Java Map view of its elements.");
		// TODO it doesn't hurt to implement it later.
	}

	@Override
	public Set<? extends Element> keySet() {
		HashSet<Element> result = new HashSet<Element>(); 
		for (int i=0; i < getTuple().size(); i++)
			// note: in CoreASM list indices start from 1
			result.add(NumberElement.getInstance(i + 1));
		return result;
	}

	@Override
	public Collection<? extends Element> values() {
		return getTuple();
	}

	@Override
	public boolean contains(Element e) {
		return getTuple().contains(e);
	}

	@Override
	public Collection<? extends Element> enumerate() {
		return getIndexedView();
	}

	@Override
	public List<Element> getIndexedView() throws UnsupportedOperationException {
		return getTuple();
	}

	@Override
	public int size() {
		return getTuple().size();
	}
	
}
