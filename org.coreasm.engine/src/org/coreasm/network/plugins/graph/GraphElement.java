/*	
 * GraphElement.java 
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

/**
 * Graph elements in CoreASM state.
 * 
 * @author Roozbeh Farahbod
 *
 */
public abstract class GraphElement extends Element {

	/**
	 * Creates a default graph element.
	 * 
	 * @see GraphElement#createDirectedGraph()
	 */
	public static GraphElement createNewInstance() {
		return createDirectedGraph();
	}
	
	/**
	 * Creates a directed graph.
	 */
	public static GraphElement createDirectedGraph() {
		return new DirectedGraphElement();
	}
	
	@Override
	public String denotation() {
		return "Graph:" + this.toString();
	}

	@Override
	public boolean equals(Object anElement) {
		if (anElement instanceof GraphElement) {
			return ((GraphElement)anElement).getGraph().equals(getGraph());
		} else
			return false;
	}

	
	@Override
	public int hashCode() {
		return getGraph().hashCode();
	}

	@Override
	public String getBackground() {
		return GraphBackgroundElement.BACKGROUND_NAME;
	}

	@Override
	public String toString() {
		return getGraph().toString();
	}
	
	/**
	 * Returns the underlying graph object.
	 */
	public abstract Graph<Element, Element> getGraph();

	/**
	 * @return a directed graph view of this graph element.
	 */
	public abstract DirectedGraph<Element, Element> getDirectedGraph();

	/**
	 * @return an undirected graph view of this graph element
	 */
	public abstract UndirectedGraph<Element, Element> getUndirectedGraph();
	
	/**
	 * @return <code>true</code> if this is a directed graph.
	 */
	public boolean isDirected() {
		return (getGraph() instanceof DirectedGraph);
	}
}
