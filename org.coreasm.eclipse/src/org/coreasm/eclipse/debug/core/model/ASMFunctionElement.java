package org.coreasm.eclipse.debug.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;

/**
 * Wrapper class for ASM function elements. It is needed for the history functionality.
 * @author Michael Stegmaier
 *
 */
public class ASMFunctionElement {
	private String name;
	private FunctionElement functionElement;
	private Set<Location> locations = new HashSet<Location>();
	private HashMap<ElementList, Element> values = new HashMap<ElementList, Element>();
	
	public ASMFunctionElement(Location location) {
		locations.add(location);
	}
	
	public ASMFunctionElement(String name, ASMFunctionElement functionElement) {
		this.name = name;
		this.functionElement = functionElement.getFunctionElement();
		this.locations.addAll(functionElement.getLocations(name));
		for (Location location : locations)
			this.values.put(location.args, functionElement.getValue(location.args));
	}
	
	public ASMFunctionElement(String name, FunctionElement functionElement) {
		this.name = name;
		this.functionElement = functionElement;
		this.locations.addAll(functionElement.getLocations(name));
		for (Location location : locations)
			this.values.put(location.args, functionElement.getValue(location.args));
	}
	
	public FunctionElement getFunctionElement() {
		return functionElement;
	}
	
	public boolean isModifiable() {
		return functionElement == null || functionElement.isModifiable();
	}
	
	public Set<Location> getLocations(String name) {
		return locations;
	}
	
	public Element getValue(List<? extends Element> args) {
		return values.get(args);
	}
	
	public void setValue(List<? extends Element> args, Element value) {
		if (name != null && !locations.contains(args))
			locations.add(new Location(name, args));
		values.put((ElementList) args, value);
	}
}
