/*	
 * CycleDetectorCache.java 
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
import org.jgrapht.alg.CycleDetector;

/**
 * A cache of cycle detectors for graphs.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class CycleDetectorCache {

	HashMap<Graph<Element,Element>, CycleDetector<Element,Element>> detectorCache = 
		new HashMap<Graph<Element,Element>, CycleDetector<Element,Element>>();

	/**
	 * Returns a cycle detector for the given graph g, assuming that g does not change.
	 * Currently works only on directed graphs.
	 * 
	 * @param g an instance of {@link Graph}
	 */
	public CycleDetector<Element, Element> getCycleDetector(Graph<Element, Element> g) {
		CycleDetector<Element, Element> detector = detectorCache.get(g);
		if (detector == null) {
			if (g instanceof DirectedGraph)
				detector = new CycleDetector<Element, Element>((DirectedGraph<Element, Element>)g);
			detectorCache.put(g, detector);
		}
		return detector;
	}

}
