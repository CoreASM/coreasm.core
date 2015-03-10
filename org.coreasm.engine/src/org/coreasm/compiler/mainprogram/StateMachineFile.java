package org.coreasm.compiler.mainprogram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CompilerPathConfig;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.classlibrary.LibraryEntryType;
import org.coreasm.compiler.classlibrary.MemoryInclude;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.codefragment.CodeFragmentException;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.LibraryEntryException;
import org.coreasm.compiler.interfaces.CompilerExtensionPointPlugin;
import org.coreasm.compiler.interfaces.CompilerInitCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
import org.coreasm.compiler.mainprogram.statemachine.EngineTransition;
import org.coreasm.compiler.mainprogram.statemachine.StateMachine;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.mainprogram.MainFileHelper;
import org.coreasm.engine.kernel.Kernel;

/**
 * Advanced LibraryEntry representing the Main class of the compiled code.
 * Processes vocabulary extender plugins and init code plugins, to get
 * additional code for extension.
 * @author Markus Brenner
 *
 */
public class StateMachineFile extends MemoryInclude{
	private StateMachine stateMachine;
	private ArrayList<MainFileEntry> extensions;
	private ArrayList<CodeFragment> initCodes;	
	private String initRule;
	
	/**
	 * Constructs a new, empty Main File
	 */
	public StateMachineFile(CompilerEngine engine){
		super(engine, "StateMachine", "Kernel", LibraryEntryType.DYNAMIC);
		stateMachine = new StateMachine(engine);
		extensions = new ArrayList<MainFileEntry>();
		initCodes = new ArrayList<CodeFragment>();
		this.engine = engine;
	}
	
	/**
	 * Sets the initial rule of the program
	 * @param rule The name of the initial rule (including the package path)
	 */
	public void setInitRule(String rule){
		this.initRule = rule;
	}
	
	/**
	 * Processes a list of extension point plugins.
	 * Adds the transitions provided by all extension point plugins to the
	 * state machine.
	 * @param plugins A list of CompilerExtensionPointPlugins
	 */
	public void processExtensionPlugins(List<CompilerExtensionPointPlugin> plugins){
		for(CompilerExtensionPointPlugin p : plugins){
			List<EngineTransition> transitions = p.getTransitions();
			for(EngineTransition t : transitions){
				stateMachine.addTransition(t);
			}
		}
	}
	
	/**
	 * Processes a list of Vocabulary Extender Plugins.
	 * Adds all main file entries provided by the plugins to the file,
	 * including them as necessary in the generated main class.
	 * @param vocabularyExtenderPlugins A list of vocabulary extender plugins
	 * @throws CompilerException If an error occurred in on of the plugins
	 */
	public void processVocabularyExtenderPlugins(List<CompilerVocabularyExtender> vocabularyExtenderPlugins) throws CompilerException {
		//load extenders in order, respecting dependencies of plugins
		Map<CompilerVocabularyExtender, Boolean> isLoaded = new HashMap<CompilerVocabularyExtender, Boolean>();
		Map<String, CompilerVocabularyExtender> pluginMapping = new HashMap<String, CompilerVocabularyExtender>();
		CompilerVocabularyExtender kernel = null;
		
		//initialize data structures
		for(CompilerVocabularyExtender cve : vocabularyExtenderPlugins){
			if(cve.getName().equals(Kernel.PLUGIN_NAME)){
				kernel = cve;
				continue;
			}
			
			isLoaded.put(cve, false);
			pluginMapping.put(cve.getName(), cve);
		}
		
		//load kernel
		loadVocabExtender(kernel);
		isLoaded.put(kernel, true);
		
		//load remaining plugins
		for(int i = 0; i < vocabularyExtenderPlugins.size(); i++){
			attemptLoad(isLoaded, pluginMapping, vocabularyExtenderPlugins.get(i));
		}
	}
	
	private void attemptLoad(Map<CompilerVocabularyExtender, Boolean> loaded, Map<String, CompilerVocabularyExtender> plugins, CompilerVocabularyExtender current) throws CompilerException{
		if(!loaded.get(current)){
			for(String s : ((CompilerPlugin)current).getInterpreterPlugin().getDependencyNames()){
				CompilerVocabularyExtender dep = plugins.get(s);
				if(dep != null && !loaded.get(dep)){
					attemptLoad(loaded, plugins, dep);
				}
			}
			
			loadVocabExtender(current);
			loaded.put(current, true);
		}
	}
	
	private void loadVocabExtender(CompilerVocabularyExtender cve) throws CompilerException{
		try{
			extensions.addAll(cve.loadClasses(engine.getClassLibrary()));
		}
		catch(CompilerException e){
			if(e.getCause() instanceof EntryAlreadyExistsException){
				String tmp = ((EntryAlreadyExistsException)e.getCause()).getEntryName();
				engine.addError("Plugin " + cve.getName() + " could not load all its classes, an entry with the name " + tmp + " already exists");
			}
			else{
				engine.addError("Plugin " + cve.getName() + " could not load all its classes");
			}
			throw e;
		}
	}

	/**
	 * Processes a list of InitCode Plugins.
	 * Adds the provided CodeFragments to the initialization of the main class.
	 * The initialization is before the state machine and the initialization of
	 * the main file entries.
	 * @param initCodePlugins A list of init code plugins
	 */
	public void processInitCodePlugins(
			List<CompilerInitCodePlugin> initCodePlugins) {
		for(CompilerInitCodePlugin cicp : initCodePlugins){
			initCodes.add(cicp.getInitCode());
		}
		
	}
	
	@Override
	protected String buildContent(String entryName) throws LibraryEntryException {
		long start = System.nanoTime();
		//build the main class of the specification
		CompilerPathConfig paths = engine.getPath();
		//generate the state machine
		MainFileHelper.populateStateMachine(this.stateMachine, engine);
		//generate the state machines code
		CodeFragment smcode = null;	
		smcode = new CodeFragment("");
		
		//find scheduler policy
		
		LibraryEntry scheduler = null;
		CodeFragment finalContent = null;
		for(MainFileEntry mfe : extensions){
			if(mfe.entryType == EntryType.SCHEDULER){
				if(scheduler == null) scheduler = mfe.classFile;
				else{
					engine.addError("more than one scheduler provided, currently only one is allowed");
					throw new LibraryEntryException("only one scheduling policy can be used");
				}
			}
		}
		if(scheduler == null){
			engine.addError("no scheduler selected");
			throw new LibraryEntryException("no scheduler selected");		
		}
		
		try {
			smcode = stateMachine.generateClasses();
		} catch (Exception e) {
			engine.addError("state machine generated invalid code");
			throw new LibraryEntryException(e);
		}
		
		finalContent = new CodeFragment();
		finalContent.appendLine("package " + getPackage(entryName) + ";\n");
		finalContent.appendLine("public class StateMachine implements @RuntimePkg@.Runtime, Runnable{\n");
		finalContent.appendLine("\tprivate java.util.Map<Thread, @RuntimePkg@.Element> selfEntries = new java.util.HashMap<Thread, @RuntimePkg@.Element>();\n");
		finalContent.appendLine("\tpublic void setSelf(Thread t, @RuntimePkg@.Element e){\nselfEntries.put(t, e);\n}\n");
		finalContent.appendLine("\tpublic @RuntimePkg@.Element getSelf(Thread t){\nreturn selfEntries.get(t);\n}\n");
		finalContent.appendLine("\tprivate @RuntimePkg@.EvalStack evalStack = new @RuntimePkg@.EvalStack();\n");
		finalContent.appendLine("\tprivate boolean isRunning;\n");
		finalContent.appendLine("\tprivate @RuntimePkg@.AbstractStorage storage;\n");
		finalContent.appendLine("\tprivate @RuntimePkg@.Scheduler scheduler;\n");
		finalContent.appendLine("\tprivate java.util.Random random = new java.util.Random();\n");
		finalContent.appendLine("\tprivate @RuntimePkg@.UpdateList prevupdates;\n");
		finalContent.appendLine("\tprivate java.util.Set<@RuntimePkg@.UpdateAggregator> aggregators;\n");
		finalContent.appendLine("\tprivate String lastError;\n");
		finalContent.appendLine("\tprivate boolean abortProgram;\n");
		finalContent.appendLine("\n");
		finalContent.appendLine("\tpublic StateMachine(){\n");
		finalContent.appendLine("\t\t@RuntimePkg@.RuntimeProvider.setRuntime(this);\n");
		finalContent.appendLine("\t\tlastError = null;\n");
		finalContent.appendLine("\t\tabortProgram = false;\n");
		finalContent.appendLine("\t\tstorage = new @RuntimePkg@.HashStorage(this);\n");
		finalContent.appendLine("\t\t@RuntimePkg@.Rule initRule = new " + initRule + "();\n");
		finalContent.appendLine("\t\tscheduler = new @RuntimePkg@.Scheduler(initRule, new "  + paths.getEntryName(scheduler) + "());\n");
		finalContent.appendLine("\t\taggregators = new java.util.HashSet<>();\n");
		finalContent.appendLine("\t\tisRunning = true;\n");
		finalContent.appendLine("\t}\n");
		finalContent.appendLine("\tpublic java.util.Set<@RuntimePkg@.UpdateAggregator> getAggregators(){\n");
		finalContent.appendLine("\t\treturn this.aggregators;\n");
		finalContent.appendLine("\t}\n");
		finalContent.appendLine("\tpublic @RuntimePkg@.AbstractStorage getStorage(){\n");
		finalContent.appendLine("\t\treturn this.storage;\n");
		finalContent.appendLine("\t}\n");
		finalContent.appendLine("\tpublic @RuntimePkg@.Scheduler getScheduler(){\n");
		finalContent.appendLine("\t\treturn this.scheduler;\n");
		finalContent.appendLine("\t}\n");
		finalContent.appendLine("\tpublic void error(String msg){}\n\tpublic void error(Exception e){}\n\tpublic void warning(String s, String m){}\n");
		finalContent.appendLine("\tpublic void stopEngine(){\n\t\tthis.isRunning = false;\n\t}\n");
		finalContent.appendLine("\tpublic void run(){\n");
		finalContent.appendLine("\t\t\n\t\trunMachine();\n");
		finalContent.appendLine("\t}\n");
		finalContent.appendLine("\tpublic int randInt(int max) {\n\t\treturn random.nextInt(max);\n\t}\n");
		
		//add run machine method
		finalContent.appendLine("\n");
		
		finalContent.appendLine("\tpublic void runMachine(){\n");
		//1. add all init code provided by plugins
		for(CodeFragment c : initCodes){
			finalContent.appendFragment(c);
		}
		finalContent.appendLine("\n");
		//2. add init code for all initial elements
		//2a helper code for agents:
		//retrieve agents universe
		finalContent.appendLine("\t\t@RuntimePkg@.Rule initRule = new " + initRule + "();\n");
		finalContent.appendLine("\t\tstorage.initAbstractStorage(initRule);\n");
		finalContent.appendLine("\t\t@RuntimePkg@.ProgramFunction programfct = new @RuntimePkg@.ProgramFunction();\n");
		finalContent.appendLine("\t\ttry{\n");
		finalContent.appendLine("\t\tthis.storage.addFunction(@RuntimePkg@.AbstractStorage.PROGRAM_FUNCTION_NAME, programfct);\n");
		finalContent.appendLine("\t\t}catch(Exception e){\n}\n");
		finalContent.appendLine("\t\t@RuntimePkg@.AbstractUniverse agents = this.storage.getUniverse(@RuntimePkg@.AbstractStorage.AGENTS_UNIVERSE_NAME);\n");
		finalContent.appendLine("\t\tjava.util.List<@RuntimePkg@.Element> fctArgTmp = null;\n");
		finalContent.appendLine("\t\t@RuntimePkg@.MapFunction fctMapTmp = null;\n");
		finalContent.appendLine("\t\t@RuntimePkg@.UniverseElement uniTmp = null;\n");
		finalContent.appendLine("\t\ttry{\n");
		for(MainFileEntry entry : extensions){
			switch(entry.entryType){
			case AGENT:
				finalContent.appendLine("\t\tagents.setValue(new " + paths.getEntryName(entry.classFile) + ", @RuntimePkg@.BooleanElement.TRUE);\n");
				break;
			case AGGREGATOR:
				finalContent.appendLine("\t\tthis.aggregators.add(new " + paths.getEntryName(entry.classFile) + "());\n");
				break;
			case BACKGROUND:
				finalContent.appendLine("\t\tthis.storage.addUniverse(\"" + entry.entryName + "\", new " + paths.getEntryName(entry.classFile) + "());\n");
				break;
			case FUNCTION:
				if(entry.entryName.equals("program")){
					//add elements to the program function
					finalContent.appendLine("\t\tfctMapTmp = new " + paths.getEntryName(entry.classFile) + "();\n");
					finalContent.appendLine("\t\tfor(@RuntimePkg@.Location l : fctMapTmp.getLocations(\"program\")){\n");
					finalContent.appendLine("\t\t\tstorage.setValue(l, fctMapTmp.getValue(l.args));\n");
					finalContent.appendLine("\t\t}\n");
				}
				else{
					finalContent.appendLine("\t\tthis.storage.addFunction(\"" + entry.entryName + "\", new " + paths.getEntryName(entry.classFile) + "());\n");
				}
				break;
			case FUNCTION_CAPI:
				//program function needs no parameter for the constructor, so it cannot appear here
				finalContent.appendLine("\t\tthis.storage.addFunction(\"" + entry.entryName + "\", new " + paths.getEntryName(entry.classFile) + "(new @RuntimePkg@.ControlAPI()));\n");
				break;
			case UNIVERSE:
				if(entry.entryName.equals(CompilerRuntime.AbstractStorage.AGENTS_UNIVERSE_NAME)){
					finalContent.appendLine("\t\tuniTmp = new " + paths.getEntryName(entry.classFile) + "();\n");
					finalContent.appendLine("\t\tfor (@RuntimePkg@.Location l: uniTmp.getLocations(@RuntimePkg@.AbstractStorage.AGENTS_UNIVERSE_NAME)){\n");
					finalContent.appendLine("\t\ttry {\n");
					finalContent.appendLine("\t\tagents.setValue(l.args, uniTmp.getValue(l.args));\n");
					finalContent.appendLine("\t\t} catch (@RuntimePkg@.UnmodifiableFunctionException e) {}\n");
					finalContent.appendLine("}\n");
				}
				else{
					finalContent.appendLine("\t\tthis.storage.addUniverse(\"" + entry.entryName + "\", new " + paths.getEntryName(entry.classFile) + "());\n");
				}
				break;
			default:
				//default case is for RULE and SCHEDULER cases, which do not need to be handled here
				break;
			}
		}
		finalContent.appendLine("}\ncatch(Exception e){\nSystem.out.println(\"error: conflict while initializing\");\nSystem.exit(0);\n}\n");

		finalContent.appendFragment(smcode);
			
			
			
		finalContent.appendLine("\t}\n");
	
		finalContent.appendLine("\n");
		
		//end class
		finalContent.appendLine("}");
		
		long end = System.nanoTime();
		engine.addTiming("Main File building", end - start);
		try{
			return finalContent.generateCode(engine);
		}
		catch(CodeFragmentException e){
			engine.addError("Building the main class has failed");
			engine.getLogger().error(this.getClass(), "Building the main class has failed");
			throw new LibraryEntryException(e);
		}
	}
}
