/*	
 * IOPlugin.java 	1.
 * 
 * Copyright (C) 2006 Roozbeh Farahbod, Michael Stegmaier, Marcel Dausend
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.io.CompilerIOPlugin;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.PluginAggregationAPI;
import org.coreasm.engine.absstorage.PluginAggregationAPI.Flag;
import org.coreasm.engine.absstorage.PluginCompositionAPI;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;
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
import org.coreasm.engine.plugins.list.ListElement;
import org.coreasm.engine.plugins.string.StringElement;
import org.coreasm.util.Tools;

/** 
 * A plugin that provides Input/Output services to a CoreASM specification.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class IOPlugin extends Plugin implements 
		ParserPlugin, InterpreterPlugin, VocabularyExtender, ExtensionPointPlugin, Aggregator {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 3, 2, "");

	public static final String PLUGIN_NAME = IOPlugin.class.getSimpleName();

	/** The print rule */
	public static final String PRINT_KEYWORD = "print";
	public static final String PRINT_ACTION = "printAction";
	public static final String PRINT_OUTPUT_FUNC_NAME = "printOutput";
	public static final Location PRINT_OUTPUT_FUNC_LOC = new Location(IOPlugin.PRINT_OUTPUT_FUNC_NAME,
			ElementList.NO_ARGUMENT);

	/** The write rule */
	public static final String KEYWORD_TO = "to";
	public static final String KEYWORD_INTO = "into";
	public static final String OPERATOR_LINUX_INTO = ">>";
	public static final String OPERATOR_LINUX_TO = ">";
	public static final String WRITE_ACTION = "writeAction";
	public static final String APPEND_ACTION = "appendAction";
	public static final String FILE_OUTPUT_FUNC_NAME = "writeOutput";
	public static final String[] UPDATE_ACTIONS = { PRINT_ACTION, WRITE_ACTION, APPEND_ACTION };
	
	/** The input function */
	public static final String INPUT_FUNC_NAME = "input";
	public static final Location INPUT_FUNC_LOC = new Location(IOPlugin.INPUT_FUNC_NAME, ElementList.NO_ARGUMENT);

	/** The read function */
	public static final String READ_FUNC_NAME = "read";
	
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
	protected FunctionElement filereadFunction;
	protected FunctionElement filewriteFunction;
	protected FunctionElement outputFunction;
	protected FunctionElement inputFunction;
	protected FunctionElement fileInputFunction;
	protected InputProvider inputProvider;
	protected PrintStream outputStream;

	private final String[] keywords = { PRINT_KEYWORD, KEYWORD_TO, KEYWORD_INTO };
	private final String[] operators = { OPERATOR_LINUX_TO, OPERATOR_LINUX_INTO };
	
	private final CompilerPlugin compilerPlugin = new CompilerIOPlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
	/**
	 *  create a list of StringElements from a given file where each line corresponds to one StringElement
	 * @param path	path of the file to read from
	 * @return			list of strings representing a line of the given file
	 * @throws IOException
	 */
	protected ListElement readFromFile(String path) throws IOException {
		String path2spec = "";
		if (!new File(path).isAbsolute())
			path2spec = capi.getSpec().getFileDir();
		FileInputStream fis = new FileInputStream(Tools.concatFileName(path2spec, path));
		LinkedList<StringElement> lines = new LinkedList<StringElement>();

		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line = null;
		while ((line = br.readLine()) != null) {
			lines.add(new StringElement(line));
		}
		br.close();

		LinkedList<StringElement> linesInTextOrder = new LinkedList<StringElement>();
		while (!lines.isEmpty()) {
			linesInTextOrder.add(lines.pop());
		}

		return new ListElement(linesInTextOrder);
	}
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
		functionNames.add(PRINT_OUTPUT_FUNC_NAME);
		functionNames.add(READ_FUNC_NAME);
		functionNames.add(FILE_OUTPUT_FUNC_NAME);
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
		filereadFunction = new FileReadFunctionElement(this);
		filewriteFunction = new FileWriteFunctionElement();
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
							npTools.getKeywParser(PRINT_KEYWORD, PLUGIN_NAME),
							termParser,
							Parsers.array(
									Parsers.or(npTools.getKeywParser(KEYWORD_TO, PLUGIN_NAME),
											npTools.getOprParser(OPERATOR_LINUX_TO),
											npTools.getKeywParser(KEYWORD_INTO, PLUGIN_NAME),
											npTools.getOprParser(OPERATOR_LINUX_INTO)),
									termParser).optional()
					}).map(new org.codehaus.jparsec.functors.Map<Object[], Node>() {
						public Node map(Object[] vals) {
							if (vals[2] == null) {
								Node node = new PrintRuleNode(((Node) vals[0]).getScannerInfo());
								node.addChild((Node) vals[0]);
								node.addChild("alpha", (Node) vals[1]);
								return node;
							}
							Node node = new PrintToFileRuleNode(((Node) vals[0]).getScannerInfo());
							node.addChild((Node) vals[0]);
							node.addChild("alpha", (Node) vals[1]);
							Object[] toPart = (Object[]) vals[2];
							node.addChild((Node) toPart[0]);
							node.addChild("beta", (Node) toPart[1]);
							return node;
						}
					});


			parsers.put("Rule",
					new GrammarRule("outputRule", "'print' Term ('to' Term)?", printParser, PLUGIN_NAME));

		}

		return parsers;
	}

	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		// Print Rule
		if (pos instanceof PrintRuleNode) {
			return interpretPrint(interpreter, (PrintRuleNode)pos); 
		}
		if (pos instanceof PrintToFileRuleNode) {
			return interpretPrintToFile(interpreter, (PrintToFileRuleNode) pos);
		}
		return pos;
	}

	/*
	 * Interprets the Print rule.
	 */
	private ASTNode interpretPrint(Interpreter interpreter, PrintRuleNode pos) throws InterpreterException {
		if (!pos.getMessage().isEvaluated()) {
			return pos.getMessage();
		} else {
			pos.setNode(
					null, 
					new UpdateMultiset(
							new Update(
									PRINT_OUTPUT_FUNC_LOC,
									new StringElement(pos.getMessage().getValue().toString()),
									PRINT_ACTION,
									interpreter.getSelf(),
									pos.getScannerInfo()
									)), 
					null);
		}
		return pos;
	}

	/*
	 * Interprets the Write rule.
	 */
	private ASTNode interpretPrintToFile(Interpreter interpreter, PrintToFileRuleNode pos) throws InterpreterException {
		if (!pos.getMessage().isEvaluated()) {
			return pos.getMessage();
		}
		if (!pos.getFileName().isEvaluated()) {
			return pos.getFileName();
		}
		else {
			pos.setNode(
					null, 
					new UpdateMultiset(
							new Update(
									new Location(FILE_OUTPUT_FUNC_NAME,
											ElementList.create(Arrays.asList(pos.getFileName().getValue()))),
									new StringElement(pos.getMessage().getValue().toString()),
									(pos.isAppend() ? APPEND_ACTION : WRITE_ACTION),
									interpreter.getSelf(),
									pos.getScannerInfo())),
					null);
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
			functions.put(PRINT_OUTPUT_FUNC_NAME, outputFunction);
			functions.put(READ_FUNC_NAME, filereadFunction);
			functions.put(FILE_OUTPUT_FUNC_NAME, filewriteFunction);
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

	/**
	 * Write updates to files and print updates on the console.
	 * 
	 * @param source
	 * @param target
	 * @throws UnmodifiableFunctionException
	 */
	public void fireOnModeTransition(EngineMode source, EngineMode target) throws UnmodifiableFunctionException {
		//on initialization clear output messages
		if (EngineMode.emInitializingState.equals(target))
			outputMessages.clear();
		//aggregate and compose updates for print to console and print (in)to file
		if (source.equals(EngineMode.emAggregation) && target.equals(EngineMode.emStepSucceeded)) {
			outputPrintUpdates();
			writePrintInToFileUpdates();
		}
	}

	/**
	 * Writes all updates into files taking into account weather they should be appended to the file or not. Existing files are overwritten without any further warnings.
	 * 
	 * @throws UnmodifiableFunctionException
	 */
	private void writePrintInToFileUpdates() throws UnmodifiableFunctionException {
		FunctionElement fileOutputFunction = capi.getStorage().getFunction(IOPlugin.FILE_OUTPUT_FUNC_NAME);
		for (Update u : capi.getScheduler().getUpdateSet()) {
			if (APPEND_ACTION.equals(u.action) || WRITE_ACTION.equals(u.action)) {
				ListElement outputList = (ListElement) u.value;
				if (outputList != Element.UNDEF) {
					//set location to undef to prevent unnecessary output to file
					fileOutputFunction.setValue(u.loc.args, Element.UNDEF);
					List<? extends Element> lines = outputList.getList();
					//if the path is relative to the MAIN specification file, make it absolute.
					String path2spec = "";
					String fileName = u.loc.args.get(0).toString();
					if (!new File(fileName).isAbsolute())
						path2spec = capi.getSpec().getFileDir();
					String outputFile = Tools.concatFileName(path2spec, fileName);
					FileWriter fw = null;
					try {
						fw = new FileWriter(outputFile, APPEND_ACTION.equals(u.action));
						for (Element line : lines)
							fw.append(line + System.lineSeparator());
					}
					catch (IOException e) {
						throw new CoreASMError("File " + outputFile + " could not be created.");
					}
					finally {
						if (fw != null)
							try {
								fw.close();
							}
						catch (IOException e) {
								e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * Print the value of the print location to the outputSteam which depends on the user interface of CoreASM, i.e. EngineDriver
	 */
	private void outputPrintUpdates() {
		try {
			FunctionElement outputFunction = capi.getStorage().getFunction(IOPlugin.PRINT_OUTPUT_FUNC_NAME);
			String msgs = outputFunction.getValue(PRINT_OUTPUT_FUNC_LOC.args).toString();
			if (outputStream == null)
				outputMessages.add(msgs);
			outputStream.print(msgs);
			outputFunction.setValue(PRINT_OUTPUT_FUNC_LOC.args, new StringElement(""));
		}
		catch (UnmodifiableFunctionException e) {
			// Should not happen
			throw new CoreASMError("Output function is unmodifiable.");
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
		synchronized (this) {
			aggregatePrint(pluginAgg);
			aggregateWrite(pluginAgg);
			aggregateAppend(pluginAgg);
		}
	}

	/**
	 * Aggregate updates for each write locations. Whenever write as well as append updates for the same location exists, the updates are not consistent.
	 * @param pluginAgg
	 */
	public void aggregateWrite(PluginAggregationAPI pluginAgg) {
		Set<Location> writeLocsToAggregate = pluginAgg.getLocsWithAnyAction(WRITE_ACTION);
		Set<Location> appendLocsToAggregate = pluginAgg.getLocsWithAnyAction(APPEND_ACTION);

		for (Location writeLoc : writeLocsToAggregate) {
			// if regular update affects this location
			if (pluginAgg.regularUpdatesAffectsLoc(writeLoc)) {
				pluginAgg.handleInconsistentAggregationOnLocation(writeLoc, this);
			}
			else {
				Element locValue = null;
				//mark at least one inconsistent update
				for (Update update : pluginAgg.getLocUpdates(writeLoc)) {
					if (WRITE_ACTION.equals(update.action)) {
						//different values for the same location
						if (locValue != null && locValue.equals(update.value))
							pluginAgg.flagUpdate(update, Flag.FAILED, this);
						else {
							locValue = update.value;
							//append and write within the same step for the same location
							if (appendLocsToAggregate.contains(writeLoc)) {
								pluginAgg.flagUpdate(update, Flag.FAILED, this);
							}
							else {
								pluginAgg.flagUpdate(update, Flag.SUCCESSFUL, this);
							}
						}
						if (!(update.value instanceof ListElement)) {
							pluginAgg.addResultantUpdate(
									new Update(
											writeLoc,
											new ListElement(Arrays.asList(new Element[] { update.value })),
											update.action,
											update.agents,
											update.sources),
									this);
						}
						else {
							pluginAgg.addResultantUpdate(
									update,
									this);
						}
					}
				}
			}
		}
	}

	/**
	 * Aggregate updates for each append location. Whenever write as well as append updates for the same location exists, the updates are not consistent.
	 * @param pluginAgg
	 */
	public void aggregateAppend(PluginAggregationAPI pluginAgg) {
		Set<Location> writeLocsToAggregate = pluginAgg.getLocsWithAnyAction(WRITE_ACTION);
		Set<Location> appendLocsToAggregate = pluginAgg.getLocsWithAnyAction(APPEND_ACTION);

		for (Location appendLoc : appendLocsToAggregate) {
			// if regular update affects this location
			if (pluginAgg.regularUpdatesAffectsLoc(appendLoc)) {
				pluginAgg.handleInconsistentAggregationOnLocation(appendLoc, this);
			}
			else {
				//mark at least one inconsistent update
				for (Update update : pluginAgg.getLocUpdates(appendLoc)) {
					if (APPEND_ACTION.equals(update.action)) {
						//append and write within the same step for the same location
						if (writeLocsToAggregate.contains(appendLoc)) {
							pluginAgg.flagUpdate(update, Flag.FAILED, this);
						}
						else {
							pluginAgg.flagUpdate(update, Flag.SUCCESSFUL, this);
						}
						pluginAgg.addResultantUpdate(
								update,
								this);
					}
				}
			}
		}
	}

	/**
	 * Aggregate updates for the print location.
	 * @param pluginAgg
	 */
	public void aggregatePrint(PluginAggregationAPI pluginAgg) {
		// all locations on which contain print actions
		Set<Location> locsToAggregate = pluginAgg.getLocsWithAnyAction(PRINT_ACTION);
		Set<Element> contributingAgents = new HashSet<Element>();
		Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();

		// for all locations to aggregate
		for (Location l : locsToAggregate) {
			if (l.equals(PRINT_OUTPUT_FUNC_LOC)) {
				String outputResult = "";

				// if regular update affects this location
				if (pluginAgg.regularUpdatesAffectsLoc(l)) {
					pluginAgg.handleInconsistentAggregationOnLocation(l, this);
				}
				else {
					for (Update update : pluginAgg.getLocUpdates(l)) {
						if (update.action.equals(PRINT_ACTION)) {
							outputResult += update.value.toString() + "\n";
							// flag update aggregation as successful for this update
							pluginAgg.flagUpdate(update, Flag.SUCCESSFUL, this);
							contributingAgents.addAll(update.agents);
							contributingNodes.addAll(update.sources);
						}
					}
				}
				pluginAgg.addResultantUpdate(
						new Update(
								PRINT_OUTPUT_FUNC_LOC,
								new StringElement(outputResult),
								Update.UPDATE_ACTION,
								contributingAgents,
								contributingNodes),
						this);
			}
		}

	}

	/**
	 * Compose print updates, write updates and append updates in turbo asm blocks.
	 * @param compAPI
	 */
	public void compose(PluginCompositionAPI compAPI) {
		synchronized (this) {
			composePrint(compAPI);
			composeAppend(compAPI);
		}
	}

	/**
	 * Compose write and append print updates. The type write or append as well as the order of the updates are taken into account.
	 * @param compAPI
	 */
	private void composeAppend(PluginCompositionAPI compAPI) {
		for (Location l : compAPI.getAffectedLocations()) {
			if (!FILE_OUTPUT_FUNC_NAME.equals(l.name))
				continue;
			LinkedList<Element> elems1 = new LinkedList<>();
			LinkedList<Element> elems2 = new LinkedList<>();
			Set<Element> contributingAgents = new HashSet<Element>();
			Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();
			String action = APPEND_ACTION;

			// if the second set does not have a basic update, 
			// add all the updates from the first set as well
			if (!compAPI.isLocUpdatedWithActions(2, l, Update.UPDATE_ACTION)) {
				for (Update update : compAPI.getLocUpdates(1, l)) {
					if (APPEND_ACTION.equals(update.action) || WRITE_ACTION.equals(update.action)) {
						Element value = update.value;
						if (!(update.value instanceof ListElement))
							value = new ListElement(Arrays.asList(new Element[] { value }));
						ListElement list = (ListElement) value;
						elems1.addAll(list.getList());
						contributingAgents.addAll(update.agents);
						contributingNodes.addAll(update.sources);
					}
					if (WRITE_ACTION.equals(update.action))
						action = WRITE_ACTION;
				}
			}
			for (Update update : compAPI.getLocUpdates(2, l)) {
				if (APPEND_ACTION.equals(update.action) || WRITE_ACTION.equals(update.action)) {
					Element value = update.value;
					if (!(update.value instanceof ListElement))
						value = new ListElement(Arrays.asList(new Element[] { value }));
					ListElement list = (ListElement) value;
					elems2.addAll(list.getList());
					contributingAgents.addAll(update.agents);
					contributingNodes.addAll(update.sources);
				}
				if (WRITE_ACTION.equals(update.action)) {
					action = WRITE_ACTION;
					elems1.clear();
				}
			}
			if (!elems1.isEmpty() || !elems2.isEmpty()) {
				LinkedList<Element> outputResult = elems1;
				if (outputResult.isEmpty())
					outputResult = elems2;
				else if (!elems2.isEmpty()) {
					outputResult = new LinkedList<>();
					outputResult.addAll(elems1);
					outputResult.addAll(elems2);
				}
				compAPI.addComposedUpdate(new Update(l,
						new ListElement(new ArrayList<Element>(outputResult)),
						action, contributingAgents, contributingNodes), this);
			}
		}
	}

	private void composePrint(PluginCompositionAPI compAPI) {
			String outputResult1 = "";
			String outputResult2 = "";
			Set<Element> contributingAgents = new HashSet<Element>();
			Set<ScannerInfo> contributingNodes = new HashSet<ScannerInfo>();
			
			// First, add all the updates in the second set
			for (Update u: compAPI.getLocUpdates(2, PRINT_OUTPUT_FUNC_LOC)) {
				if (u.action.equals(PRINT_ACTION)) {
					if (!outputResult2.isEmpty())
						outputResult2 += '\n';
					outputResult2 += u.value.toString();
					contributingAgents.addAll(u.agents);
					contributingNodes.addAll(u.sources);
				}
			else
					compAPI.addComposedUpdate(u, this);
			}
			
			// if the second set does not have a basic update, 
			// add all the updates from the first set as well
			if (!compAPI.isLocUpdatedWithActions(2, PRINT_OUTPUT_FUNC_LOC, Update.UPDATE_ACTION)) {
				for (Update u: compAPI.getLocUpdates(1, PRINT_OUTPUT_FUNC_LOC)) {
					if (u.action.equals(PRINT_ACTION)) {
						if (!outputResult1.isEmpty())
							outputResult1 += '\n';
						outputResult1 += u.value.toString();
						contributingAgents.addAll(u.agents);
						contributingNodes.addAll(u.sources);
					}
				else
						compAPI.addComposedUpdate(u, this);
				}
			}
			if (!outputResult1.isEmpty() || !outputResult2.isEmpty()) {
				String outputResult = outputResult1;
				if (outputResult.isEmpty())
					outputResult = outputResult2;
				else if (!outputResult2.isEmpty())
					outputResult = outputResult1 + '\n' + outputResult2;
				compAPI.addComposedUpdate(new Update(PRINT_OUTPUT_FUNC_LOC, 
						new StringElement(outputResult), 
						PRINT_ACTION, contributingAgents, contributingNodes), this);
			}
	}

	@Override
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
