package org.coreasm.compiler.plugins.math;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.plugins.math.code.rcode.RandomValueHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.math.MathPlugin;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerMathPlugin extends CompilerCodePlugin implements CompilerPlugin,
		CompilerVocabularyExtender {

	private Plugin interpreterPlugin;
	
	public CompilerMathPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {

		Map<String, MathFunctionEntry> functions = MathPluginHelper
				.createFunctions();

		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		File enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
		
		if(enginePath == null){
			CoreASMCompiler.getEngine().getLogger().error(getClass(), "loading classes from a directory is currently not supported");
			throw new CompilerException("could not load classes");
		}
		else{
			try {
				//classLibrary.addPackageReplacement("org.coreasm.engine.plugins.set.SetElement", "plugins.SetPlugin.SetElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.math.MathFunction", "plugins.MathPlugin.MathFunction");
				classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.math.include.PowerSetElement", "plugins.MathPlugin.PowerSetElement");
				
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
	public void registerCodeHandlers() throws CompilerException {
		register(new RandomValueHandler(), CodeType.R, "Expression", "RandomValue", null);
	}
}
