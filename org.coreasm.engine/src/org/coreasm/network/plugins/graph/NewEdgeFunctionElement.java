/*	
 * NewEdgeFunctionElement.java 
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

import java.util.List;
import java.util.Set;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;

/**
 * A function element that creates new edges.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class NewEdgeFunctionElement extends FunctionElement {

	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 2)
			return new EdgeElement(args.get(0), args.get(1));
		else
			throw new CoreASMError("Two vertices are required to create an edge.");
	}

	@Override
	public FunctionClass getFClass() {
		return FunctionClass.fcDerived;
	}

	@Override
	public Set<? extends Element> getRange() {
		// TODO Auto-generated method stub
		return super.getRange();
	}

	@Override
	public Signature getSignature() {
		// TODO Auto-generated method stub
		return super.getSignature();
	}

}
