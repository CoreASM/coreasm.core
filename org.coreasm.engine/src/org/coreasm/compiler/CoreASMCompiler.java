package org.coreasm.compiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.backend.CompilerFileWriter;
import org.coreasm.compiler.backend.CompilerPacker;
import org.coreasm.compiler.backend.KernelBackend;
import org.coreasm.compiler.classlibrary.CodeWrapperEntry;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.DirectoryNotEmptyException;
import org.coreasm.compiler.exception.EmptyContextStackException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.NotCompilableException;
import org.coreasm.compiler.interfaces.CompilerBackendProvider;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerMainClassProvider;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPathPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.mainprogram.MainClass;
import org.coreasm.compiler.mainprogram.StateMachineFile;
import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.Preprocessor;
import org.coreasm.compiler.variablemanager.VarManager;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngineFactory;
import org.coreasm.engine.Engine;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Implementation of the CompilerEngine interface and the actual CoreASM compiler.
 * Provides services for plugins and controls the compilation process.
 * @author Markus Brenner
 *
 */

public class CoreASMCompiler implements CompilerEngine {	
	private LoggingHelper logging;
	
	private CompilerOptions options;
	private PluginLoader pluginLoader;
	private ClassLibrary classLibrary;
	private VarManager varManager;
	private StateMachineFile mainFile;
	private Map<String, List<CompilerPlugin>> unaryOperators;
	private Map<String, List<CompilerPlugin>> binaryOperators;
	private Map<String, CompilerFunctionPlugin> functionMapping;
	private Preprocessor preprocessor;
	private CompilerPathConfig paths;
	
	private List<String> warnings;
	private List<String> errors;
	
	private CoreASMEngine coreasm;
	
	private List<String> timings;
	private long lastTime;
	private long cTime;
	
	private boolean tryCompiling = false;
	
	private Map<String, String> globalMakros;
	
	/**
	 * Constructs a new CoreASMCompiler instance with the given options
	 * @param options The options for the compilation process
	 */
	public CoreASMCompiler(CompilerOptions options){
		init(options, null);
	}
	
	/**
	 * Constructs a new CoreASMCompiler instance using an external CoreASMEngine
	 * @param options The options for the compilation process
	 * @param engine A CoreASMEngine
	 */
	public CoreASMCompiler(CompilerOptions options, CoreASMEngine casm){
		init(options, casm);
	}
	
	private void init(CompilerOptions options, CoreASMEngine casm){		
		lastTime = System.nanoTime();
		//initialize components
		this.options = options;
		pluginLoader = new DummyLoader(this);
		classLibrary = new ClassLibrary(this);
		varManager = new VarManager();
		mainFile = new StateMachineFile(this);
		preprocessor = new Preprocessor(this);
		
		logging = new LoggingHelper();
		
		//initialize data structures
		unaryOperators = new HashMap<String, List<CompilerPlugin>>();
		binaryOperators = new HashMap<String, List<CompilerPlugin>>();
		functionMapping = new HashMap<String, CompilerFunctionPlugin>();
		
		warnings = new ArrayList<String>();
		errors = new ArrayList<String>();
		
		coreasm = casm;
		
		paths = new DefaultPaths();
		
		globalMakros = new HashMap<String, String>();
		
		timings = new LinkedList<String>();
		cTime = System.nanoTime();
		addTiming("Initialization");
	}
	
	private void setGlobalMakros(){
		globalMakros.put("RuntimePkg", paths.runtimePkg());
		globalMakros.put("BasePkg", paths.basePkg());
		globalMakros.put("StaticPkg", paths.pluginStaticPkg());
		globalMakros.put("DynamicPkg", paths.pluginDynamicPkg());
		globalMakros.put("RulePkg", paths.rulePkg());
		globalMakros.put("RuntimeProvider", paths.runtimeProvider());
	}
	
	public void addTiming(String name, long time){
		timings.add(name + ": " + (time));
	}
	
	private void addTiming(String name){
		timings.add(name + ": " + (cTime - lastTime));
	}

	/**
	 * Starts the compilation process for the CoreASM specification
	 * provided by the options Object.
	 * @throws CompilerException If an error occured during the compilation process
	 */
	public void compile() throws CompilerException{
		try{	
			getLogger().debug(CoreASMCompiler.class, "starting compiler");
			CompilerInformation info = new CompilerInformation();
			
			getLogger().debug(CoreASMCompiler.class, "loading specification");
			loadSpecification(info);
			
			lastTime = System.nanoTime();
			getLogger().debug(CoreASMCompiler.class, "preprocessing specification");
			preprocessSpecification(info);
			cTime = System.nanoTime();
			addTiming("Preprocessing");
			
			//load plugins, which have to be loaded first
			getLogger().debug(CoreASMCompiler.class, "loading first set of plugins");
			applyFirstPlugins(info);
			
			//compile specification first, so that plugins may
			//provide objects based on the parse tree
			getLogger().debug(CoreASMCompiler.class, "compiling specification");
			compileSpecification(info);
			
			getLogger().debug(CoreASMCompiler.class, "applying second set of plugins");
			applyPlugins(info);
			
			getLogger().debug(CoreASMCompiler.class, "building main file");
			//note: most operations will actually happen in the next step
			buildMain(info);
			
			getLogger().debug(CoreASMCompiler.class, "compiling java sources");
			compileSources(info);
		}
		catch(CompilerException ce){
			throw ce;
		}
		catch(Exception e){
			System.out.println("uncaught exception: "  + e.getMessage());
			e.printStackTrace();
		}
		finally{
			lastTime = System.nanoTime();
			if(!options.keepTempFiles){
				purgeTempDir();
			}
			System.out.println("end of compiler run.");
			System.out.println("" + errors.size() + " error(s) were issued" + ((errors.size() != 0) ? ":" : ""));
			for(String s : errors){
				System.out.println(" *\t" + s.replace("\n", "\n\t"));
			}
			System.out.println("" + warnings.size() + " warning(s) were issued" + ((warnings.size() != 0) ? ":" : ""));
			for(String s : warnings){
				System.out.println(" *\t" + s.replace("\n", "\n\t"));
			}
			cTime = System.nanoTime();
			addTiming("Cleanup");
			
			if(options.logTimings){
				Iterator<String> it = timings.iterator();
				while(it.hasNext()){
					System.out.println(it.next());
				}
			}
		}
	}
	
	@Override
	public Preprocessor getPreprocessor(){
		return this.preprocessor;
	}

	@Override
	public ClassLibrary getClassLibrary(){
		return this.classLibrary;
	}
	
	private void purgeTempDir(){
		getLogger().debug(CoreASMCompiler.class, "cleaning up temp directory");
		
		purgeDir(options.tempDirectory);
		
		getLogger().debug(CoreASMCompiler.class, "cleanup finished");
	}
	
	private void purgeDir(File f){
		if(f.exists()){
			if(f.isFile()){
				if(!f.delete()) getLogger().warn(CoreASMCompiler.class, "Could not delete file " + f.getAbsolutePath());
			}
			else{
				for(File d : f.listFiles()){
					purgeDir(d);
				}
				if(!f.delete()) getLogger().warn(CoreASMCompiler.class, "Could not delete directory " + f.getAbsolutePath());
			}
		}
	}

	@Override
	public PluginLoader getPluginLoader() {
		return this.pluginLoader;
	}
	
	@Override
	public VarManager getVarManager() {
		return this.varManager;
	}
	
	@Override
	public CodeFragment tryCompile(ASTNode node, CodeType type) throws CompilerException{
		this.tryCompiling = true;
		CodeFragment result = null;
		try{
			result = compile(node, type);
		}
		finally{
			this.tryCompiling = false;
		}
		return result;
	}
	
	@Override
	public CodeFragment compile(ASTNode node, CodeType type) throws CompilerException {
		getLogger().debug(CoreASMCompiler.class, type + " requested for node(" + node.getGrammarRule() + ", " + node.getPluginName() + ")");
		CompilerPlugin cp = pluginLoader.getPlugin(node.getPluginName());
		
		//----------------------------------------
		//---------Handle Operator Calls----------
		//----------------------------------------
		CodeFragment result = handleOperatorCall(node);
		if(result != null) return result;
		
		
		//test, if the node is a function call
		if(node.getGrammarClass().equals("FunctionRule") && node.getGrammarRule().equals("FunctionRuleTerm") && type == CodeType.R && node.getPluginName().equals("Kernel")){
			getLogger().debug(CoreASMCompiler.class, "Function call detected - checking for Function Plugin");
			//get the function name
			String functionname = node.getAbstractChildNodes().get(0).getToken();
			CompilerFunctionPlugin cfp = functionMapping.get(functionname);
			//if a function is registered for this identifier, compile it; otherwise use the generic code
			if(cfp != null){
				getLogger().debug(CoreASMCompiler.class, "Function Plugin found");
				return cfp.compileFunctionCall(node);
			}
			else{
				getLogger().debug(CoreASMCompiler.class, "No Function Plugin found for '" + functionname + "' using default case");
			}
		}
		
		if(cp == null) throw new CompilerException("no plugin available - perhaps an unregistered operator?");
		
		//compile code
		if(cp instanceof CompilerCodePlugin){
			CompilerCodePlugin resp = (CompilerCodePlugin) cp;
			
			CodeFragment coderes = null;
			try{
				coderes = resp.compile(type, node);
			}
			catch(CompilerException e){
				//try to build information about the node
				if(!e.isEvaluated()){
					this.addError(CompilationErrorHelper.makeErrorMessage(node, (ControlAPI) coreasm, e.getMessage(), cp.getClass().getName()));
					throw new CompilerException(e, true);
				}
				throw e;
			}
			catch(Exception e){
				this.addError(CompilationErrorHelper.makeErrorMessage(node, (ControlAPI) coreasm, e.getMessage(), cp.getClass().getName()));
				throw new CompilerException(e, true);
			}
			
			
			//note: code might get too large for java limits (65535 bytes for methods)
			//check here
			
			//System.out.println("------------------generated code:");
			//System.out.println(coderes);
			
			if(coderes.getByteCount() > 40000){
				
				this.addWarning("warning: compiled code turned to large, splitting it up");
				
				coderes = CodeWrapperEntry.buildWrapper(coderes, "coreasmcompiler " + resp.getClass().toString(), this);
			}
			
			//System.out.println("------------------generated wrapper:");
			//System.out.println(coderes);
			
			return coderes;
		}
		else{
			//not compilable
			this.addError("plugin " + cp.getName() + " does not register any code handlers");
			throw new CompilerException("plugin " + cp.getName()  + " does not register any code handlers");
		}
	}
	
	private CodeFragment handleOperatorCall(ASTNode node) throws CompilerException{
		if(node.getGrammarClass().equals("BinaryOperator")){
			//first, check if the optimization for values has a result for us
			Information inf = preprocessor.getNodeInformation(node).get("value");
			try{
				String val = (String) inf.getInformation("code").getValue();
				getLogger().debug(CoreASMCompiler.class, "optimization point found");
				getLogger().debug(CoreASMCompiler.class, "replacing operator node with '" + val + "'");
				return new CodeFragment("evalStack.push(" + val + ");\n");
			}
			catch(NullPointerException e){
				//do nothing, unfortunately we have no value stored at the node
			}
			
			CodeFragment result = new CodeFragment("");
			result.appendFragment(compile(node.getAbstractChildNodes().get(0), CodeType.R));
			result.appendFragment(compile(node.getAbstractChildNodes().get(1), CodeType.R));
			
			result.appendLine("@decl(CompilerRuntime.Element, rhs)=(CompilerRuntime.Element)evalStack.pop();\n");
			result.appendLine("@decl(CompilerRuntime.Element, lhs)=(CompilerRuntime.Element)evalStack.pop();\n");
			
			List<CompilerPlugin> tmp = binaryOperators.get(node.getToken());
			for(int i = 0; i < tmp.size(); i++){
				String s = ((CompilerOperatorPlugin)tmp.get(i)).compileBinaryOperator(node.getToken());
				result.appendLine(s);
			}
			
			result.appendLine("\nevalStack.push(CompilerRuntime.Element.UNDEF);\n");
			
			return result;
		}	
		else if(node.getGrammarClass().equals("UnaryOperator")){
			CodeFragment result = new CodeFragment("");
			result.appendFragment(compile(node.getAbstractChildNodes().get(0), CodeType.R));
			
			result.appendLine("@decl(CompilerRuntime.Element, lhs)=(CompilerRuntime.Element)evalStack.pop();\n");
			
			List<CompilerPlugin> tmp = unaryOperators.get(node.getToken());
			for(int i = 0; i < tmp.size(); i++){
				String s = ((CompilerOperatorPlugin)tmp.get(i)).compileUnaryOperator(node.getToken());
				result.appendLine(s);
			}
			
			result.appendLine("\nevalStack.push(CompilerRuntime.Element.UNDEF);\n");
			
			return result;
		}
		return null;
	}

	@Override
	public CompilerOptions getOptions() {
		return this.options;
	}

	@Override
	public StateMachineFile getMainFile() {
		return this.mainFile;
	}
	
	//----------------------start of helper functions for the actual compilation process------------------------
	
	private void loadSpecification(CompilerInformation info) throws CompilerException{
		lastTime = System.nanoTime();
		//create an engine and parse the specification
		Engine cae = null;
		//mute the coreasm engine
		
		PrintStream origOutput = null;
		PrintStream devnull = null;
		if(getOptions().hideCoreASMOutput){
			getLogger().warn(this.getClass(), "CoreASM output is hidden");
			origOutput = System.out;
			devnull = new PrintStream(new ByteArrayOutputStream());
			System.setOut(devnull);
		}
		
		if(coreasm != null){
			cae = (Engine) coreasm;
		}
		else{
			cae = (Engine)CoreASMEngineFactory.createEngine();
			cae.initialize();
		}
		
		cae.loadSpecification(options.SpecificationName.getAbsolutePath());
		//wait until parsing has finished
		cae.waitWhileBusy();
		if(cae.hasErrorOccurred()){
			cae.terminate();
			while(cae.isBusy()){};
			this.addError("CoreASM Parser could not parse the specification, check your syntax");
			throw new CompilerException("could not load specification");
		}
		cae.terminate();
		

		if(getOptions().hideCoreASMOutput){
			System.setOut(origOutput);
			devnull.close();
		}
		
		cTime = System.nanoTime();
		addTiming("Load and parse");
		
		getLogger().debug(CoreASMCompiler.class, "Parsing finished");
		getLogger().debug(CoreASMCompiler.class, "Loading plugins");
		
		lastTime = System.nanoTime();
		try{
			pluginLoader.loadPlugins(cae);
			System.out.println("Plugins loaded");
		}
		catch(NotCompilableException nce){
			//nce.printStackTrace();
			//System.out.println("error: " + nce.getMessage());
			throw new CompilerException(nce);
		}
		catch(Throwable t){
			System.out.println("throwable: " + t.getMessage());
		}
		cTime = System.nanoTime();
		addTiming("Plugin loading");
		//store the root node
		info.root = (ASTNode) cae.getSpec().getRootNode();
	}
	
	private void preprocessSpecification(CompilerInformation info) throws CompilerException{		
		try{
			preprocessor.loadPlugins(pluginLoader.getPreprocessorPlugins());
			preprocessor.preprocessSpecification(info.root);
		}
		catch(Exception e){
			//e.printStackTrace();
			addError("preprocessor had errors: " + e.getMessage());
			throw new CompilerException(e);
		}
	}
	
	private void applyFirstPlugins(CompilerInformation info) throws CompilerException {
		//path plugin´
		List<CompilerPathPlugin> pathplugins = pluginLoader.getCompilerPathPlugins();
		if(pathplugins.size() > 1){
			this.addError("Only one path configurator can be active");
			throw new CompilerException("Only one path configurator can be active");
		}
		else if(!pathplugins.isEmpty()){
			this.paths = pathplugins.get(0).getPathConfig();
			getLogger().debug(CoreASMCompiler.class, "loaded path config from plugin " + pathplugins.get(0).getName());
		}
		setGlobalMakros();
		
		
		//operator plugins
		lastTime = System.nanoTime();
		getLogger().debug(CoreASMCompiler.class, "loading operators");
		List<CompilerOperatorPlugin> ops = pluginLoader.getOperatorPlugins();
		for(CompilerOperatorPlugin cop : ops){
			getLogger().debug(CoreASMCompiler.class, "loading operators of plugin " + cop.getName());
			for(String s : cop.unaryOperations()){
				if(unaryOperators.get(s) == null) unaryOperators.put(s, new ArrayList<CompilerPlugin>());
				unaryOperators.get(s).add(cop);
				getLogger().debug(CoreASMCompiler.class, "loaded unary Operator " + s);
			}
			for(String s : cop.binaryOperations()){
				if(binaryOperators.get(s) == null) binaryOperators.put(s, new ArrayList<CompilerPlugin>());
				binaryOperators.get(s).add(cop);
				getLogger().debug(CoreASMCompiler.class, "loaded binary Operator " + s);
			}
		}
		cTime = System.nanoTime();
		addTiming("Operator loading");
		
		getLogger().debug(CoreASMCompiler.class, "loading additional functions");
		lastTime = System.nanoTime();
		//function plugins
		for(CompilerFunctionPlugin cfp : pluginLoader.getFunctionPlugins()){
			for(String s : cfp.getCompileFunctionNames()){
				functionMapping.put(s, cfp);
			}
		}
		cTime = System.nanoTime();
		addTiming("Function plugins");
		//compiler plugins
		
		lastTime = System.nanoTime();
		for(CompilerCodePlugin ccp : pluginLoader.getCompilerCodePlugins()){
			ccp.registerCodeHandlers();
		}
		cTime = System.nanoTime();
		addTiming("Code Handlers");
	}
	
	private void applyPlugins(CompilerInformation info) throws CompilerException{
		//init Plugins
		lastTime = System.nanoTime();
		mainFile.processInitCodePlugins(pluginLoader.getInitCodePlugins());
		cTime = System.nanoTime();
		addTiming("Init Code plugins");
		
		//extension point plugins
		lastTime = System.nanoTime();
		mainFile.processExtensionPlugins(pluginLoader.getExtensionPointPlugins());
		cTime = System.nanoTime();
		addTiming("Extension Plugins");
		
		//vocabulary extenders plugins
		lastTime = System.nanoTime();
		mainFile.processVocabularyExtenderPlugins(pluginLoader.getVocabularyExtenderPlugins());
		cTime = System.nanoTime();
		addTiming("Vocabulary Extender Plugins");
	}
	
	private void compileSpecification(CompilerInformation info) throws CompilerException{
		lastTime = System.nanoTime();
		getLogger().debug(CoreASMCompiler.class, "creating temporary directory");
		File tempDir = options.tempDirectory;
		if(tempDir.exists()){
			getLogger().warn(CoreASMCompiler.class, "temp directory already exists");
			if(tempDir.list().length > 0 && !options.removeExistingFiles){
				getLogger().error(CoreASMCompiler.class, "temp directory is not empty");
				this.addError("temporary directory is not empty. Use -removeExistingFiles true to purge the temporary directory before the run");
				throw new CompilerException(new DirectoryNotEmptyException(""));
			}
			else if(tempDir.list().length > 0){
				getLogger().debug(CoreASMCompiler.class, "temp directory is not empty, purging existing files");
				purgeTempDir();
				if(!tempDir.exists()) tempDir.mkdir();
			}
		}
		else{
			tempDir.mkdir();
		}
		cTime = System.nanoTime();
		addTiming("Directory preparation");

		lastTime = System.nanoTime();
		varManager.startContext();
		
		//errors in here have to be made public separately
		compile(info.root, CodeType.BASIC);
		
		try {
			varManager.endContext();
		} catch (EmptyContextStackException e1) {
			this.addError("final variable context already ended - check plugin code");
			throw new CompilerException(e1);
		}
		cTime = System.nanoTime();
		addTiming("Compilation");
	}
	
	private void compileSources(CompilerInformation info) throws CompilerException{
		getLogger().debug(CoreASMCompiler.class, "code generation complete, dumping source files to " + options.tempDirectory);
		
		lastTime = System.nanoTime();
		
		//find backend providers
		List<CompilerBackendProvider> backend = pluginLoader.getCompilerBackendProviders();
		CompilerFileWriter fileWriter = null;
		CompilerPacker filePacker = null;
		for(CompilerBackendProvider cbp : backend){
			CompilerFileWriter tmpWriter = cbp.getFileWriter();
			CompilerPacker tmpPacker = cbp.getPacker();
			
			if(fileWriter == null) fileWriter = tmpWriter;
			else if(fileWriter != null && fileWriter != null) {
				addError("Only one file writer can be active at a time");
				throw new CompilerException("Only one file writer can be active at a time");
			}
			
			if(tmpPacker == null) filePacker = tmpPacker;
			else if(tmpPacker != null && filePacker != null) {
				addError("Only one file packer can be active at a time");
				throw new CompilerException("Only one file packer can be active at a time");
			}
		}
		
		//construct default
		KernelBackend back = new KernelBackend();
		if(fileWriter == null) fileWriter = (CompilerFileWriter) back;
		if(filePacker == null) filePacker = (CompilerPacker) back;
		
		//dump class library
		List<LibraryEntry> entries = classLibrary.buildLibrary();
		
		//dump files
		List<File> files = fileWriter.writeEntriesToDisk(entries, this);
		
		//compile TODO: perhaps include some configurability here?
		if(!options.noCompile){
			getLogger().debug(CoreASMCompiler.class, "class dump complete");
			
			getLogger().debug(CoreASMCompiler.class, "starting java compiler");
			
			lastTime = System.nanoTime();
			JavaCompilerWrapper.compile(options, files, this);
			cTime = System.nanoTime();
			addTiming("Javac");
			
			getLogger().debug(CoreASMCompiler.class, "java compilation successfull");
			
			//pack files
			getLogger().debug(CoreASMCompiler.class, "packing jar archive");
			
			lastTime = System.nanoTime();
			filePacker.packFiles(files, this);
			cTime = System.nanoTime();
			addTiming("Jar packing");
			
			getLogger().debug(CoreASMCompiler.class, "packing successfull");
			
			getLogger().debug(CoreASMCompiler.class, "compilation operation successfull");
		}
		else{
			getLogger().debug(CoreASMCompiler.class, "Compilation is disabled - stopping compiler");
		}
	}
	
	private void buildMain(CompilerInformation info) throws CompilerException{
		try {
			classLibrary.addEntry(mainFile);
			
			//find main entry point from plugins
			LibraryEntry mc = null;
			List<CompilerMainClassProvider> providers = pluginLoader.getCompilerMainClassProviders();
			if(providers.size() > 1) throw new CompilerException("cannot have more than one program entry point");
			else if(providers.size() < 1) mc = new MainClass(this);
			else mc = providers.get(0).getMainClass();
			
			classLibrary.addEntry(mc);
			
		} catch (EntryAlreadyExistsException e) {
			this.addError("Could not add main file to library, the entry already exists");
			throw new CompilerException(e);
		}
	}

	@Override
	public void addError(String msg) {
		if(!this.tryCompiling){
			if(!errors.contains(msg)) errors.add(msg);
		}
	}

	@Override
	public void addWarning(String msg) {
		if(!this.tryCompiling){
			if(!warnings.contains(msg)) warnings.add(msg);
		}
	}

	@Override
	public List<String> getErrors() {
		return java.util.Collections.unmodifiableList(errors);
	}

	@Override
	public List<String> getWarnings() {
		return java.util.Collections.unmodifiableList(warnings);
	}

	@Override
	public LoggingHelper getLogger() {
		return logging;
	}

	@Override
	public CompilerPathConfig getPath() {
		return paths;
	}
	
	@Override
	public Map<String, String> getGlobalMakros(){
		return Collections.unmodifiableMap(globalMakros);
	}
}
