package org.coreasm.compiler.plugins.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.mainprogram.statemachine.EngineTransition;
import org.coreasm.compiler.plugins.io.code.ucode.PrintRuleHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerExtensionPointPlugin;
import org.coreasm.compiler.interfaces.CompilerInitCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerIOPlugin extends CompilerCodePlugin implements CompilerPlugin, CompilerVocabularyExtender, CompilerExtensionPointPlugin, CompilerInitCodePlugin{

	private Plugin interpreterPlugin;
	
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
		String enginePathStr = CoreASMCompiler.getEngine().getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePathStr == null){
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.stringplugin.include.StringElement", 
					"plugins.StringPlugin.StringElement");
			
			try{
				File iopluginFolder = new File("src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\ioplugin\\include".replace("\\", File.separator));

				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(iopluginFolder, "OutputFunctionElement.java"),
						this), EntryType.FUNCTION, "output"));
		
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(iopluginFolder, "InputFunctionElement.java"),
						this), EntryType.FUNCTION, "input"));
		
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(iopluginFolder, "InputProvider.java"),
						this), EntryType.INCLUDEONLY, ""));
		
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(iopluginFolder, "IOHelper.java"),
						this), EntryType.INCLUDEONLY, ""));
		
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(iopluginFolder, "IOAggregator.java"),
						this), EntryType.AGGREGATOR, ""));
			}
			catch(EntryAlreadyExistsException e){
				throw new CompilerException(e);
			}
		}
		else{			
			try {
				File enginePath = new File(enginePathStr);

				//classLibrary.addPackageReplacement("org.coreasm.engine.plugins.string.StringElement", "plugins.StringPlugin.StringElement");
				
				//add package replacements for classes accessible for other plugins
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.io.InputProvider", "plugins.IOPlugin.InputProvider");
				classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.io.include.IOPlugin", "plugins.IOPlugin.IOPlugin");
				
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/io/OutputFunctionElement.java", this), EntryType.FUNCTION, "output"));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/io/include/InputFunctionElement.java", this), EntryType.FUNCTION, "input"));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/io/InputProvider.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/io/include/IOPlugin.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/io/include/IOAggregator.java", this), EntryType.AGGREGATOR, ""));
			} catch (IncludeException e) {
				throw new CompilerException(e);
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
		
		c.appendLine("try{\n@decl(String,msgs) = CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(new CompilerRuntime.Location(\"output\", new java.util.ArrayList<CompilerRuntime.Element>())).toString();\n");
		c.appendLine("outputStream.print(@msgs@);\n");
		c.appendLine("CompilerRuntime.RuntimeProvider.getRuntime().getStorage().setValue(new CompilerRuntime.Location(\"output\", new java.util.ArrayList<CompilerRuntime.Element>()), new plugins.StringPlugin.StringElement(\"\"));\n");
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
}
