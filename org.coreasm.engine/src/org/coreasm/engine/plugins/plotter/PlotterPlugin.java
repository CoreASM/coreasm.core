/*	
 * PlotterPlugin.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.plotter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.MapFunction;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.plugins.string.StringElement;
import org.coreasm.engine.plugins.string.StringPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * This is a sample CoreASM Plug-in to draw a number of function 
 * elements in a window.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class PlotterPlugin extends Plugin implements 
			ParserPlugin, InterpreterPlugin, ExtensionPointPlugin, VocabularyExtender {

	protected static final Logger logger = LoggerFactory.getLogger(PlotterPlugin.class);

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 3, 1, "beta");

	/** the name of this plug-in */
	public static final String PLUGIN_NAME = PlotterPlugin.class.getSimpleName();
	   
	/** keyword for the plot rule */
	public static final String PLOT_KEYWORD = "plot";
	
	/** name of the function that holds the list of functions to be drawn in the state */
	public static final String PLOT_LOCATION_NAME = "nextPlotFunctions";

	private Map<String,FunctionElement> functions = null;
	private Map<String,BackgroundElement> backgrounds = null;
	private PlotWindowBackground pwBackground = null;
	private MapFunction pwFunction = null;
	protected Map<String, GrammarRule> parsers = null;
	protected Set<String> dependencyNames = null;
	
	private final String[] keywords = {"plot", "in"};
	private final String[] operators = {};

	private HashMap<EngineMode, Integer> targetModes;
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
		pwBackground = new PlotWindowBackground();
		pwFunction = new MapFunction();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	@Override
	public Set<String> getDependencyNames() {
		if (dependencyNames == null) {
			dependencyNames = new HashSet<String>();
			dependencyNames.add(StringPlugin.PLUGIN_NAME);
		}
		return dependencyNames;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	/**
	 * @return <code>null</code>
	 */
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}

	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices)capi.getPlugin("Kernel").getPluginInterface();
			
			Parser<Node> termParser = kernel.getTermParser();
			
			ParserTools pTools = ParserTools.getInstance(capi);
			
			Parser<Node> plotParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("plot", PLUGIN_NAME),
					termParser,
					pTools.seq(
							pTools.getKeywParser("in", PLUGIN_NAME),
							termParser).optional()
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object[] vals) {
							Node node = new PlotRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
				
					});
			parsers.put("Rule", 
					new GrammarRule("PlotRule",
							"'plot' Term ('in' Term)?", plotParser, PLUGIN_NAME));
		}
		
		return parsers;
	}

	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		/* only if pos is a Plot rule ... */
		if (pos instanceof PlotRuleNode) {
			/* get the function part and the window part */
			ASTNode functionNode = ((PlotRuleNode)pos).getFunctionNode();
			ASTNode idNode = ((PlotRuleNode)pos).getWindowId();
			Element window = null;
			Location l = null;
			
			if (!functionNode.isEvaluated()) 
				return functionNode;
			else {
				if (idNode != null && !idNode.isEvaluated())
					return idNode;
				
				/* if the function part is actually a function element */
				if (functionNode.getValue() instanceof FunctionElement) {
					/* based on whether a window element was provided or not
					 * either create a new window element (through its background)
					 * or use the one that is provided.
					 */
					if (idNode == null) {
						window = pwBackground.getNewValue();
					}
					else
						if (idNode.getValue() instanceof PlotWindowElement)
							window = idNode.getValue();
						else {
							capi.error("Cannot plot a function using a non PlotterWindow element.", idNode, interpreter);
							return pos;
						}
					/* create an update of the form: 'nextPlotFunctions(window, function) := true' */
					l = new Location(PLOT_LOCATION_NAME, ElementList.create(window, functionNode.getValue(), getStringRep(functionNode)));
					Update update = new Update(l, BooleanElement.TRUE, Update.UPDATE_ACTION, interpreter.getSelf(), pos.getScannerInfo());
					pos.setNode(null, new UpdateMultiset(update), null);
				} else
					capi.error("Cannot plot a non-function value.", functionNode, interpreter);
			}
		}
		return pos;
	}

	private StringElement getStringRep(ASTNode fNode) {
		String result = fNode.unparseTree();
		if (result.startsWith("@"))
			result = result.substring(1);
		return new StringElement(result);
	}
	/**
	 * Is called by the engine whenever the engine mode is changed
	 * from <code>source</code> to <code>target</code>.
	 * This plug-in steps in before two modes: 
	 * <ol>
	 * <li><i>emStepSucceeded</i>: to read the list of functions
	 * to be plotted and plot them after every step</li
	 * <li><i>emTerminating</i>: to send a kill signal to all 
	 * plot windows before the engine terminates</li>
	 * </ol>
	 * 
	 * @param source the source mode
	 * @param target the target mode
	 */
	public void fireOnModeTransition(EngineMode source, EngineMode target) {
		/* Step Succeeded */
		if (target.equals(EngineMode.emStepSucceeded)) {

			Set<PlotWindowElement> wSet = new HashSet<PlotWindowElement>();
			
			/* get all the locations of 'nextPlotFunctions' */
			for (Location l: pwFunction.getLocations(PLOT_LOCATION_NAME)) {
				Element window = l.args.get(0);
				Element f = l.args.get(1);
				String fname = l.args.get(2).toString();
				/* if the values are correct initiate plotting by adding
				 * functions to their appropriate windows
				 */
				if (f instanceof FunctionElement && window instanceof PlotWindowElement) {
					PlotWindowElement pw = (PlotWindowElement)window; 
					pw.addFunction((FunctionElement)f, fname);
					wSet.add(pw);
				} else
					logger.warn("Skipping a plot command.");
					// otherwise do nothing
			}
			
			// Clearing the values for the next step
			pwFunction.clear();
			
			// showing or repainting windows
			for (PlotWindowElement pw: wSet) 
				pw.setVisible(true);
			
		} else
			/* Terminating */
			if (target.equals(EngineMode.emTerminating)) {
				// send a kill signal to all windows
				pwBackground.killAll();
			}
	}

	public Map<EngineMode, Integer> getSourceModes() {
		return Collections.emptyMap();
	}

	public Map<EngineMode, Integer> getTargetModes() {
		if (targetModes == null) {
			targetModes = new HashMap<EngineMode, Integer>();
			targetModes.put(EngineMode.emStepSucceeded, ExtensionPointPlugin.DEFAULT_PRIORITY);
			targetModes.put(EngineMode.emTerminating, ExtensionPointPlugin.DEFAULT_PRIORITY);
		}
		return targetModes;
	}

	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	}

	public Map<String, BackgroundElement> getBackgrounds() {
		if (backgrounds == null) {
			backgrounds = new HashMap<String,BackgroundElement>();
			backgrounds.put(PlotWindowBackground.NAME, pwBackground);
		}
		return backgrounds;
	}

	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String,FunctionElement>();
			
			functions.put(PLOT_LOCATION_NAME, pwFunction);
		}
		return functions;
	}

	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	public Map<String, UniverseElement> getUniverses() {
		return Collections.emptyMap();
	}

	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		return null;
	}

}
