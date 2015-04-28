package org.coreasm.compiler.plugins.kernel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.components.classlibrary.ClassLibrary;
import org.coreasm.compiler.components.classlibrary.JarInclude;
import org.coreasm.compiler.components.classlibrary.JarIncludeHelper;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.components.mainprogram.EntryType;
import org.coreasm.compiler.components.mainprogram.MainFileEntry;
import org.coreasm.compiler.components.preprocessor.InheritRule;
import org.coreasm.compiler.components.preprocessor.SynthesizeRule;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerPreprocessorPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
import org.coreasm.compiler.plugins.kernel.code.bcode.KernelCoreHandler;
import org.coreasm.compiler.plugins.kernel.code.bcode.KernelInitHandler;
import org.coreasm.compiler.plugins.kernel.code.bcode.KernelRuleDeclarationHandler;
import org.coreasm.compiler.plugins.kernel.code.lcode.KernelFunctionRuleTermHandler;
import org.coreasm.compiler.plugins.kernel.code.lcode.KernelIDCodeHandler;
import org.coreasm.compiler.plugins.kernel.code.lrcode.KernelLRFunctionRuleTermHandler;
import org.coreasm.compiler.plugins.kernel.code.rcode.KernelBooleanTermHandler;
import org.coreasm.compiler.plugins.kernel.code.rcode.KernelExpressionLiftHandler;
import org.coreasm.compiler.plugins.kernel.code.rcode.KernelFunctionRuleExpressionHandler;
import org.coreasm.compiler.plugins.kernel.code.rcode.KernelRuleOrFuncHandler;
import org.coreasm.compiler.plugins.kernel.code.rcode.KernelSelfHandler;
import org.coreasm.compiler.plugins.kernel.code.rcode.KernelUndefHandler;
import org.coreasm.compiler.plugins.kernel.code.ucode.KernelImportRule;
import org.coreasm.compiler.plugins.kernel.code.ucode.KernelMacroCallRule;
import org.coreasm.compiler.plugins.kernel.code.ucode.KernelSkipRule;
import org.coreasm.compiler.plugins.kernel.code.ucode.KernelUpdateRule;
import org.coreasm.compiler.plugins.kernel.preprocessor.IDSpawner;
import org.coreasm.compiler.plugins.kernel.preprocessor.RuleParamInheritRule;
import org.coreasm.compiler.plugins.kernel.preprocessor.SignatureTransformer;
import org.coreasm.engine.absstorage.BooleanBackgroundElement;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.FunctionBackgroundElement;
import org.coreasm.engine.absstorage.RuleBackgroundElement;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugin.Plugin;

/**
 * Provides core functionality to the compiler.
 * The kernel plugin aims to behave as a normal plugin.
 * One has to keep in mind though, that it still has a special
 * role in the architecture.
 * @author Spellmaker
 *
 */
public class CompilerKernelPlugin extends CompilerCodePlugin implements
		CompilerPlugin, CompilerVocabularyExtender, CompilerOperatorPlugin, CompilerPreprocessorPlugin {

	private Plugin interpreterPlugin;

	/**
	 * Constructs a new plugin
	 * @param parent The interpreter version
	 */
	public CompilerKernelPlugin(Plugin parent) {
		this.interpreterPlugin = parent;
	}

	@Override
	public Plugin getInterpreterPlugin() {
		return interpreterPlugin;
	}

	private void addAbsReplacement(String name) {
		engine.getClassLibrary()
				.addPackageReplacement("org.coreasm.engine.absstorage." + name,
						engine.getPath().runtimePkg() + "." + name);
	}

	public void registerCodeHandlers() throws CompilerException {
		this.register(new KernelIDCodeHandler(), CodeType.L, null, "ID", null);
		this.register(new KernelFunctionRuleTermHandler(), CodeType.L,
				"FunctionRule", "FunctionRuleTerm", null);

		this.register(new KernelUndefHandler(), CodeType.R, null,
				"KernelTerms", "undef");
		this.register(new KernelSelfHandler(), CodeType.R, null, "KernelTerms",
				"self");
		this.register(new KernelBooleanTermHandler(), CodeType.R, null,
				"BooleanTerm", null);
		this.register(new KernelFunctionRuleExpressionHandler(), CodeType.R,
				null, "FunctionRuleTerm", null);
		this.register(new KernelExpressionLiftHandler(), CodeType.R,
				"Expression", "", null);
		this.register(new KernelRuleOrFuncHandler(), CodeType.R, "Expression",
				"RuleOrFunctionElementTerm", null);

		this.register(new KernelSkipRule(), CodeType.U, "Rule", "SkipRule",
				null);
		this.register(new KernelUpdateRule(), CodeType.U, "Rule", "UpdateRule",
				null);
		this.register(new KernelImportRule(), CodeType.U, "Rule", "ImportRule",
				null);
		this.register(new KernelMacroCallRule(), CodeType.U, "Rule",
				"MacroCallRule", null);

		this.register(new KernelCoreHandler(), CodeType.BASIC, null, "CoreASM",
				null);
		this.register(new KernelInitHandler(), CodeType.BASIC, null,
				"Initialization", null);
		this.register(new KernelRuleDeclarationHandler(), CodeType.BASIC, null,
				"RuleDeclaration", null);

		this.register(new KernelLRFunctionRuleTermHandler(), CodeType.LR,
				"FunctionRule", "FunctionRuleTerm", null);
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {
		// load runtime classes
		List<MainFileEntry> loadedClasses = new ArrayList<MainFileEntry>();

		File enginePath = engine.getOptions().enginePath;
		// if no jar archive is set for the engine, simply copy files from the
		// runtime directory
		if (enginePath == null) {
			engine.getLogger().error(getClass(), "Loading the runtime from a directory is currently not supported");
			throw new CompilerException("could not load compiler runtime");
		} else {
			// otherwise the runtime is contained in the jar archive
			JarFile jar = null;
			try {
				jar = new JarFile(enginePath);
			} catch (IOException e) {
				e.printStackTrace();
			}

			Enumeration<JarEntry> entries = jar.entries();

			// list of entries not to include from the runtime directory
			classLibrary.addPackageReplacement("org.coreasm.engine.absstorage", engine.getPath().runtimePkg());
			classLibrary.addPackageReplacement("org.coreasm.engine.CoreASMError", engine.getPath().runtimePkg() + ".CoreASMError");
			classLibrary.addPackageReplacement("org.coreasm.engine.ControlAPI", engine.getPath().runtimePkg() + ".ControlAPI");
			classLibrary.addPackageReplacement("org.coreasm.engine.interpreter.InitAgent", engine.getPath().runtimePkg() + ".InitAgent");
			classLibrary.addPackageReplacement(
					"org.coreasm.engine.interpreter.Node",
					engine.getPath().runtimePkg() + ".Node");
			classLibrary.addPackageReplacement(
					"org.coreasm.engine.interpreter.ScannerInfo",
					engine.getPath().runtimePkg() + ".ScannerInfo");
			classLibrary.addPackageReplacement(
					"org.coreasm.engine.scheduler.SchedulingPolicy",
					engine.getPath().runtimePkg() + ".SchedulingPolicy");
			classLibrary.addPackageReplacement(
					"org.coreasm.engine.EngineError",
					engine.getPath().runtimePkg() + ".EngineError");
			classLibrary.addPackageReplacement(
					"org.coreasm.engine.EngineException",
					engine.getPath().runtimePkg() + ".EngineException");
			classLibrary.addPackageReplacement("org.coreasm.util.Tools",
					engine.getPath().runtimePkg() + ".Tools");
			classLibrary.addPackageReplacement("org.slf4j.Logger",
					"java.util.ArrayList");
			classLibrary.addPackageReplacement("org.slf4j.LoggerFactory",
					"java.util.HashMap");

			addAbsReplacement("AbstractUniverse");
			addAbsReplacement("BackgroundElement");
			addAbsReplacement("BooleanBackgroundElement");
			addAbsReplacement("BooleanElement");
			addAbsReplacement("Element");
			addAbsReplacement("ElementBackgroundElement");
			addAbsReplacement("Enumerable");
			addAbsReplacement("FunctionBackgroundElement");
			addAbsReplacement("FunctionElement");
			addAbsReplacement("Location");
			addAbsReplacement("MapFunction");
			addAbsReplacement("NameElement");
			addAbsReplacement("Signature");
			addAbsReplacement("UniverseElement");
			addAbsReplacement("Update");
			addAbsReplacement("ElementFormatException");
			addAbsReplacement("ElementList");
			addAbsReplacement("IdentifierNotFoundException");
			addAbsReplacement("InvalidLocationException");
			addAbsReplacement("NameConflictException");
			addAbsReplacement("UnmodifiableFunctionException");

			try {
				//Manually add CompilerRuntime entries from the coreasm interpreter source
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/interpreter/InitAgent.java", 
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/AbstractUniverse.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/BackgroundElement.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
								"org/coreasm/engine/absstorage/BooleanBackgroundElement.java",
								"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/BooleanElement.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/Element.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
								"org/coreasm/engine/absstorage/ElementBackgroundElement.java",
								"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/Enumerable.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
								"org/coreasm/engine/absstorage/FunctionBackgroundElement.java",
								"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/FunctionElement.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/Location.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/MapFunction.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/NameElement.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/Signature.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/UniverseElement.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/Update.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
								"org/coreasm/engine/absstorage/ElementFormatException.java",
								"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/EngineError.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/EngineException.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
								"org/coreasm/engine/absstorage/IdentifierNotFoundException.java",
								"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
								"org/coreasm/engine/absstorage/InvalidLocationException.java",
								"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
								"org/coreasm/engine/absstorage/NameConflictException.java",
								"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
								"org/coreasm/engine/absstorage/UnmodifiableFunctionException.java",
								"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/absstorage/ElementList.java",
						"Kernel", LibraryEntryType.RUNTIME));
				classLibrary.addEntry(new JarInclude(engine, enginePath, 
						"org/coreasm/engine/scheduler/SchedulingPolicy.java",
						"Kernel", LibraryEntryType.RUNTIME));
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException("could not load classes "
						+ e.getMessage());
			}
			//add all remaining runtime entries in the CompilerRuntime directory
			for (JarEntry jarEntry = entries.nextElement(); entries
					.hasMoreElements(); jarEntry = entries.nextElement()) {
				String name = jarEntry.getName();
				if (name.startsWith("CompilerRuntime/")
						&& name.endsWith(".java")) {
					try {
						classLibrary.addEntry(new JarInclude(engine, enginePath, 
								jarEntry.getName(), "Kernel", LibraryEntryType.RUNTIME));
					} catch (EntryAlreadyExistsException e) {
						engine.getLogger()
								.error(CompilerKernelPlugin.class,
										"kernel should not have collisions with itself");
						e.printStackTrace();
					} 
				}
			}

			// TODO: fix these includes
			try {
				loadedClasses = (new JarIncludeHelper(engine, this)).
						includeStatic("org/coreasm/compiler/plugins/kernel/include/KernelAggregator.java", EntryType.AGGREGATOR, "kernelaggregator").
						includeStatic("org/coreasm/compiler/plugins/kernel/include/DefaultSchedulingPolicy.java", EntryType.SCHEDULER, "scheduler").
						build();
			} catch (EntryAlreadyExistsException e) {
				e.printStackTrace();
			} 
			try {
				jar.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// universes
		// the agents universe is loaded by default
		// loadedClasses.add(new
		// MainFileEntry(classLibrary.findEntry("CompilerRuntime.UniverseElement"),
		// EntryType.UNIVERSE, AbstractStorage.AGENTS_UNIVERSE_NAME));

		// backgrounds
		loadedClasses.add(new MainFileEntry(classLibrary
				.findEntry("BooleanBackgroundElement", null, LibraryEntryType.RUNTIME), 
				EntryType.BACKGROUND,
				BooleanBackgroundElement.BOOLEAN_BACKGROUND_NAME));
		loadedClasses.add(new MainFileEntry(classLibrary
				.findEntry("FunctionBackgroundElement", null, LibraryEntryType.RUNTIME),
				EntryType.BACKGROUND,
				FunctionBackgroundElement.FUNCTION_BACKGROUND_NAME));
		loadedClasses.add(new MainFileEntry(classLibrary
				.findEntry("ElementBackgroundElement", null, LibraryEntryType.RUNTIME),
				EntryType.BACKGROUND,
				ElementBackgroundElement.ELEMENT_BACKGROUND_NAME));
		loadedClasses.add(new MainFileEntry(classLibrary
				.findEntry("RuleBackgroundElement", null, LibraryEntryType.RUNTIME),
				EntryType.BACKGROUND,
				RuleBackgroundElement.RULE_BACKGROUND_NAME));

		// functions
		// loadedClasses.add(new
		// MainFileEntry(classLibrary.findEntry("CompilerRuntime.ProgramFunction"),
		// EntryType.FUNCTION, AbstractStorage.PROGRAM_FUNCTION_NAME));
		/*
		 * try { } catch (EntryAlreadyExistsException e) { throw new
		 * CompilerException(e); }
		 */
		return loadedClasses;
	}

	@Override
	public List<String> unaryOperations() {
		return new ArrayList<String>();
	}

	@Override
	public List<String> binaryOperations() {
		List<String> result = new ArrayList<String>();
		result.add("=");
		return result;
	}

	@Override
	public String compileBinaryOperator(String token) {
		String result = "if(true){\nevalStack.push(@RuntimePkg@.BooleanElement.valueOf(@lhs@.equals(@rhs@)));\n}\n";
		result = result + "else ";
		return result;
	}

	@Override
	public String compileUnaryOperator(String token) {
		return null;
	}

	@Override
	public List<SynthesizeRule> getSynthesizeRules() {
		List<SynthesizeRule> result = new ArrayList<SynthesizeRule>();

		result.add(new SignatureTransformer());
		result.add(new IDSpawner());

		return result;
	}

	@Override
	public Map<String, SynthesizeRule> getSynthDefaultBehaviours() {
		Map<String, SynthesizeRule> result = new HashMap<String, SynthesizeRule>();

		result.put("ID", new IDSpawner());

		return result;
	}

	@Override
	public List<InheritRule> getInheritRules() {
		List<InheritRule> result = new ArrayList<InheritRule>();
		result.add(new RuleParamInheritRule());
		return result;
	}

	@Override
	public Map<String, InheritRule> getInheritDefaultBehaviours() {
		Map<String, InheritRule> result = new HashMap<String, InheritRule>();
		result.put("RuleParameter", new RuleParamInheritRule());
		return result;
	}

	@Override
	public String getName() {
		return Kernel.PLUGIN_NAME;
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}
}
