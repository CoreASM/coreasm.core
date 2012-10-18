/*	
 * DijkstraShortestPathFunctionElement.java 
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
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.list.ListBackgroundElement;
import org.coreasm.engine.plugins.list.ListElement;
import org.jgrapht.alg.DijkstraShortestPath;

/**
 * Computes a shortest path on a graph using the Dijkstra algorithm.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class DijkstraShortestPathFunctionElement extends FunctionElement {

	Signature sig = null;
	public static final String FUNCTION_NAME = "dijkstraShortestPath";
	
	@Override
	public FunctionClass getFClass() {
		return FunctionClass.fcDerived;
	}

	@Override
	public Signature getSignature() {
		if (sig == null)
			sig = new Signature(GraphBackgroundElement.BACKGROUND_NAME,
				ElementBackgroundElement.ELEMENT_BACKGROUND_NAME,
				ElementBackgroundElement.ELEMENT_BACKGROUND_NAME,
				ListBackgroundElement.LIST_BACKGROUND_NAME);
		return sig;
	}

	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF; 
		
		if (args.size() == 3) 
			if (args.get(0) instanceof GraphElement) {
				GraphElement ge = (GraphElement)args.get(0);
				Element start = args.get(1);
				Element end = args.get(2);
				
				List<Element> shortestPath = DijkstraShortestPath.findPathBetween(ge.getGraph(), start, end);
				if (shortestPath != null)
					result = new ListElement(shortestPath);
				else
					result = new ListElement();
			}
		
		return result;
	}

}
