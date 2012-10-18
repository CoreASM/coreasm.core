/*	
 * GraphPlugin.java 
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

import java.awt.Dimension;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParseMapN;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.plugins.set.SetBackgroundElement;
import org.coreasm.engine.plugins.set.SetElement;
import org.coreasm.util.Logger;
import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.ListenableDirectedGraph;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.graph.JGraphSimpleLayout;

/**
 * A plugin to provide the Graph background.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class GraphPlugin extends Plugin implements VocabularyExtender, ParserPlugin, InterpreterPlugin, ExtensionPointPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 1, 1, "alpha");
	
	public static final String PLUGIN_NAME = GraphPlugin.class.getSimpleName();

	public static final String VERTICES_FUNC_NAME = "vertices";
	public static final String EDGES_FUNC_NAME = "edges";
	public static final String SRC_VERTEX_FUNC_NAME = "sourceVertex";
	public static final String TRG_VERTEX_FUNC_NAME = "targetVertex";
	public static final String TO_GRAPH_FUNC_NAME = "toGraph";
	public static final String CREATE_GRAPH_FUNC_NAME = "createGraph";
	public static final String NEW_EDGE_TERM_NAME = "NewEdgeTerm";
	public static final String ADD_VERTEX_GR_NAME = "AddGraphVertexRule";
	public static final String ADD_EDGE_GR_NAME = "AddGraphEdgeRule";
	public static final String SHOW_GRAPH_RULE_NAME = "ShowGraphRule";
	
	public static final String SHOW_GRAPH_KW_NAME = "showgraph";
	public static final String NEW_EDGE_KW_NAME = "newedge";
	
	
	private final String[] keywords = {NEW_EDGE_KW_NAME, SHOW_GRAPH_KW_NAME, "at"};
	private final String[] operators = {};
	
	private Map<String, FunctionElement> functions = null;
	private Map<String, BackgroundElement> backgrounds = null;
    private Map<String, GrammarRule> parsers;

	private HashSet<String> dependencies;
	private HashMap<Location, VizData> graphViewers;

	private Map<EngineMode, Integer> targetModes = null;
	
	@Override
	public Set<String> getDependencyNames() {
		if (dependencies == null) {
			dependencies = new HashSet<String>();
			dependencies.add("SetPlugin");
			dependencies.add("ListPlugin");
			dependencies.add("CollectionPlugin");
		}
		return dependencies;
	}

	public GraphPlugin() {}

    @Override
	public void initialize() throws InitializationFailedException {
    	graphViewers = new HashMap<Location, VizData>();
	}

	@Override
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	@Override
	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	}

	@Override
	public Map<String, BackgroundElement> getBackgrounds() {
		if (backgrounds == null) {
			backgrounds = new HashMap<String, BackgroundElement>();
			backgrounds.put(GraphBackgroundElement.BACKGROUND_NAME, new GraphBackgroundElement());
			backgrounds.put(EdgeBackgroundElement.BACKGROUND_NAME, new EdgeBackgroundElement());
		} 
		return backgrounds;
	}

	@Override
	public Set<String> getFunctionNames() {
		return functions.keySet();
	}

	@Override
	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();

			// vertices
			functions.put(VERTICES_FUNC_NAME, new GraphAttributeFunctionElement() {
				
				@Override
				public Element getValue(GraphElement ge) {
					return new SetElement(ge.getGraph().vertexSet());
				}
				
				@Override
				public String getResultBackgroun() {
					return SetBackgroundElement.SET_BACKGROUND_NAME;
				}
			});
			
			// edges
			functions.put(EDGES_FUNC_NAME, new GraphAttributeFunctionElement() {
				
				@Override
				public Element getValue(GraphElement ge) {
					return new SetElement(ge.getGraph().edgeSet());
				}
				
				@Override
				public String getResultBackgroun() {
					return SetBackgroundElement.SET_BACKGROUND_NAME;
				}
			});

			// sourceVertex
			functions.put(SRC_VERTEX_FUNC_NAME, new EdgeAttributeFunctionElement() {
				
				@Override
				public Element getValue(EdgeElement ge) {
					return ge.source;
				}
				
				@Override
				public String getResultBackgroun() {
					return ElementBackgroundElement.ELEMENT_BACKGROUND_NAME;
				}
			});

			// targetVertex
			functions.put(TRG_VERTEX_FUNC_NAME, new EdgeAttributeFunctionElement() {
				
				@Override
				public Element getValue(EdgeElement ge) {
					return ge.target;
				}
				
				@Override
				public String getResultBackgroun() {
					return ElementBackgroundElement.ELEMENT_BACKGROUND_NAME;
				}
			});
			
			ToGraphFunctionElement tgfe = new ToGraphFunctionElement();
			// toGraph
			functions.put(TO_GRAPH_FUNC_NAME, tgfe);
			
			// createGraph
			functions.put(CREATE_GRAPH_FUNC_NAME, tgfe);
			
			// dijkstra
			functions.put(DijkstraShortestPathFunctionElement.FUNCTION_NAME, new DijkstraShortestPathFunctionElement());

			ConnectivityInspectorCache inspectorCache = new ConnectivityInspectorCache();
			
			// connected set
			functions.put(ConnectedSetFunctionElement.FUNCTION_NAME, new ConnectedSetFunctionElement(inspectorCache));

			// isConnected
			functions.put(IsConnectedFunctionElement.FUNCTION_NAME, new IsConnectedFunctionElement(inspectorCache));
			
			CycleDetectorCache detectorCache = new CycleDetectorCache();
			
			// hasCycle
			functions.put(HasCyclesFunctionElement.FUNCTION_NAME, new HasCyclesFunctionElement(detectorCache));
			
			// findCyclesWithVertex
			functions.put(FindCyclesFunctionElement.FUNCTION_NAME, new FindCyclesFunctionElement(detectorCache));
			
			// subgraph
			functions.put(SubGraphFunctionElement.FUNCTION_NAME, new SubGraphFunctionElement());
			
			// asUndirectedGraph
			functions.put(AsUndirectedFunctionElement.FUNCTION_NAME, new AsUndirectedFunctionElement());
		}
		return functions;
	}

	@Override
	public Set<String> getRuleNames() {
		return getRules().keySet();
	}

	@Override
	public Map<String, RuleElement> getRules() {
		return Collections.emptyMap();
	}

	@Override
	public Set<String> getUniverseNames() {
		return getUniverses().keySet();
	}

	@Override
	public Map<String, UniverseElement> getUniverses() {
		return Collections.emptyMap();
	}

	@Override
	public String[] getKeywords() {
		return keywords;
	}

	@Override
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	@Override
	public String[] getOperators() {
		return operators;
	}

	@Override
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}

	@Override
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			
			KernelServices kernel = (KernelServices)capi.getPlugin("Kernel").getPluginInterface();
			
			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> termParser = kernel.getTermParser();
			Parser<Node> guardParser = kernel.getGuardParser();
			
			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			
			// NewEdgeTerm : 'newedge' Term
			Parser<Node> newEdgeParser = Parsers.array( 
					new Parser[] {
					pTools.getKeywParser(NEW_EDGE_KW_NAME, PLUGIN_NAME),
					termParser}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node map(Object... nodes) {
							NewEdgeNode node = new NewEdgeNode(((Node)nodes[0]).getScannerInfo());
							addChildren(node, nodes);
							return node;
						}
				
					});
			
			// ShowGraphRule : 'showgraph' Term
			Parser<Node> showGraphParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser(SHOW_GRAPH_KW_NAME, PLUGIN_NAME),
					pTools.getKeywParser("at", PLUGIN_NAME).optional(),
					termParser}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {
					
						@Override
						public Node map(Object... nodes) {
							ShowGraphNode node = new ShowGraphNode(((Node)nodes[0]).getScannerInfo());
							addChildren(node, nodes);
							return node;
						}
				
					});

			parsers.put("BasicTerm", new GrammarRule(NEW_EDGE_TERM_NAME, "'" + NEW_EDGE_KW_NAME + "' Term", newEdgeParser, PLUGIN_NAME));
			
			parsers.put("Rule", new GrammarRule(SHOW_GRAPH_RULE_NAME, "'" + SHOW_GRAPH_KW_NAME + "' Term", showGraphParser, PLUGIN_NAME));
			
		}
		
		return parsers;
	}

	@Override
	public ASTNode interpret(Interpreter interpreter, ASTNode pos)
			throws InterpreterException {

		// newedge
		if (pos instanceof NewEdgeNode) {
			NewEdgeNode ne = (NewEdgeNode)pos;
			if (!ne.getVertices().isEvaluated())
				return ne.getVertices();
			
			Element v = ne.getVertices().getValue();
			if (v != null && v instanceof Enumerable) {
				Enumerable vs = (Enumerable)v;
				if (vs.size() == 2) {
					pos.setNode(null, null, new EdgeElement(vs.enumerate()));
					return pos;
				}
			}
			
			// if we are here, we didn't have a tuple as vertices
			String msg = "'" + NEW_EDGE_KW_NAME + "' requires a collection of two vertices to create a new edge";
			capi.error(msg, pos, interpreter);
			Logger.log(Logger.ERROR, Logger.plugins, msg);
		}

		// showgraph
		if (pos instanceof ShowGraphNode) {
			ShowGraphNode sgn = (ShowGraphNode)pos;
			if (!sgn.getGraphNode().isEvaluated())
				return sgn.getGraphNode();
			
			Element g = sgn.getGraphNode().getValue();
			if (g != null && g instanceof GraphElement) {
				if (sgn.isLocationValue()) {
					Location loc = sgn.getGraphNode().getLocation();
					if (loc == null) {
						String msg = "'" + SHOW_GRAPH_KW_NAME + " at' requires a location.";
						capi.error(msg, pos, interpreter);
						Logger.log(Logger.ERROR, Logger.plugins, msg);
						return pos;
					} else
						showGraph((GraphElement)g, true, loc);
				} else
					showGraph((GraphElement)g, false, null);
				pos.setNode(null, new UpdateMultiset(), null);
				return pos;
			}

			// if we are here, the term did not evaluate to a graph element
			String msg = "'" + SHOW_GRAPH_KW_NAME + " must be followed by a graph value."; 
			capi.error(msg, pos, interpreter);
			Logger.log(Logger.ERROR, Logger.plugins, msg);
		}
		
		return pos;
	}

	protected void showGraph(GraphElement ge, boolean persistent, Location loc) {
		JGraph jgraph = createJGraph(ge);
		
		JPanel panel = new JPanel();
	    panel.add(jgraph);
	    
		JFrame frame = new JFrame("Graph Viewer");
		frame.getContentPane().add( panel );
		frame.setSize( new Dimension(800, 600));
		frame.setVisible(true);
		
		if (persistent) 
			graphViewers.put(loc, new VizData(frame, panel, jgraph, ge));
	}

	/**
	 * Creates a JGraph component based on the given graph element.
	 * 
	 * @param ge an instance of {@link GraphElement}
	 * @return a {@link JGraph} view of <code>ge</code> 
	 */
	public JGraph createJGraph(GraphElement ge) {
		Graph<Element, Element> g = ge.getGraph();
		DirectedGraph<Element, Element> dg = new DefaultDirectedGraph<Element, Element>(EdgeElement.class);

		for (Element v: g.vertexSet())
			dg.addVertex(v);
		for (Element e: g.edgeSet())
			dg.addEdge(((EdgeElement)e).source, ((EdgeElement)e).target, e);
		
	    ListenableGraph<Element, Element> lg = new ListenableDirectedGraph<Element, Element>(dg);

        // create a visualization using JGraph, via an adapter
	    JGraphModelAdapter<Element, Element> m_jgAdapter = new JGraphModelAdapter<Element, Element>(lg);
  
	    JGraph jgraph = new JGraph( m_jgAdapter );
	    jgraph.validate();

	    JGraphFacade facade = new JGraphFacade(jgraph); // Pass the facade the JGraph instance
	    facade.setDirected(ge.isDirected());
	    
	    //JGraphLayout layout = new JGraphFastOrganicLayout(); // Create an instance of the appropriate layout
	    JGraphLayout layout = new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE);

	    layout.run(facade); // Run the layout on the facade. Note that layouts do not implement the Runnable interface, to avoid confusion
	    Map nested = facade.createNestedMap(true, true); // Obtain a map of the resulting attribute changes from the facade
	    	    
	    jgraph.getGraphLayoutCache().edit(nested); // Apply the results to the actual graph
	    
	    return jgraph;
	}

	@Override
	public void fireOnModeTransition(EngineMode source, EngineMode target)
			throws EngineException {
		if (target.equals(CoreASMEngine.EngineMode.emStepSucceeded)) {
			
			// update all the views that are monitoring a graph location
			for (Location loc: graphViewers.keySet()) {
				Element newValue = capi.getStorage().getValue(loc);
				VizData data = graphViewers.get(loc);
				// if the new value is changed and it is not null
				if (newValue != null && !newValue.equals(data.value)) {
					
					// if the new value is a graph
					if (newValue instanceof GraphElement) {
						data.value = newValue;
						data.panel.setEnabled(true);
						data.panel.removeAll();
						data.panel.add(createJGraph((GraphElement)data.value));
						data.frame.getContentPane().invalidate();
						data.frame.getContentPane().repaint();
						data.frame.setVisible(true);
					} else {
						data.panel.setEnabled(false);
						data.value = newValue;
					}
					graphViewers.put(loc, data);
				}
			}
		}
	}

	@Override
	public Map<EngineMode, Integer> getSourceModes() {
		return Collections.emptyMap();
	}

	@Override
	public Map<EngineMode, Integer> getTargetModes() {
		if (targetModes  == null) {
			targetModes = new HashMap<EngineMode, Integer>();
			targetModes.put(CoreASMEngine.EngineMode.emStepSucceeded, ExtensionPointPlugin.DEFAULT_PRIORITY);
		}
		return targetModes;
	}

	/**
	 * A structure to hold viewer data
	 */
	protected static class VizData {
		JPanel panel;
		JFrame frame;
		JGraph graph;
		Element value;
		
		public VizData(JFrame frame, JPanel panel, JGraph graph, Element value) {
			this.panel = panel;
			this.graph = graph;
			this.value = value; 
			this.frame = frame;
		}
	}
}
