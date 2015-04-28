package org.coreasm.compiler.plugins.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.ClassLibrary;
import org.coreasm.compiler.components.classlibrary.JarIncludeHelper;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.components.mainprogram.EntryType;
import org.coreasm.compiler.components.mainprogram.MainFileEntry;
import org.coreasm.compiler.components.mainprogram.statemachine.EngineTransition;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.plugins.io.code.ucode.PrintRuleHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerExtensionPointPlugin;
import org.coreasm.compiler.interfaces.CompilerInitCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

/**
 * Provides IO methods.
 * The current version does not allow for a lot of customization in
 * terms of which channels are used.
 * Future versions should optimize this.
 * @author Spellmaker
 *
 */
public class CompilerIOPlugin extends CompilerCodePlugin implements CompilerPlugin, CompilerVocabularyExtender, CompilerExtensionPointPlugin, CompilerInitCodePlugin{

	private Plugin interpreterPlugin;
	/**
	 * Constructs a new plugin
	 * @param parent The interpreter version
	 */
	public CompilerIOPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	@Override
	public String getName() {
		return "IOPlugin";
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary) throws CompilerException {
		File enginePath = engine.getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePath == null){
			engine.getLogger().error(getClass(), "loading classes from a directory is currently not supported");
			throw new CompilerException("could not load classes");
		}
		else{			
			try {
				//classLibrary.addPackageReplacement("org.coreasm.engine.plugins.string.StringElement", "plugins.StringPlugin.StringElement");
				
				//add package replacements for classes accessible for other plugins
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.io.InputProvider", engine.getPath().getEntryName(LibraryEntryType.STATIC, "InputProvider", "IOPlugin"));
				classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.io.include.IOPlugin", engine.getPath().getEntryName(LibraryEntryType.STATIC, "IOPlugin", "IOPlugin"));
				
				result = (new JarIncludeHelper(engine, this)).
						includeStatic("org/coreasm/engine/plugins/io/OutputFunctionElement.java", EntryType.FUNCTION, "output").
						includeStatic("org/coreasm/compiler/plugins/io/include/InputFunctionElement.java", EntryType.FUNCTION, "input").
						includeStatic("org/coreasm/engine/plugins/io/InputProvider.java", EntryType.INCLUDEONLY).
						includeStatic("org/coreasm/compiler/plugins/io/include/IOPlugin.java", EntryType.INCLUDEONLY).
						includeStatic("org/coreasm/compiler/plugins/io/include/IOAggregator.java", EntryType.AGGREGATOR).build();
				
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}
		return result;
	}

	@Override
	public List<EngineTransition> getTransitions() {
		List<EngineTransition> result = new ArrayList<EngineTransition>();
		CodeFragment c = new CodeFragment("");
		String stringelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "StringElement", "StringPlugin");
		c.appendLine("try{\n@decl(String,msgs) = @RuntimeProvider@.getStorage().getValue(new @RuntimePkg@.Location(\"output\", new java.util.ArrayList<@RuntimePkg@.Element>())).toString();\n");
		c.appendLine("outputStream.print(@msgs@);\n");
		c.appendLine("@RuntimeProvider@.getStorage().setValue(new @RuntimePkg@.Location(\"output\", new java.util.ArrayList<@RuntimePkg@.Element>()), new " + stringelement + "(\"\"));\n");
		c.appendLine("}\ncatch(@decl(Exception,e)){\n}\n");
		EngineTransition et = new EngineTransition(c, "emAggregation", "emStepSucceeded");
		result.add(et);
		
		return result;
	}

	@Override
	public CodeFragment getInitCode() {
		CodeFragment result = new CodeFragment("");
		result.appendLine("java.io.PrintStream outputStream = System.out;\n");
		
		return result;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new PrintRuleHandler(), CodeType.U, "Rule", "PrintRule", null);
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}
}
