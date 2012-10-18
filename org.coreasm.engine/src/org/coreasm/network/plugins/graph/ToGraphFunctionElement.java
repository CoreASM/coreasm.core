/*	
 * ToGraphFunctionElement.java 
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
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.util.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * Creates a graph element from a set of nodes and a collection of edges.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class ToGraphFunctionElement extends FunctionElement {

	Signature sig = null;
	
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (args.size() == 2) {
			final Element arg1 = args.get(0);
			final Element arg2 = args.get(1);
			if (arg1 instanceof Enumerable && arg2 instanceof Enumerable) {
				Collection<? extends Element> vset = ((Enumerable)arg1).enumerate();
				Collection<? extends Element> eset = ((Enumerable)arg2).enumerate();

				result = createGraph(vset, eset);
			}
		}
		if (result.equals(Element.UNDEF)) {
			final String msg = "Requires a set of vertices and a collection of edges to create a graph.";
			Logger.log(Logger.WARNING, Logger.plugins, msg);
		}
		return result;
	}

	@Override
	public FunctionClass getFClass() {
		return FunctionClass.fcDerived;
	}

	@Override
	public Signature getSignature() {
		if (sig == null) {
			sig = new Signature(
					ElementBackgroundElement.ELEMENT_BACKGROUND_NAME, 
					ElementBackgroundElement.ELEMENT_BACKGROUND_NAME,
					GraphBackgroundElement.BACKGROUND_NAME);
		}
		return sig;
	}

	/**
	 * Creates a directed graph element with the given vertices and edges.
	 *  
	 * @param vset set of vertices
	 * @param eset set of edges
	 */
	public static Element createGraph(Collection<? extends Element> vset, Collection<? extends Element> eset) {
		Element result = Element.UNDEF;
		List<EdgeElement> edges = new ArrayList<EdgeElement>();
		boolean pass = true;
		for (Element edge: eset) {
			if (!(edge instanceof Enumerable)) {
				pass = false;
				break;
			} else {
				final Collection<? extends Element> vertices = ((Enumerable)edge).enumerate();
				if (vertices.size() != 2) {
					pass = false;
					break;
				}
				Element[] vs = vertices.toArray(new Element[] {});
				if (!vset.contains(vs[0]) || !vset.contains(vs[1])) {
					pass = false;
					break;
				}
				edges.add(new EdgeElement(vertices));
			}
		}
		if (pass) {
			DirectedGraph<Element, Element> g = new DefaultDirectedGraph<Element, Element>(new EdgeElement.DefaultEdgeFactory());
			for (Element v: vset)
				g.addVertex(v);
			for (EdgeElement e: edges)
				g.addEdge(e.source, e.target, e);
			result = new DirectedGraphElement(g);
		}
		return result;
	}
}
