package org.coreasm.eclipse.debug.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;

/**
 * Wrapper class for ASM universe elements. It is needed for the history functionality.
 * @author Michael Stegmaier
 *
 */
public class ASMUniverse extends AbstractUniverse {
	private final AbstractUniverse abstractUniverse;
	protected final Set<Element> elements = new HashSet<Element>();

	public ASMUniverse(String name, AbstractUniverse abstractUniverse) {
		setFClass(abstractUniverse.getFClass());
		setSignature(abstractUniverse.getSignature());
		this.abstractUniverse = abstractUniverse;
		if (abstractUniverse.isModifiable()) {
			if (abstractUniverse instanceof UniverseElement)
				elements.addAll(((UniverseElement)abstractUniverse).enumerate());
		}
	}
	
	public boolean isUniverseElement() {
		return abstractUniverse instanceof UniverseElement;
	}
	
	@Override
	public String getBackground() {
		return abstractUniverse.getBackground();
	}
	
	@Override
	protected Element getValue(Element e) {
		if (!abstractUniverse.isModifiable())
			return (abstractUniverse.member(e) ? BooleanElement.TRUE : BooleanElement.FALSE);
		return (elements.contains(e) ? BooleanElement.TRUE : BooleanElement.FALSE);
	}
	
	@Override
	public Set<? extends Element> getRange() {
		return abstractUniverse.getRange();
	}
	
	@Override
	public void setValue(List<? extends Element> args, Element value) throws UnmodifiableFunctionException {
		super.setValue(args, value);
		if (args.size() == 1 && value instanceof BooleanElement) {
			if (((BooleanElement)value).getValue())
				elements.add(args.get(0));
			else
				elements.remove(args.get(0));
		}
	}
	
	@Override
	public Set<Location> getLocations(String name) {
		if (!abstractUniverse.isModifiable())
			return abstractUniverse.getLocations(name);
		Set<Location> locations = new HashSet<Location>();
		for (Element element : elements)
			locations.add(new Location(name, ElementList.create(element)));
		return locations;
	}
	
	@Override
	public String toString() {
		return abstractUniverse.toString();
	}
}
