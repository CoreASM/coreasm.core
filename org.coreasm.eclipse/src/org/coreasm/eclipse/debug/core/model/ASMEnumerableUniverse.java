package org.coreasm.eclipse.debug.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;

/**
 * Wrapper class for ASM universe elements that implement the Enumerable interface. It is needed for the history functionality.
 * @author Michael Stegmaier
 *
 */
public class ASMEnumerableUniverse extends ASMUniverse implements Enumerable {
	private final Enumerable enumerable;
	private List<Element> enumerationCache = null;

	public ASMEnumerableUniverse(String name, AbstractUniverse abstractUniverse) {
		super(name, abstractUniverse);
		enumerable = (Enumerable)abstractUniverse;
	}

	@Override
	public Collection<? extends Element> enumerate() {
		if (enumerable instanceof BackgroundElement)
			return enumerable.enumerate();
		return getIndexedView();
	}

	@Override
	public boolean contains(Element e) {
		if (enumerable instanceof BackgroundElement)
			return enumerable.contains(e);
		return elements.contains(e);
	}

	@Override
	public int size() {
		if (enumerable instanceof BackgroundElement)
			return enumerable.size();
		return elements.size();
	}

	@Override
	public boolean supportsIndexedView() {
		return enumerable.supportsIndexedView();
	}

	@Override
	public List<Element> getIndexedView() throws UnsupportedOperationException {
		if (enumerable instanceof BackgroundElement)
			return enumerable.getIndexedView();
		if (enumerationCache == null)
			enumerationCache = new ArrayList<Element>(elements);
		return enumerationCache;
	}

}
