/*	
 * IOPlugin.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.io;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.PluginAggregationAPI;
import org.coreasm.engine.absstorage.PluginAggregationAPI.Flag;
import org.coreasm.engine.absstorage.PluginCompositionAPI;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.Aggregator;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.PluginServiceInterface;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.plugins.string.StringElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * A plugin that provides Input/Output services to a CoreASM specification.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class IOPlugin extends Plugin implements 
		ParserPlugin, InterpreterPlugin, VocabularyExtender, ExtensionPointPlugin, Aggregator {

	private static final Logger logger = LoggerFactory.getLogger(IOPlugin.class);
	
	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 3, 1, "");

	public static final String PLUGIN_NAME = IOPlugin.class.getSimpleName();

	/** The print rule keyword */
	public static final String PRINT_KEYWORD = "print";
	
	/** Name of the output function */
	public static final String OUTPUT_FUNC_NAME = "output";
	
	/** Location of output function */
	public static final Location OUTPUT_FUNC_LOC = new Location(IOPlugin.OUTPUT_FUNC_NAME, ElementList.NO_ARGUMENT);

	/** Name of the output function */
	public static final String INPUT_FUNC_NAME = "input";
	
	/** Location of output function */
	public static final Location INPUT_FUNC_LOC = new Location(IOPlugin.INPUT_FUNC_NAME, ElementList.NO_ARGUMENT);

	/** Print Action */
	public static final String PRINT_ACTION = "printAction";
	public static final String[] UPDATE_ACTIONS = {PRINT_ACTION};
	
	private final Set<String> dependencyList;
	
	/** 
	 * List of all the messages generated in the current run. 
	 * This list will be empty if an output stream is set (i.e., {@link #outputStream} is not null). 
	 */
	public List<String> outputMessages;
	
	private Map<EngineMode, Integer> sourceModes;
	private Map<EngineMode, Integer> targetModes;
	private HashSet<String> functionNames;
	private Map<String,FunctionElement> functions = null;
	
	protected Map<String, GrammarRule> parsers = null;
	protected IOPluginPSI pluginPSI;
	protected FunctionElement outputFunction;
	protected FunctionElement inputFunction;
	protected InputProvider inputProvider;
	protected PrintStream outputStream;

	private final String[] keywords = {"print"};
	private final String[] operators = {};
	
	/**
	 * 
	 */
	public IOPlugin() {
		super();
		dependencyList = new HashSet<String>();
		dependencyList.add("StringPlugin");
		pluginPSI = new IOPluginPSI();
		
		functionNames = new HashSet<String>();
		functionNames.add(INPUT_FUNC_NAME);
		functionNames.add(OUTPUT_FUNC_NAME);
	}


	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
		outputFunction = new OutputFunctionElement();
		inputFunction = new InputFunctionElement(this);
		outputMessages = new ArrayList<String>();
		outputStream = System.out;
		pluginPSI = new IOPluginPSI();
		sourceModes = new HashMap<EngineMode, Integer>();
		sourceModes.put(EngineMode.emAggregation, ExtensionPointPlugin.DEFAULT_PRIORITY);
		targetModes = new HashMap<EngineMode, Integer>();
		targetModes.put(EngineMode.emStepSucceeded, ExtensionPointPlugin.DEFAULT_PRIORITY);
		targetModes.put(EngineMode.emInitializingState, ExtensionPointPlugin.DEFAULT_PRIORITY);
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
			
			ParserTools npTools = ParserTools.getInstance(capi);

			Parser<Node> printParser = Parsers.array(
					new Parser[] {
					npTools.getKeywParser("print", PLUGIN_NAME),
					termParser
					}).map(new org.codehaus.jparsec.functors.Map<Object[],Node>() { 
						public Node map(Object... vals) {
							Node node = new PrintRuleNode(((Node)vals[0]).getScannerInfo());
							node.addChild((Node)vals[0]);
							node.addChild("alpha", (Node)vals[1]);
							return node;
						}
					});
			parsers.put("Rule", 
					new GrammarRule("PrintRule",
							"'print' Term", printParser, PLUGIN_NAME));
		}
		
		return parsers;
	}
	
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		// Print Rule
		if (pos instanceof PrintRuleNode) {
			return interpretPrint(interpreter, (PrintRuleNode)pos); 
		}
		return pos;
	}

	/*
	 * Interprets the Pritn rule.
	 */
	private ASTNode interpretPrint(Interpreter interpreter, PrintRuleNode pos) throws InterpreterException {
		if (!pos.getMessage().isEvaluated()) {
			return pos.getMessage();
		} else {
			pos.setNode(
					null, 
					new UpdateMultiset(
							new Update(
									OUTPUT_FUNC_LOC,
									new StringElement(pos.getMessage().getValue().toString()),
									PRINT_ACTION,
									interpreter.getSelf(),
									pos.getScannerInfo()
									)), 
					null);
			
			/*
			 * Old Code
			 * 
			synchronized (pluginPSI) {
				outputBuffer.add(pos.getMessage().getValue());
			}
			pos.setNode(
					null, 
					new UpdateMultiset(),
					//		new Update(
					//				OUTPUT_FUNC_LOC,
					//				pos.getMessage().getValue(),
					//				SetPlugin.SETADD_ACTION
					//				)), 
					null);
			*/
		}
		return pos;
	}
	
	/**
	 * Returns a set containing the following functions:
	 * <ul>
	 * <li><i>output</i></li>
	 * </ul>
	 */
	public Map<String,FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String,FunctionElement>();
			functions.put(INPUT_FUNC_NAME, inputFunction);
			functions.put(OUTPUT_FUNC_NAME, outputFunction);
		}
		return functions;
	}

	public Map<String,UniverseElement> getUniverses() {
		return Collections.emptyMap();
	}

	public Map<String,BackgroundElement> getBackgrounds() {
		return Collections.emptyMap();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#getPluginInterface()
	 */
	@Override
	public PluginServiceInterface getPluginInterface() {
		return pluginPSI;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#getDependencyNames()
	 */
	@Override
	public Set<String> getDependencyNames() {
		return this.dependencyList;
	}
	
	/**
	 * Interface of the IOPlugin to engine environment
	 * 
	 * @author Roozbeh Farahbod
	 * 
	 */
	public class IOPluginPSI implements PluginServiceInterface {
		
		/**
		 * @return output messages as an array of <code>Element</code>
		 * @see Element
		 */
		public String[] getOutputHistory() {
			synchronized (pluginPSI) {
				String[] elements = new String[outputMessages.size()];
				return outputMessages.toArray(elements);
			}
		}
		
		/**
		 * Sets the input provider of this plugin.
		 * @param ip an input provider
		 */
		public void setInputProvider(InputProvider ip) {
			synchronized (pluginPSI) {
				inputProvider = ip;
			}
		}
		
		/**
		 * Sets the output stream for 'print' rules.
		 * @param output a <code>PrintStream</code> object
		 */	
		public void setOutputStream(PrintStream output) {
			synchronized (pluginPSI) {
				outputStream = output;
			}
		}
	}

	public void fireOnModeTransition(EngineMode source, EngineMode target) {
		if (source.equals(EngineMode.emAggregation) 
				&& target.equals(EngineMode.emStepSucceeded)) {
			/* old code
			outputMessages.addAll(outputBuffer);
			for (Element e: outputBuffer) 
				outputStream.println(e);
			outputBuffer.clear();
			*/
			try {
				String msgs = capi.getStorage().getValue(OUTPUT_FUNC_LOC).toString();
				if (outputStream == null)
					outputMessages.add(msgs);
				outputStream.print(msgs);
				capi.getStorage().setValue(OUTPUT_FUNC_LOC, new StringElement(""));
			} catch (InvalidLocationException e) {
				// Should not happen
				logger.error("Output function is not available.");
			}
		} 
		
		else if (target.equals(EngineMode.emInitializingState)) {
			outputMessages.clear();
		}
	}

	public Map<EngineMode, Integer> getSourceModes() {
		return sourceModes;
	}

	public Map<EngineMode, Integer> getTargetModes() {
		return targetModes;
	}

	public Set<String> getBackgroundNames() {
		return Collections.emptySet();
	}

	public Set<String> getFunctionNames() {
		return functionNames;
	}

	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public void aggregateUpdates(PluginAggregationAPI pluginAgg) {
		// all locations on which contain print actions
		synchronized (this) {
			Set<Location> locsToAggregate = pluginAgg.getLocsWithAnyAction(PRINT_ACTION);
			Set<Element> contributingAgents = new HashSet<Element>();
			Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();
			
			// for all locations to aggregate
			for (Location l : locsToAggregate) {
				if (l.equals(OUTPUT_FUNC_LOC)) {
					StringBuffer outputResult = new StringBuffer();
					
					// if regular update affects this location
					if (pluginAgg.regularUpdatesAffectsLoc(l)) {
						pluginAgg.handleInconsistentAggregationOnLocation(l,this);
					} else {
						for (Update update: pluginAgg.getLocUpdates(l)) {
							if (update.action.equals(PRINT_ACTION)) {
								outputResult.append(update.value.toString() + "\n");
								// flag update aggregation as successful for this update
								pluginAgg.flagUpdate(update, Flag.SUCCESSFUL, this);
								contributingAgents.addAll(update.agents);
								contributingNodes.addAll(update.sources);
							}
						}
					}
					pluginAgg.addResultantUpdate(
							new Update(
									OUTPUT_FUNC_LOC, 
									new	StringElement(outputResult.toString()),  
									Update.UPDATE_ACTION,
									contributingAgents,
									contributingNodes
							), 
							this
					);
				}
			}
		}
		
		/*
		if (!wasThereAnyOutput)
			pluginAgg.addResultantUpdate(
					new Update(
							OUTPUT_FUNC_LOC, 
							new	StringElement(""),  
							Update.UPDATE_ACTION
					), 
					this
			);
		*/
	}

	public void compose(PluginCompositionAPI compAPI) {
		synchronized (this) {
			StringBuffer outputResult1 = new StringBuffer();
			StringBuffer outputResult2 = new StringBuffer();
			Set<Element> contributingAgents = new HashSet<Element>();
			Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();
			
			// First, add all the updates in the second set
			for (Update u: compAPI.getLocUpdates(2, OUTPUT_FUNC_LOC)) {
				if (u.action.equals(PRINT_ACTION)) {
					outputResult2.append(u.value.toString() + "\n");
					contributingAgents.addAll(u.agents);
					contributingNodes.addAll(u.sources);
				}
				else
					compAPI.addComposedUpdate(u, this);
			}
			
			// if the second set does not have a basic update, 
			// add all the updates from the first set as well
			if (!compAPI.isLocUpdatedWithActions(2, OUTPUT_FUNC_LOC, Update.UPDATE_ACTION)) {
				for (Update u: compAPI.getLocUpdates(1, OUTPUT_FUNC_LOC)) {
					if (u.action.equals(PRINT_ACTION)) {
						outputResult1.append(u.value.toString() + "\n");
						contributingAgents.addAll(u.agents);
						contributingNodes.addAll(u.sources);
					}
					else
						compAPI.addComposedUpdate(u, this);
				}
			}
			if (outputResult1.length() > 0 || outputResult2.length() > 0) {
				String strResult2 = "";
				if (outputResult2.length() > 0)
					strResult2 = outputResult2.substring(0, outputResult2.length() - 1);
				compAPI.addComposedUpdate(new Update(OUTPUT_FUNC_LOC, 
						new StringElement(outputResult1.append(strResult2).toString()), 
						PRINT_ACTION, contributingAgents, contributingNodes), this);
			}
		}
	}

	public String[] getUpdateActions() {
		return UPDATE_ACTIONS;
	}

	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		return null;
	}

}
