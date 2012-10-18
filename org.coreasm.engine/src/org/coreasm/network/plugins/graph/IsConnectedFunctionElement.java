/*	
 * IsConnectedFunctionElement.java 
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

import org.coreasm.engine.absstorage.BooleanBackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;
import org.jgrapht.Graph;
import org.jgrapht.alg.ConnectivityInspector;

/**
 *  Test if the given graph is connected
 * 
 * @author Roozbeh Farahbod
 *
 */
public class IsConnectedFunctionElement extends FunctionElement {

	Signature sig = null;
	final ConnectivityInspectorCache inspectorCache;
	
	public static final String FUNCTION_NAME = "isConnected";
	
	public IsConnectedFunctionElement(ConnectivityInspectorCache inspectorCache) {
		this.inspectorCache = inspectorCache;
	}
	
	@Override
	public FunctionClass getFClass() {
		return FunctionClass.fcDerived;
	}

	@Override
	public Signature getSignature() {
		if (sig == null)
			sig = new Signature(GraphBackgroundElement.BACKGROUND_NAME,
				BooleanBackgroundElement.BOOLEAN_BACKGROUND_NAME);
		return sig;
	}

	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF; 
		
		if (args.size() == 1) 
			if (args.get(0) instanceof GraphElement) {
				Graph<Element, Element> g = ((GraphElement)args.get(0)).getGraph();
				ConnectivityInspector<Element, Element> inspector = inspectorCache.getInspector(g);
						
				if (inspector != null) {
					result = BooleanElement.valueOf(inspector.isGraphConnected());
				}
			}
		
		return result;
	}

}
