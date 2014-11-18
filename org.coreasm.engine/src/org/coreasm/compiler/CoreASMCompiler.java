package org.coreasm.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.DirectoryNotEmptyException;
import org.coreasm.compiler.exception.EmptyContextStackException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.LibraryEntryException;
import org.coreasm.compiler.exception.NotCompilableException;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.mainprogram.MainFile;
import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.Preprocessor;
import org.coreasm.compiler.variablemanager.VarManager;
import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngineFactory;
import org.coreasm.engine.Engine;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Implementation of the CompilerEngine interface and the actual Compiler.
 * Provides services for plugins and directs the compilation process.
 * @author Markus Brenner
 *
 */

public class CoreASMCompiler implements CompilerEngine {
	private static CompilerEngine engine;
	
	private LoggingHelper logging;
	
	private CompilerOptions options;
	private PluginLoader pluginLoader;
	private ClassLibrary classLibrary;
	private VarManager varManager;
	private MainFile mainFile;
	private Map<String, List<CompilerPlugin>> unaryOperators;
	private Map<String, List<CompilerPlugin>> binaryOperators;
	private Map<String, CompilerFunctionPlugin> functionMapping;
	private Preprocessor preprocessor;
	
	private List<String> warnings;
	private List<String> errors;
	
	private CoreASMEngine coreasm;
	
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
		engine = this;
		
		//initialize components
		this.options = options;
		pluginLoader = new DummyLoader();
		classLibrary = new ClassLibrary(options);
		varManager = new VarManager();
		mainFile = new MainFile();
		preprocessor = new Preprocessor();
		
		logging = new LoggingHelper();
		
		//initialize data structures
		unaryOperators = new HashMap<String, List<CompilerPlugin>>();
		binaryOperators = new HashMap<String, List<CompilerPlugin>>();
		functionMapping = new HashMap<String, CompilerFunctionPlugin>();
		
		warnings = new ArrayList<String>();
		errors = new ArrayList<String>();
		
		coreasm = casm;
	}

	/**
	 * Starts the compilation process for the CoreASM specification
	 * provided by the options Object.
	 * @throws CompilerException If an error occured during the compilation process
	 */
	public void compile() throws CompilerException{
		try{	
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "starting compiler");
			CompilerInformation info = new CompilerInformation();
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "loading specification");
			loadSpecification(info);
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "preprocessing specification");
			preprocessSpecification(info);
			//load plugins, which have to be loaded first
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "loading first set of plugins");
			applyFirstPlugins(info);
			//compile specification first, so that plugins may
			//provide objects based on the parse tree
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "compiling specification");
			compileSpecification(info);
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "applying second set of plugins");
			applyPlugins(info);
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "building main file");
			//note: most operations will actually happen in the next step
			buildMain(info);
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "compiling java sources");
			compileSources(info);
		}
		catch(CompilerException ce){
			throw ce;
		}
		catch(Exception e){
			System.out.println("uncaught exception:"  + e.getMessage());
		}
		finally{
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
		CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "cleaning up temp directory");
		
		purgeDir(new File(options.tempDirectory));
		
		CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "cleanup finished");
	}
	
	private void purgeDir(File f){
		if(f.exists()){
			if(f.isFile()){
				if(!f.delete()) CoreASMCompiler.getEngine().getLogger().warn(CoreASMCompiler.class, "Could not delete file " + f.getAbsolutePath());
			}
			else{
				for(String s : f.list()){
					purgeDir(new File(f.getAbsolutePath() + "\\" + s));
				}
				if(!f.delete()) CoreASMCompiler.getEngine().getLogger().warn(CoreASMCompiler.class, "Could not delete directory " + f.getAbsolutePath());
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
	public CodeFragment compile(ASTNode node, CodeType type) throws CompilerException {
		CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, type + " requested for node(" + node.getGrammarRule() + ", " + node.getPluginName() + ")");
		CompilerPlugin cp = pluginLoader.getPlugin(node.getPluginName());
		
		//----------------------------------------
		//---------Handle Operator Calls----------
		//----------------------------------------
		CodeFragment result = handleOperatorCall(node);
		if(result != null) return result;
		
		
		//test, if the node is a function call
		if(node.getGrammarClass().equals("FunctionRule") && node.getGrammarRule().equals("FunctionRuleTerm") && type == CodeType.R && node.getPluginName().equals("Kernel")){
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "Function call detected - checking for Function Plugin");
			//get the function name
			String functionname = node.getAbstractChildNodes().get(0).getToken();
			CompilerFunctionPlugin cfp = functionMapping.get(functionname);
			//if a function is registered for this identifier, compile it; otherwise use the generic code
			if(cfp != null){
				CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "Function Plugin found");
				return cfp.compileFunctionCall(node);
			}
			else{
				CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "No Function Plugin found for '" + functionname + "' using default case");
			}
		}
		
		if(cp == null) throw new CompilerException("no plugin available - perhaps an unregistered operator?");
		
		//compile code
		if(cp instanceof CompilerCodePlugin){
			CompilerCodePlugin resp = (CompilerCodePlugin) cp;
			return resp.compile(type, node);			
		}
		else{
			//not compilable
			this.addError("plugin " + cp.getName() + " does not register any code handlers");
			throw new CompilerException("plugin " + cp.getName()  + " does not register any code handlers");
		}
		
		/*switch(type){
			case BASIC:
				if(!(cp instanceof CompilerCodeBPlugin)){
					this.addError("plugin " + cp.getName() + " does not compile bCode");
					throw new CompilerException("plugin " + cp.getName() + " does not compile bCode");
				}
				((CompilerCodeBPlugin) cp).bCode(node);
				return null;
			case R:	
				if(!(cp instanceof CompilerCodeRPlugin)){
					this.addError("plugin " + cp.getName() + " does not compile rCode");
					throw new CompilerException("plugin " + cp.getName() + " does not compile rCode");
				}
				return ((CompilerCodeRPlugin) cp).rCode(node);
			case U:	
				if(!(cp instanceof CompilerCodeUPlugin)){
					this.addError("plugin " + cp.getName() + " does not compile uCode");
					throw new CompilerException("plugin " + cp.getName() + " does not compile uCode");
				}
				return ((CompilerCodeUPlugin) cp).uCode(node);
			case L:	
				if(!(cp instanceof CompilerCodeLPlugin)){
					this.addError("plugin " + cp.getName() + " does not compile lCode");
					throw new CompilerException("plugin " + cp.getName() + " does not compile lCode");
				}
				return ((CompilerCodeLPlugin) cp).lCode(node);
			case LU:	
				if(!(cp instanceof CompilerCodeLUPlugin)){
					this.addError("plugin " + cp.getName() + " does not compile luCode");
					throw new CompilerException("plugin " + cp.getName() + " does not compile luCode");
				}
				return ((CompilerCodeLUPlugin) cp).luCode(node);
			case UR:	
				if(!(cp instanceof CompilerCodeURPlugin)){
					this.addError("plugin " + cp.getName() + " does not compile urCode");
					throw new CompilerException("plugin " + cp.getName() + " does not compile urCode");
				}
				return ((CompilerCodeURPlugin) cp).urCode(node);
			case LR:	
				if(!(cp instanceof CompilerCodeLRPlugin)){
					this.addError("plugin " + cp.getName() + " does not compile lrCode");
					throw new CompilerException("plugin " + cp.getName() + " does not compile lrCode");
				}
				return ((CompilerCodeLRPlugin) cp).lrCode(node);
			case LUR:	
				if(!(cp instanceof CompilerCodeLURPlugin)){
					this.addError("plugin " + cp.getName() + " does not compile lurCode");
					throw new CompilerException("plugin " + cp.getName() + " does not compile lurCode");
				}
				return ((CompilerCodeLURPlugin) cp).lurCode(node);
				
			default: 			CoreASMCompiler.getEngine().getLogger().error(CoreASMCompiler.class, "Unknown compile type requested"); 
								throw new CompilerException("Unknown compile type: " + type);
		}*/
	}
	
	private CodeFragment handleOperatorCall(ASTNode node) throws CompilerException{
		if(node.getGrammarClass().equals("BinaryOperator")){
			//first, check if the optimization for values has a result for us
			Information inf = preprocessor.getNodeInformation(node).get("value");
			try{
				String val = (String) inf.getInformation("code").getValue();
				CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "optimization point found");
				CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "replacing operator node with '" + val + "'");
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
	public MainFile getMainFile() {
		return this.mainFile;
	}
	
	//----------------------start of helper functions for the actual compilation process------------------------
	
	private void loadSpecification(CompilerInformation info) throws CompilerException{
		//create an engine and parse the specification
		Engine cae = null;
		if(coreasm != null){
			cae = (Engine) coreasm;
		}
		else{
			cae = (Engine)CoreASMEngineFactory.createEngine();
			cae.initialize();
		}
		
		cae.loadSpecification(options.SpecificationName);
		//wait until parsing has finished
		cae.waitWhileBusy();
		if(cae.hasErrorOccurred()){
			cae.terminate();
			while(cae.isBusy()){};
			this.addError("CoreASM Parser could not parse the specification, check your syntax");
			throw new CompilerException("could not load specification");
		}
		cae.terminate();
		CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "Parsing finished");
		CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "Loading plugins");
		try{
			pluginLoader.loadPlugins(cae);
			System.out.println("Plugins loaded");
		}
		catch(NotCompilableException nce){
			nce.printStackTrace();
			System.out.println("error: " + nce.getMessage());
			throw new CompilerException(nce);
		}
		catch(Throwable t){
			System.out.println("throwable: " + t.getMessage());
		}
		//store the root node
		info.root = (ASTNode) cae.getSpec().getRootNode();
	}
	
	private void preprocessSpecification(CompilerInformation info) throws CompilerException{		
		try{
			preprocessor.loadPlugins(pluginLoader.getPreprocessorPlugins());
			preprocessor.preprocessSpecification(info.root);
		}
		catch(Exception e){
			e.printStackTrace();
			CoreASMCompiler.getEngine().addError("preprocessor had errors: " + e.getMessage());
			throw new CompilerException(e);
		}
	}
	
	private void applyFirstPlugins(CompilerInformation info) throws CompilerException {
		//operator plugins
		CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "loading operators");
		List<CompilerOperatorPlugin> ops = pluginLoader.getOperatorPlugins();
		for(CompilerOperatorPlugin cop : ops){
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "loading operators of plugin " + cop.getName());
			for(String s : cop.unaryOperations()){
				if(unaryOperators.get(s) == null) unaryOperators.put(s, new ArrayList<CompilerPlugin>());
				unaryOperators.get(s).add(cop);
				CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "loaded unary Operator " + s);
			}
			for(String s : cop.binaryOperations()){
				if(binaryOperators.get(s) == null) binaryOperators.put(s, new ArrayList<CompilerPlugin>());
				binaryOperators.get(s).add(cop);
				CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "loaded binary Operator " + s);
			}
		}
		CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "loading additional functions");
		//function plugins
		for(CompilerFunctionPlugin cfp : pluginLoader.getFunctionPlugins()){
			for(String s : cfp.getCompileFunctionNames()){
				functionMapping.put(s, cfp);
			}
		}
		//compiler plugins
		for(CompilerCodePlugin ccp : pluginLoader.getCompilerCodePlugins()){
			ccp.registerCodeHandlers();
		}
	}
	
	private void applyPlugins(CompilerInformation info) throws CompilerException{
		//init Plugins
		mainFile.processInitCodePlugins(pluginLoader.getInitCodePlugins());
		//extension point plugins
		mainFile.processExtensionPlugins(pluginLoader.getExtensionPointPlugins());
		//vocabulary extenders plugins
		mainFile.processVocabularyExtenderPlugins(pluginLoader.getVocabularyExtenderPlugins());
	}
	
	private void compileSpecification(CompilerInformation info) throws CompilerException{
		CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "creating temporary directory");
		File tempDir = new File(options.tempDirectory);
		if(tempDir.exists()){
			CoreASMCompiler.getEngine().getLogger().warn(CoreASMCompiler.class, "temp directory already exists");
			if(tempDir.list().length > 0 && !options.removeExistingFiles){
				CoreASMCompiler.getEngine().getLogger().error(CoreASMCompiler.class, "temp directory is not empty");
				this.addError("temporary directory is not empty. Use -removeExistingFiles true to purge the temporary directory before the run");
				throw new CompilerException(new DirectoryNotEmptyException(""));
			}
			else if(tempDir.list().length > 0){
				CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "temp directory is not empty, purging existing files");
				purgeTempDir();
				if(!tempDir.exists()) tempDir.mkdir();
			}
		}
		else{
			tempDir.mkdir();
		}

		varManager.startContext();
		
		//errors in here have to be made public separately
		compile(info.root, CodeType.BASIC);
		
		try {
			varManager.endContext();
		} catch (EmptyContextStackException e1) {
			this.addError("final variable context already ended - check plugin code");
			throw new CompilerException(e1);
		}
	}
	
	private void compileSources(CompilerInformation info) throws CompilerException{
		CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "code generation complete, dumping source files to " + options.tempDirectory);
		ArrayList<String> classes = null;
		try {
			classes = classLibrary.dumpClasses();
		} catch (LibraryEntryException e) {
			throw new CompilerException(e);
		}
		
		if(!options.noCompile){
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "class dump complete");
			
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "starting java compiler");
			
			JavaCompilerWrapper.compile(options, classes);
			
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "java compilation successfull");
			
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "packing jar archive");
			
			JarPacker.packJar(options);
			
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "packing successfull");
			
			CoreASMCompiler.getEngine().getLogger().debug(CoreASMCompiler.class, "compilation operation successfull");
		}
	}
	
	private void buildMain(CompilerInformation info) throws CompilerException{
		try {
			classLibrary.addEntry(mainFile);
		} catch (EntryAlreadyExistsException e) {
			this.addError("Could not add main file to library, the entry already exists");
			throw new CompilerException(e);
		}
	}

	@Override
	public void addError(String msg) {
		if(!errors.contains(msg)) errors.add(msg);
	}

	@Override
	public void addWarning(String msg) {
		if(!warnings.contains(msg)) warnings.add(msg);
	}

	/**
	 * Helper methods for tests to inject mock Compiler Engines.
	 * @param e The new CompilerEngine
	 */
	public static void setEngine(CompilerEngine e){
		engine = e;
	}

	/**
	 * Static method to get a handle to the running CompilerEngine
	 * without having to pass it around as a parameter
	 * @return The running CompilerEngine
	 */
	public static CompilerEngine getEngine(){
		return engine;
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
}
