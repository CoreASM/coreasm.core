/*	
 * GraphAttributeFunctionElement.java 
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

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;

/**
 * Base class for graph attribute functions.
 * 
 * @author Roozbeh Farahbod
 *
 */
public abstract class GraphAttributeFunctionElement extends FunctionElement {

	@Override
	public Element getValue(List<? extends Element> args) {
		if (args.size() == 1 && args.get(0) instanceof GraphElement)
			return getValue((GraphElement)args.get(0));
		else
			return Element.UNDEF;
	}

	@Override
	public FunctionClass getFClass() {
		return FunctionClass.fcDerived;
	}

	@Override
	public Signature getSignature() {
		return new Signature(GraphBackgroundElement.BACKGROUND_NAME, getResultBackgroun());
	}

	/**
	 * @return the background name of the range of this function
	 */
	public abstract String getResultBackgroun();

	/**
	 * @param ge an instance of {@link GraphElement}
	 * @return value of this function
	 */
	public abstract Element getValue(GraphElement ge);
	
}
