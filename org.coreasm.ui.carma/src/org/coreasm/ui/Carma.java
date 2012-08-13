/*	
 * Carma.java 	$Revision: 255 $
 * 
 * Copyright (C) 2006-2010 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-05-15 02:33:32 +0200 (So, 15 Mai 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.ui;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.CoreASMEngineFactory;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.CoreASMWarning;
import org.coreasm.engine.EngineErrorEvent;
import org.coreasm.engine.EngineErrorObserver;
import org.coreasm.engine.EngineEvent;
import org.coreasm.engine.EngineProperties;
import org.coreasm.engine.EngineStepObserver;
import org.coreasm.engine.Specification;
import org.coreasm.engine.Specification.BackgroundInfo;
import org.coreasm.engine.Specification.FunctionInfo;
import org.coreasm.engine.Specification.UniverseInfo;
import org.coreasm.engine.StepFailedEvent;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.VersionInfoProvider;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugin.PluginServiceInterface;
import org.coreasm.engine.plugins.io.IOPlugin.IOPluginPSI;
import org.coreasm.engine.plugins.io.InputProvider;
import org.coreasm.latex.CoreLaTeX;
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;


/** 
 * Runs a CoreASM specification
 *   
 * @author  Roozbeh Farahbod
 */
public class Carma implements EngineStepObserver, EngineErrorObserver, VersionInfoProvider, 
							  InputProvider, Runnable {

	private static final VersionInfo VERSION_INFO = new VersionInfo(0, 8, 1, "");
	
	private static final Logger logger = LoggerFactory.getLogger(Carma.class);
	
	private static final String APP_NAME = "Carma";
	private static final String INTRO = APP_NAME + " " + VERSION_INFO + " by Roozbeh Farahbod";
	private static final String INFO = "A command-line user interface for CoreASM engine";
	
	/* Command-line Arguments */
	private static final String ARG_ENGINE_VERBOSITY = "engine-verbosity";
	private static final String ARG_SILENT = "silent";
	private static final String ARG_STEPS = "steps";
	private static final String ARG_ENGINE_PROPERTY = "<name>=<value>";
	private static final String ARG_EMPTY_UPDATES_STOP = "empty-updates";
	private static final String ARG_SAME_UPDATES_STOP = "same-updates";
	private static final String ARG_EMPTY_AGENTS_STOP = "no-agent";
	private static final String ARG_PRINT_PARSE_TREE = "parse-tree";
	private static final String ARG_PRINT_VOCABULARY = "print-vocabulary";
	private static final String ARG_MARKSTEPS = "marksteps";
	private static final String ARG_STACKTRACE = "stacktrace";
	private static final String ARG_DUMP_UPDATES = "dump-updates";
	private static final String ARG_DUMP_EACH_STATE = "dump-state";
	private static final String ARG_DUMP_FINAL_STATE = "dump-final-state";
	private static final String ARG_DUMP_ENGINE_PROPERTIES = "dump-properties";
	private static final String ARG_PLUGIN_LOAD_REQUEST_LONG = "load-plugins";
	private static final String ARG_PLUGIN_LOAD_REQUEST = "<plugin-name>,...";
	private static final String ARG_PRINT_INFO = "version";
	private static final String ARG_PRINT_LAST_AGENTS = "print-agents";
	private static final String ARG_PRINT_PROCESSOR_STATS = "print-processor-stats";
	private static final String ARG_MAX_THREADS = "max-threads";
	private static final String ARG_MIN_BATCH_SIZE = "thread-batch-size";
	private static final String ARG_PRINT_SPEC = "print-spec";
	private static final String ARG_PRINT_LOADED_SPEC = "print-loaded-spec";
	private static final String ARG_LATEX_OUTPUT = "tolatex";
	private static final String ARG_SPEC_FILE = "file";
	private static final String VERB_OFF = "off";
	private static final String VERB_ERROR = "error";
	private static final String VERB_WARNING = "warn";
	private static final String VERB_INFO = "info";
	private static final String VERB_DEBUG = "debug";
	
	private int steps = 1;
	private String fileName = "";
	private boolean silent = false;
	private boolean stopEmptyUpdates = false;
	private boolean stopSameUpdates = false;
	private boolean stopEmptyActiveAgents = false;
	private boolean printParseTree = false;
	private boolean printVocabulary = false;
	private boolean markSteps = false;
	private boolean printStackTrace = false;
	private boolean printInfo = false;
	private boolean dumpUpdates = false;
	private boolean dumpFinalState = false;
	private boolean dumpEachState = false;
	private boolean dumpEngineProperties = false;
	private String pluginLoadRequest = null;
	private boolean printLastAgents = false;
	private boolean printProcessorStats = false;
	private boolean printSpec = false;
	private boolean printLoadedSpec = false;
	private boolean toLatex = false;
	private String[] engineProperties = null;
	private int maxThreads = 1;
	private int batchSize = 1;
	private String[] arguments = null;
	
	/* Other information gathered in run */
	private boolean updateFailed = false;
	private CoreASMError lastError = null;
	private String stepFailedMsg = "";
	private UpdateMultiset lastUpdateSet = null;

	private CoreASMEngine engine = null;
	
	/*
	 * Processes the command-line arguments
	 */
	private JSAPResult processArguments(String[] args) {

        SimpleJSAP jsap = null;
		try {
	        FlaggedOption propOption = new FlaggedOption( ARG_ENGINE_PROPERTY, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'D', JSAP.NO_LONGFLAG, 
    		"Sets an engine property.");
	        propOption.setAllowMultipleDeclarations(true);

    		jsap = new SimpleJSAP( 
			        APP_NAME, 
			        INFO,
			        new Parameter[] {
			            
			        	new FlaggedOption( ARG_ENGINE_VERBOSITY, JSAP.STRING_PARSER, VERB_OFF, JSAP.REQUIRED, 'v', ARG_ENGINE_VERBOSITY, 
			                "Sets the engine's verbosity level which can be '" 
			            		+ VERB_ERROR + "', '" + VERB_WARNING + "', '" + VERB_INFO + ", '" + VERB_DEBUG + "', or '" + VERB_OFF + "'."),
			            
			            new Switch( ARG_SILENT, 'q', ARG_SILENT, "Do not print any message."),
					            
			            new Switch( ARG_LATEX_OUTPUT, JSAP.NO_SHORTFLAG, ARG_LATEX_OUTPUT, "Generate LaTeX output."),

			            new FlaggedOption( ARG_STEPS, JSAP.INTEGER_PARSER, "-1", JSAP.REQUIRED, 's', ARG_STEPS, 
				                "Sets the maximum number of steps before termination."),
				            
			        	new FlaggedOption( ARG_MAX_THREADS, JSAP.INTEGER_PARSER, "1", JSAP.REQUIRED, 'c', ARG_MAX_THREADS, 
				                "Sets the maximum number of execution threads to be used for simulation."),
				            
			        	new FlaggedOption( ARG_MIN_BATCH_SIZE, JSAP.INTEGER_PARSER, "1", JSAP.REQUIRED, JSAP.NO_SHORTFLAG , ARG_MIN_BATCH_SIZE, 
				                "Sets the minimum number of agents assigned to every thread in a multi-threaded simulation."),
				
				        propOption,
				        
			            new Switch( ARG_EMPTY_UPDATES_STOP, 'y', ARG_EMPTY_UPDATES_STOP, "Stop when a step returns an empty set of updates."),
			            
			            new Switch( ARG_SAME_UPDATES_STOP, 'l', ARG_SAME_UPDATES_STOP, "Stop when a step returns the same set of updates as the previous step."),

			            new Switch( ARG_EMPTY_AGENTS_STOP, 'p', ARG_EMPTY_AGENTS_STOP, "Stop when there is no agent with a defined program."),

			            new Switch( ARG_PRINT_INFO, JSAP.NO_SHORTFLAG, ARG_PRINT_INFO, "Print version information."),

			            new Switch( ARG_PRINT_PARSE_TREE, JSAP.NO_SHORTFLAG, ARG_PRINT_PARSE_TREE, "Print parse tree." ),

			            new Switch( ARG_PRINT_VOCABULARY, JSAP.NO_SHORTFLAG, ARG_PRINT_VOCABULARY, "Print vocabulary." ),

			            new Switch( ARG_MARKSTEPS, 'm', ARG_MARKSTEPS, "Mark the end of each step." ),

			            new Switch( ARG_STACKTRACE, 't', ARG_STACKTRACE, "Print the stack trace of errors."),

			            new Switch( ARG_DUMP_UPDATES, 'u', ARG_DUMP_UPDATES, "Dump the updates after each step."),

			            new Switch( ARG_DUMP_EACH_STATE, 'e', ARG_DUMP_EACH_STATE, "Dump the state after each step."),

			            new Switch( ARG_DUMP_FINAL_STATE, 'f', ARG_DUMP_FINAL_STATE, "Dump the state at the end."),

			            new Switch( ARG_DUMP_ENGINE_PROPERTIES, JSAP.NO_SHORTFLAG, ARG_DUMP_ENGINE_PROPERTIES, "Dump engine properties."),

			        	new FlaggedOption( ARG_PLUGIN_LOAD_REQUEST, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG , ARG_PLUGIN_LOAD_REQUEST_LONG, 
		                		"A comma separated list of plugins to be loaded in addition to the specification plugins."),

		                new Switch( ARG_PRINT_LAST_AGENTS, 'a', ARG_PRINT_LAST_AGENTS, "Print the set of selected agents after each step."),

			            new Switch( ARG_PRINT_SPEC, JSAP.NO_SHORTFLAG, ARG_PRINT_SPEC, "Print the text of the specification and exit."),

			            new Switch( ARG_PRINT_LOADED_SPEC, JSAP.NO_SHORTFLAG, ARG_PRINT_LOADED_SPEC, "Print the loaded specification (i.e., with possible modifications)."),

			            new Switch( ARG_PRINT_PROCESSOR_STATS, JSAP.NO_SHORTFLAG, ARG_PRINT_PROCESSOR_STATS, "Print some stats on processor utilization."),

			            new UnflaggedOption( ARG_SPEC_FILE, JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NOT_GREEDY, 
			            		"CoreASM specification file" )
			    
			        }
			    );
		} catch (JSAPException e) {
			e.printStackTrace();
		}
            
		JSAPResult config = jsap.parse(args);    
        if ( jsap.messagePrinted() ) System.exit( 1 );
        
        String  vLevel = config.getString(ARG_ENGINE_VERBOSITY).toUpperCase();
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        
        if (root instanceof ch.qos.logback.classic.Logger) {
        	ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)root;
    		if (vLevel.equals("ERROR")) 
    			rootLogger.setLevel(ch.qos.logback.classic.Level.ERROR);
    		else if (vLevel.equals("WARNING"))
    			rootLogger.setLevel(ch.qos.logback.classic.Level.WARN);
    		else if (vLevel.equals("INFO"))
    			rootLogger.setLevel(ch.qos.logback.classic.Level.INFO);
    		else if (vLevel.equals("DEBUG"))
    			rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
    		else if (vLevel.equals("OFF"))
    			rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
    		else {
    			System.err.println("WARNING: Invalid verbosity level; will use default (" + VERB_OFF + ").");
    			rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
    		}
        } else {
        	logger.warn("Could not set verbosity level. The feature is supported only if logging with Logback.");
        }
        

		fileName = config.getString(ARG_SPEC_FILE);
		silent = config.getBoolean(ARG_SILENT);
		steps = config.getInt(ARG_STEPS);
		stopEmptyUpdates = config.getBoolean(ARG_EMPTY_UPDATES_STOP);
		stopSameUpdates = config.getBoolean(ARG_SAME_UPDATES_STOP);
		stopEmptyActiveAgents = config.getBoolean(ARG_EMPTY_AGENTS_STOP);
		printParseTree = config.getBoolean(ARG_PRINT_PARSE_TREE);
		printVocabulary = config.getBoolean(ARG_PRINT_VOCABULARY);
		markSteps = config.getBoolean(ARG_MARKSTEPS);
		printStackTrace = config.getBoolean(ARG_STACKTRACE);
		dumpUpdates = config.getBoolean(ARG_DUMP_UPDATES);
		dumpEachState = config.getBoolean(ARG_DUMP_EACH_STATE);
		dumpFinalState = config.getBoolean(ARG_DUMP_FINAL_STATE);
		dumpEngineProperties = config.getBoolean(ARG_DUMP_ENGINE_PROPERTIES);
		pluginLoadRequest = config.getString(ARG_PLUGIN_LOAD_REQUEST);
		printLastAgents = config.getBoolean(ARG_PRINT_LAST_AGENTS);
		printProcessorStats = config.getBoolean(ARG_PRINT_PROCESSOR_STATS);
		printInfo = config.getBoolean(ARG_PRINT_INFO);
		maxThreads = config.getInt(ARG_MAX_THREADS);
		batchSize = config.getInt(ARG_MIN_BATCH_SIZE);
		toLatex = config.getBoolean(ARG_LATEX_OUTPUT);
		printSpec = config.getBoolean(ARG_PRINT_SPEC);
		printLoadedSpec = config.getBoolean(ARG_PRINT_LOADED_SPEC);
		engineProperties = config.getStringArray(ARG_ENGINE_PROPERTY);
		
		if (fileName == null && !printInfo) {
			System.err.println("Error: specification file is required.");
			System.exit(1);
		}
		if (steps < 1 && !(steps == -1 || toLatex)) {
			System.err.println("Error: the number of steps must be greater than zero.");
			System.exit(1);
		}
		return config;
	}
	
	/*
	 * Log a message with new line and Carma signature.
	 */
	private void logln(String msg) {
		silentLogln("* Carma: " + msg);
	}
	
	/*
	 * Log a message with new line.
	 */
	private void silentLogln(String msg) {
		if (!silent)
			System.out.println(msg);
	}
	
	private boolean isTerminationConditionReached(CoreASMEngine engine, int currentStep) {
		synchronized (this) {
			if (updateFailed) {
				return true;
			}
		}
		
		if (engine.getEngineMode().equals(EngineMode.emTerminated))
			return true;

		if (steps >= 0 && currentStep > steps) {
			silentLogln("");
			logln("Stopped after " + steps + ((currentStep>1)?" steps.":" step."));
			return true;
		}
		if (stopEmptyUpdates && engine.getUpdateSet(0).isEmpty()) {
			silentLogln("");
			logln("Stopped due to an empty update set.");
			return true;
		}
		if (stopSameUpdates &&  engine.getUpdateSet(0).equals(lastUpdateSet)) {
			silentLogln("");
			logln("Stopped as the same update set is computed in two consecutive steps.");
			return true;
		}
		if (stopEmptyActiveAgents && engine.getAgentSet().size() < 1) {
			silentLogln("");
			logln("Stopped as there is no agent with a defined program.");
			return true;
		}
		return false;
	}
	
	public void error(CoreASMEngine engine) {
		StringBuffer msg = new StringBuffer("Engine error " + Tools.getEOL());
		if (lastError != null)
			msg.append(Tools.getEOL() + lastError.showError());
		error(engine, msg.toString());
	}

	public void error(CoreASMEngine engine, String msg, Exception e) {
		error(engine, "* Carma * : " + msg + " (" + e.getMessage() + ")");
	}
	
	public void error(CoreASMEngine engine, String msg) {
		System.err.println("* Carma * : " + msg);
		printWarnings(engine);
		if (engine != null) {
			engine.terminate();
			engine.waitWhileBusy();
		}
		System.exit(1);
	}
	
	private void printWarnings(CoreASMEngine engine) {
		if (engine == null)
			return;
		
		List<CoreASMWarning> warnings = engine.getWarnings();
		if (warnings.size() > 0) {
			logln("The following warning " + (warnings.size()==1?"message":"messages") + " has also been issued during the last step:");
			for (CoreASMWarning w: warnings) 
				logln(w.showWarning());
		}
	}

	public static void main(String[] args) {
		(new Carma()).run(args);
	}
	
	/**
	 * @param args command-line arguments
	 */
	public void run(String[] args) {

		processArguments(args);
		
		// if print-spec flag is on, print the specification and exit.
		if (printSpec) {
			printSpecification(fileName);
			return;
		}
		
		CoreASMEngine tempEngine = CoreASMEngineFactory.createEngine();

		setEngineProperties(tempEngine);
		
		tempEngine.addObserver(this);
		if (printStackTrace)
			tempEngine.setProperty(EngineProperties.PRINT_STACK_TRACE, EngineProperties.YES);
		if (printProcessorStats)
			tempEngine.setProperty(EngineProperties.PRINT_PROCESSOR_STATS_PROPERTY, EngineProperties.YES);
		tempEngine.initialize();
		tempEngine.waitWhileBusy();
		
		synchronized (this) {
			engine = tempEngine;
		}
		
		if (printInfo) {
			System.out.println(INTRO);
			System.out.println("CoreASM Engine " + engine.getVersionInfo());
			System.out.println("Plugins: ");
			Map<String,VersionInfo> list = engine.getPluginsVersionInfo();
			TreeSet<String> sortedSet = new TreeSet<String>();
			
			for (String name: list.keySet()) {
				VersionInfo vinfo = list.get(name);
				sortedSet.add("   " + name + " " + (vinfo==null?"":vinfo.toString()));
			}
			for (String pinfo: sortedSet)
				System.out.println(pinfo);
			
			engine.terminate();
			engine.waitWhileBusy();
			return;
		}
	
		/* Print vocabulary */
		if (printVocabulary) {
			engine.waitWhileBusy();
			if (engine.getEngineMode() == EngineMode.emError) 
				error(engine);
			
			engine.parseSpecificationHeader(fileName, true);
			engine.waitWhileBusy();
			if (engine.getEngineMode() == EngineMode.emError) 
				error(engine);

			Specification spec = engine.getSpec();
			StringBuffer output = new StringBuffer();
			
			output.append("Printing vocabulary:" + Tools.getEOL());
			output.append(" - Backgrounds" + Tools.getEOL());
			final Set<BackgroundInfo> bkgs = spec.getDefinedBackgrounds();
			for (BackgroundInfo bInfo: bkgs)
				output.append("    - " + bInfo.name + " (by " + bInfo.plugin  + ")" + Tools.getEOL());
			
			output.append(" - Universes" + Tools.getEOL());
			final Set<UniverseInfo> univs = spec.getDefinedUniverses();
			for (UniverseInfo uInfo: univs)
				output.append("    - " + uInfo.name + " (by " + uInfo.plugin  + ")" + Tools.getEOL());
			
			output.append(" - Functions" + Tools.getEOL());
			final Set<FunctionInfo> functions = spec.getDefinedFunctions();
			for (FunctionInfo fInfo: functions) 
				output.append("    - " + fInfo.name + " (by " + fInfo.plugin  + ")" + Tools.getEOL());
			
			logln(output.toString());
			
			engine.terminate();
			engine.waitWhileBusy();
			return;
		}

		
		engine.loadSpecification(fileName);
		
		logln("Loading the specification.");
		engine.waitWhileBusy();
		if (engine.getEngineMode() == EngineMode.emError)
			error(engine);

		// we reapply the engine properties values
		setEngineProperties(engine);
		
		if (printLoadedSpec) {
			logln("--- Loaded Specification Begins ---" + Tools.getEOL() + engine.getSpec().getText());
			logln("--- Loaded Specification Ends ---");
		}
		
		/*
		if (Loger.verbosityLevel >= Loger.INFORMATION) {
			Loger.log(Loger.INFORMATION, Loger.ui, "Parser Tree:");
			Loger.log(Loger.INFORMATION, Loger.ui, engine.getSpec().getRootNode().buildTree("", 0));
		}
		*/
		if (printParseTree) {
			logln("Parser Tree:");
			logln(engine.getSpec().getRootNode().buildTree("", 0));
		}
		
		if (dumpEngineProperties) {
			logln("--- Engine Properties Begins --- ");
			engine.getProperties().list(System.out);
			logln("--- Engine Properties Ends --- ");
		}
		
		PluginServiceInterface pi = engine.getPluginInterface("IOPlugin");
		if (pi != null) {
			((IOPluginPSI)pi).setInputProvider(this);
			/*
			 * No dialog box for Carma 
			 * 
			((IOPluginPSI)pi).setInputProvider(new InputProvider() {

				public String getValue(String Message) {
					String input = JOptionPane.showInputDialog(null, Message, "");
					return input;
				}
				
			});
			*/
		}

		if (engine.getEngineMode() == EngineMode.emError)
			error(engine);
		
		// Converting to LaTeX source file
		if (toLatex) {
			final Node rootnode = engine.getSpec().getRootNode();
			final Set<String> ids = new HashSet<String>();
			final Specification spec = engine.getSpec();
			for (FunctionInfo f: spec.getDefinedFunctions()) {
				if (!f.plugin.equals("SignaturePlugin"))
					ids.add(f.name);
			}
			String fname = spec.getAbsolutePath();
			if (fname.endsWith(".coreasm"))
				fname = fname.substring(0, fname.length() - 8) + ".tex";
			else
				if (fname.endsWith(".casm"))
					fname = fname.substring(0, fname.length() - 5) + ".tex";
				else
					fname = fname + ".tex";
			try {
				writeToFile(fname, CoreLaTeX.toLatex(rootnode, ids));
				logln("LaTeX file generated.");
			} catch (IOException e) {
				System.err.println("Failed writing to file " + fileName);
				System.err.println("  - Error: " + e.getMessage());
			}
			if (steps == -1)  {
				engine.terminate();
				engine.waitWhileBusy();
				return;
			}
		}
		
		/* No initial step anymore
		
		logln("Performing the initial step.");
		engine.step();
		engine.waitForIdleOrError();
		if (engine.getEngineMode() == EngineMode.emError)
			error();
		
		if (updateFailed) {
			engine.terminate();
			logln("Execution concluded due to an incosistent update set.");
			logln(stepFailedMsg);
			System.exit(1);
		}
		
		if (dumpUpdates) {
			logln("Initial updates are: " + engine.getUpdateSet(0));
		}
		
		if (dumpEachState) {
			logln("Initial State is:\n" + engine.getState());
		}
		//logln(" done.");
		*/
		
		int currentStep = 1;
		//int agentsSum = 0;
		logln("Starting the execution using " + maxThreads + " thread(s).\n");
		do {
			if (currentStep == 1)
				lastUpdateSet = new UpdateMultiset();
			else
				lastUpdateSet = new UpdateMultiset(engine.getUpdateSet(0));
			engine.step();
			engine.waitWhileBusy();
			
			if (engine.getEngineMode() == EngineMode.emError)
				error(engine);
			if (updateFailed)
				break;

			if (markSteps && engine.getAgentSet().size() > 0) {
				silentLogln("");
				logln(" + ----- end of STEP " + currentStep + " ----- + \n");
			}

			if (dumpUpdates) 
				logln("Updates after step " + currentStep + 
						" are: " + engine.getUpdateSet(0));
			
			if (dumpEachState)
				logln("State after step " + currentStep + 
						" is:\n" + engine.getState());
			
			if (printLastAgents) 
				logln("Agents involved in the last completed step: " + engine.getLastSelectedAgents());

			//agentsSum += engine.getLastSelectedAgents().size();
			
			currentStep++;
		} while (!isTerminationConditionReached(engine, currentStep));
		
		if (updateFailed) {
			error(engine, "Execution concluded due to an incosistent update set." 
					+ Tools.getEOL() + stepFailedMsg);
		}
		
		//logln("Average number of agents per step: " + agentsSum / (currentStep - 1));
		
		if (!dumpEachState && dumpFinalState) {
			silentLogln("");
			logln("Final state is:\n" + engine.getState());
		}

		engine.terminate();
		engine.waitWhileBusy();
		logln("Execution concluded.");

	}
	
	private void setEngineProperties(CoreASMEngine engine) {
		engine.setProperty(EngineProperties.MAX_PROCESSORS, String.valueOf(maxThreads));
		engine.setProperty(EngineProperties.AGENT_EXECUTION_THREAD_BATCH_SIZE, String.valueOf(batchSize));
		if (engineProperties != null && engineProperties.length > 0) {
			String prop;
			String value;
			for (String ep: engineProperties) {
				if (ep.length() < 1)
					continue;
				int i = ep.indexOf('=');
				if (i < 1 || i == ep.length() - 1) 
					error(engine, "Invalid property-value option: " + ep);
				prop = ep.substring(0, i);
				value = ep.substring(i + 1, ep.length());
				logger.info("Setting value of engine property '{}' to '{}'.", prop, value);
				engine.setProperty(prop, value);
			}
		}
		if (pluginLoadRequest != null)
			engine.setProperty(EngineProperties.PLUGIN_LOAD_REQUEST_PROPERTY, pluginLoadRequest);
	}

	private void printSpecification(String fileName) {
		Specification spec = null; 
		try {
			spec = new Specification(null, new File(fileName));
		} catch (FileNotFoundException e) {
			error(null, "Specification file not found.", e);
//		} catch (XNIException e) {
//			error(null, "Invalid ODT format.", e);
		} catch (IOException e) {
			error(null, "Error reading the specification file.", e);
		}
		if (spec != null) {
			logln("--- Specification Begins ---" + Tools.getEOL() + spec.getText());
			logln("--- Specification Ends ---");
		}
	}

	private void writeToFile(String fileName, String content) throws IOException {
		Writer writer = new FileWriter(fileName);
		writer.write(content);
		writer.close();
	}
	
	public void update(EngineEvent event) {
		
		// Looking for StepFailed
		if (event instanceof StepFailedEvent) {
			StepFailedEvent sEvent = (StepFailedEvent)event;
			synchronized (this) {
				updateFailed = true;
				stepFailedMsg = sEvent.reason;
			}
		}
		
		// Looking for errors
		else if (event instanceof EngineErrorEvent) {
			synchronized (this) {
				lastError = ((EngineErrorEvent)event).getError();
			}
		}
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public String getValue(String message) {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));       
		System.out.print(message + " ");
		String result;
		try {
			result = stdin.readLine();
		} catch (IOException e) {
			result = "";
			logger.warn("Error reading from the standard input.");
		}
		return result;
	}
	
	/**
	 * Basic constructor. Private to ensure construction is done through the static methods main(..) or start(..)
	 * 
	 */
	private Carma(){}
	
	/**
	 * Constructor for threaded execution.
	 * 
	 * @param args command-line arguments
	 */
	private Carma(String[] args) {
		arguments = args.clone();
	}
	
	/**
	 * Executes a thread. Necessary to implement Runnable.
	 */
	public void run() {
		if( arguments != null )
			run( arguments );
	}

	/**
	 * Access function for creating and executing Carma as a thread.
	 * 
	 * @param args command-line arguments
	 * @return the instance of Carma created
	 */
	public static Carma start(String[] args) {
		final Carma carma = new Carma(args);
		new Thread(carma).start();
		return carma;
	}

	/**
	 * Returns the plugin service interface associated with the given plugin.
	 * This is a blocking method and it waits until the engine is created.
	 * 
	 * @param pName name of the plugin
	 */
	public PluginServiceInterface getPluginInterface(String pName) {
		final CoreASMEngine engine = getEngine(true);
		return engine.getPluginInterface(pName);
	}
	
	/** 
	 * Returns a reference to the CorEASM engine created by Carma.
	 * If <code>blocking</code> is <code>true</code>, it waits until 
	 * the engine is created. Otherwise, if the engine is not yet created, 
	 * this method returns null.
	 * 
	 * @param blocking determines if this method should wait until engine is created
	 */
	private CoreASMEngine getEngine(boolean blocking) {
		if (!blocking) 
			synchronized (this) {return engine;}
		else {
			while (getEngine(false) == null)
				try { Thread.sleep(50); } catch (InterruptedException e) {}
			getEngine(false).waitWhileBusy();
			return getEngine(false);
		}
	}

}
