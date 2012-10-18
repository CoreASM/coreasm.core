/*	
 * GraphBackgroundElement.java 
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

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;

/**
 * Background of {@link GraphElement}s.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class GraphBackgroundElement extends BackgroundElement {

	public static String BACKGROUND_NAME = "GRAPH";
	
	@Override
	public Element getNewValue() {
		return GraphElement.createNewInstance();
	}

	@Override
	protected Element getValue(Element e) {
		return BooleanElement.valueOf(e instanceof GraphElement);
	}
}
