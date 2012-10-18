/*	
 * DirectedGraphElement.java 
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

import org.coreasm.engine.absstorage.Element;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * Directed graph elements in CoreASM state.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class DirectedGraphElement extends GraphElement {

	protected final DirectedGraph<Element, Element> graph;
	
	/**
	 * Creates a new directed graph.
	 * 
	 * @see DefaultDirectedGraph
	 */
	public DirectedGraphElement() {
		graph = new DefaultDirectedGraph<Element, Element>(new EdgeElement.DefaultEdgeFactory());
	}

	/**
	 * Creates a new graph element based on the given graph.
	 */
	protected DirectedGraphElement(DirectedGraph<Element, Element> graph) {
		this.graph = graph;
	}
	
	@Override
	public Graph<Element, Element> getGraph() {
		return graph;
	}

	@Override
	public DirectedGraph<Element, Element> getDirectedGraph() {
		return graph;
	}

	@Override
	public UndirectedGraph<Element, Element> getUndirectedGraph() {
		return new AsUndirectedGraph<Element, Element>(graph);
	}
	
}
