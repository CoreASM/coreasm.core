/*	
 * UndirectedGraphElement.java 
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
 * Undirected graph elements in CoreASM state.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class UndirectedGraphElement extends GraphElement {

	protected final DirectedGraph<Element, Element> dgraph;
	protected final UndirectedGraph<Element, Element> ugraph;
	
	/**
	 * Creates a new graph element based on the given directed graph.
	 */
	protected UndirectedGraphElement(DirectedGraph<Element, Element> graph) {
		this.dgraph = graph;
		this.ugraph = new AsUndirectedGraph<Element, Element>(dgraph);
	}
	
	/**
	 * Creates a new undirected graph.
	 * 
	 * @see DefaultDirectedGraph
	 * @see AsUndirectedGraph
	 */
	public UndirectedGraphElement() {
		this(new DefaultDirectedGraph<Element, Element>(new EdgeElement.DefaultEdgeFactory()));
	}

	/**
	 * Returns an Undirected view of this graph.
	 */
	@Override
	public Graph<Element, Element> getGraph() {
		return ugraph;
	}
	
	/**
	 * Returns the backing directed graph. 
	 */
	public DirectedGraph<Element, Element> getDirectedGraph() {
		return dgraph;
	}

	@Override
	public UndirectedGraph<Element, Element> getUndirectedGraph() {
		return ugraph;
	}
	
}
