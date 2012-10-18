/*	
 * EdgeElement.java 
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.plugins.collection.AbstractListElement;
import org.jgrapht.EdgeFactory;

/**
 * Basic Edge element.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class EdgeElement extends TupleElement {

	protected final Element source;
	protected final Element target;

	/**
	 * Creates an edge between two nodes.
	 */
	public EdgeElement(Element source, Element target) {
		this.source = source;
		this.target = target;
	}
	
	/**
	 * Creates a new edge element out of a collection of 
	 * two elements.
	 * @param vertices a collection of two elements
	 * @throws IllegalArgumentException if the size of the collection is not 2 
	 */
	public EdgeElement(Collection<? extends Element> vertices) {
		if (vertices.size() == 2) {
			Element[] vs = vertices.toArray(new Element[] {});
			this.source = vs[0];
			this.target = vs[1];
		} else
			throw new IllegalArgumentException("Require two vertices to create an edge.");
	}
	
	@Override
	public String toString() {
		return "(" + source + ":" + target + ")";
	}

	/**
	 * @return the source node of this edge.
	 */
	public Element getSource() {
		return source;
	}

	@Override
	public String getBackground() {
		return EdgeBackgroundElement.BACKGROUND_NAME;
	}

	/**
	 * @return the target node of this edge
	 */
	public Element getTarget() {
		return target;
	}

	/**
	 * The default edge factory for edge elements.
	 * 
	 * @author Roozbeh Farahbod
	 */
	public static class DefaultEdgeFactory implements EdgeFactory<Element, Element> {

		@Override
		public Element createEdge(Element sourceVertex, Element targetVertex) {
			return new EdgeElement(sourceVertex, targetVertex);
		}
		
	}

	@Override
	public List<Element> getFixedTuple() {
		List<Element> tuple = new ArrayList<Element>();
		tuple.add(source);
		tuple.add(target);
		return tuple;
	}

	@Override
	public AbstractListElement getNewInstance(
			Collection<? extends Element> collection) {
		return new EdgeElement(Element.UNDEF, Element.UNDEF);
	}

}
