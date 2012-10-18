/*	
 * ConnectivityInspectorCache.java 
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

import java.util.HashMap;

import org.coreasm.engine.absstorage.Element;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;

/**
 * A cache of connectivity inspectors for graphs.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class ConnectivityInspectorCache {

	HashMap<Graph<Element,Element>, ConnectivityInspector<Element,Element>> inspectorCache = 
		new HashMap<Graph<Element,Element>, ConnectivityInspector<Element,Element>>();

	/**
	 * Returns a connectivity inspector for the given graph g, assuming that g does not change.
	 * 
	 * @param g an instance of {@link Graph}
	 */
	public ConnectivityInspector<Element, Element> getInspector(Graph<Element, Element> g) {
		ConnectivityInspector<Element, Element> inspector = inspectorCache.get(g);
		if (inspector == null) {
			if (g instanceof UndirectedGraph)
				inspector = new ConnectivityInspector<Element, Element>((UndirectedGraph<Element, Element>)g);
			else 
				if (g instanceof DirectedGraph)
					inspector = new ConnectivityInspector<Element, Element>((DirectedGraph<Element, Element>)g);
			inspectorCache.put(g, inspector);
		}
		return inspector;
	}

}
