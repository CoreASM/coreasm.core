package org.coreasm.eclipse.debug.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;

/**
 * Wrapper class for ASM function elements. It is needed for the history functionality.
 * @author Michael Stegmaier
 *
 */
public class ASMFunctionElement extends FunctionElement {
	private final String name;
	private final FunctionElement functionElement;
	private final Set<Location> locations = new HashSet<Location>();
	private final HashMap<ElementList, Element> values = new HashMap<ElementList, Element>();
	
	public ASMFunctionElement(String name, FunctionElement functionElement) {
		setFClass(functionElement.getFClass());
		setSignature(functionElement.getSignature());
		this.name = name;
		this.functionElement = functionElement;
		this.locations.addAll(functionElement.getLocations(name));
		for (Location location : locations)
			this.values.put(location.args, functionElement.getValue(location.args));
	}
	
	@Override
	public String getBackground() {
		return functionElement.getBackground();
	}
	
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!functionElement.isReadable())
			return null;
		
		Element value = values.get(args);
		if (value == null)
			value = functionElement.getValue(args);
		return value;
	}
	
	@Override
	public Set<? extends Element> getRange() {
		return functionElement.getRange();
	}
	
	@Override
	public void setValue(List<? extends Element> args, Element value) throws UnmodifiableFunctionException {
		super.setValue(args, value);
		if (name != null && !locations.contains(args))
			locations.add(new Location(name, args));
		values.put((ElementList) args, value);
	}
	
	@Override
	public Set<Location> getLocations(String name) {
		return locations;
	}
	
	@Override
	public String toString() {
		return functionElement.toString();
	}
}
