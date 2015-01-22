package org.coreasm.eclipse.debug.core.model;

import java.util.Collection;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;

/**
 * Wrapper class for ASM function elements that implement the Enumerable interface. It is needed for the history functionality.
 * @author Michael Stegmaier
 *
 */
public class ASMEnumerableFunctionElement extends ASMFunctionElement implements Enumerable {
	private final Enumerable enumerable;

	public ASMEnumerableFunctionElement(String name, FunctionElement functionElement) {
		super(name, functionElement);
		enumerable = (Enumerable)functionElement;
	}

	@Override
	public Collection<? extends Element> enumerate() {
		return enumerable.enumerate();
	}

	@Override
	public boolean contains(Element e) {
		return enumerable.contains(e);
	}

	@Override
	public int size() {
		return enumerable.size();
	}

	@Override
	public boolean supportsIndexedView() {
		return enumerable.supportsIndexedView();
	}

	@Override
	public List<Element> getIndexedView() throws UnsupportedOperationException {
		return enumerable.getIndexedView();
	}

}
