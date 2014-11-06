package org.coreasm.compiler.plugins.io;

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
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.io.OutputFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerExtensionPointPlugin;
import org.coreasm.compiler.interfaces.CompilerInitCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerIOPlugin implements CompilerPlugin, CompilerVocabularyExtender, CompilerExtensionPointPlugin, CompilerInitCodePlugin, CompilerCodeUPlugin{

	@Override
	public String getName() {
		return "IOPlugin";
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary) throws CompilerException {
		String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePath == null){
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.stringplugin.include.StringElement", 
					"plugins.StringPlugin.StringElement");
			
			try{
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\ioplugin\\include\\OutputFunctionElement.java", 
						this), EntryType.FUNCTION, "output"));
		
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\ioplugin\\include\\InputFunctionElement.java", 
						this), EntryType.FUNCTION, "input"));
		
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\ioplugin\\include\\InputProvider.java", 
						this), EntryType.INCLUDEONLY, ""));
		
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\ioplugin\\include\\IOHelper.java", 
						this), EntryType.INCLUDEONLY, ""));
		
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\ioplugin\\include\\IOAggregator.java", 
						this), EntryType.AGGREGATOR, ""));
			}
			catch(EntryAlreadyExistsException e){
				throw new CompilerException(e);
			}
		}
		else{			
			try {
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.string.StringElement", "plugins.StringPlugin.StringElement");
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
	public CodeFragment uCode(ASTNode n)
			throws CompilerException {
		if(n.getGrammarClass().equals("Rule")){
			if(n.getGrammarRule().equals("PrintRule")){
				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.R));
				result.appendLine("@decl(String,msg)=evalStack.pop().toString();\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=new CompilerRuntime.UpdateList();\n");
				result.appendLine("@ulist@.add(new CompilerRuntime.Update(new CompilerRuntime.Location(\"output\", new java.util.ArrayList<CompilerRuntime.Element>()), new plugins.StringPlugin.StringElement(@msg@), \"printAction\", this.getUpdateResponsible(), null));\n");
				result.appendLine("evalStack.push(@ulist@);\n");
				return result;
			}
		}
		
		throw new CompilerException(
				"unhandled code type: (IOPlugin, uCode, "
						+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}
}
