package org.coreasm.eclipse.debug.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;

public class ASMUniverse extends AbstractUniverse {
	private final AbstractUniverse abstractUniverse;
	private final ASMFunctionElement functionElement;
	private final Set<Element> elements = new HashSet<Element>();

	public ASMUniverse(String name, AbstractUniverse abstractUniverse) {
		setFClass(abstractUniverse.getFClass());
		this.abstractUniverse = abstractUniverse;
		functionElement = new ASMFunctionElement(name, abstractUniverse);
		if (abstractUniverse instanceof UniverseElement)
			elements.addAll(((UniverseElement)abstractUniverse).enumerate());
	}
	
	public boolean isUniverseElement() {
		return abstractUniverse instanceof UniverseElement;
	}
	
	@Override
	public Signature getSignature() {
		return functionElement.getSignature();
	}
	
	@Override
	public void setSignature(Signature sig) {
		functionElement.setSignature(sig);
	}
	
	@Override
	public String getBackground() {
		return functionElement.getBackground();
	}
	
	@Override
	protected Element getValue(Element e) {
		if (abstractUniverse instanceof BackgroundElement)
			return (abstractUniverse.member(e) ? BooleanElement.TRUE : BooleanElement.FALSE);
		return (elements.contains(e) ? BooleanElement.TRUE : BooleanElement.FALSE);
	}
	
	@Override
	public Set<? extends Element> getRange() {
		return functionElement.getRange();
	}
	
	@Override
	public void setValue(List<? extends Element> args, Element value) throws UnmodifiableFunctionException {
		functionElement.setValue(args, value);
	}
	
	@Override
	public Set<Location> getLocations(String name) {
		return functionElement.getLocations(name);
	}
	
	@Override
	public String toString() {
		return functionElement.toString();
	}
}
