package org.coreasm.compiler.plugins.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.math.MathPlugin;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerMathPlugin implements CompilerPlugin,
		CompilerVocabularyExtender, CompilerCodeRPlugin {

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {

		Map<String, MathFunctionEntry> functions = MathPluginHelper
				.createFunctions();

		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
		
		if(enginePath == null){
			try {
				classLibrary
						.addPackageReplacement(
								"org.coreasm.compiler.dummy.setplugin.include.SetElement",
								"plugins.SetPlugin.SetElement");
				classLibrary
						.addPackageReplacement(
								"org.coreasm.compiler.dummy.numberplugin.include.NumberElement",
								"plugins.NumberPlugin.NumberElement");
				result.add(new MainFileEntry(
						classLibrary
								.includeClass(
										"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\mathplugin\\include\\MathFunction.java",
										this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(
						classLibrary
								.includeClass(
										"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\mathplugin\\include\\PowerSetElement.java",
										this), EntryType.INCLUDEONLY, ""));
	
				for (Entry<String, MathFunctionEntry> e : functions.entrySet()) {
					classLibrary.addEntry(e.getValue());
					result.add(new MainFileEntry(e.getValue(), EntryType.FUNCTION,
							e.getKey()));
				}
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}
		else{
			try {
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.set.SetElement", "plugins.SetPlugin.SetElement");
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/math/MathFunction.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/math/include/PowerSetElement.java", this), EntryType.INCLUDEONLY, ""));
				
				for(Entry<String, MathFunctionEntry> e : functions.entrySet()){
					classLibrary.addEntry(e.getValue());
					result.add(new MainFileEntry(e.getValue(), EntryType.FUNCTION, e.getKey()));
				}
				
			} catch (IncludeException e) {
				throw new CompilerException(e);
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}

		return result;
	}

	@Override
	public String getName() {
		return MathPlugin.PLUGIN_NAME;
	}

	@Override
	public CodeFragment rCode(ASTNode n) throws CompilerException {

		if (n.getGrammarClass().equals("Expression")) {
			if (n.getGrammarRule().equals("RandomValue")) {
				return new CodeFragment(
						"evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(Math.random()));\n");
			}
		}

		throw new CompilerException("unhandled code type: (MathPlugin, rCode, "
				+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}
}
