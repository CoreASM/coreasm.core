package org.coreasm.compiler.plugins.time;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.JarIncludeHelper;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.time.NowFunctionElement;
import org.coreasm.engine.plugins.time.StepCountFunctionElement;

public class CompilerTimePlugin implements CompilerPlugin, CompilerVocabularyExtender {
	private CompilerEngine engine;
	private Plugin interpreterPlugin;
	
	public CompilerTimePlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		File enginePath = engine.getOptions().enginePath;
		
		if(enginePath == null){
			engine.getLogger().error(getClass(), "loading classes from a directory is currently not supported");
			throw new CompilerException("could not load classes");
		}
		else{
			try {
				//classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.NumberElement", "plugins.NumberPlugin.NumberElement");
				result = (new JarIncludeHelper(engine, this)).
						includeStatic("org/coreasm/engine/plugins/time/NowFunctionElement.java", EntryType.FUNCTION, NowFunctionElement.NOW_FUNC_NAME).
						includeStatic("org/coreasm/engine/plugins/time/StepCountFunctionElement.java", EntryType.FUNCTION_CAPI, StepCountFunctionElement.FUNC_NAME).
						build();
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
			
		}
		
		return result;
	}

	@Override
	public String getName() {
		return "TimePlugin";
	}
}
