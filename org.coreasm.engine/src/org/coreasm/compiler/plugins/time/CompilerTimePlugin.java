package org.coreasm.compiler.plugins.time;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.engine.plugins.time.NowFunctionElement;
import org.coreasm.engine.plugins.time.StepCountFunctionElement;

public class CompilerTimePlugin implements CompilerPlugin, CompilerVocabularyExtender {

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
		
		if(enginePath == null){
			try {
				classLibrary
						.addPackageReplacement(
								"de.spellmaker.coreasmc.plugins.dummy.numberplugin.include.NumberElement",
								"plugins.NumberPlugin.NumberElement");
				result.add(new MainFileEntry(
						classLibrary
								.includeClass(
										"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\timeplugin\\include\\NowFunctionElement.java",
										this), EntryType.FUNCTION, "now"));
				result.add(new MainFileEntry(
						classLibrary
								.includeClass(
										"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\timeplugin\\include\\StepCountFunctionElement.java",
										this), EntryType.FUNCTION_CAPI, "stepcount"));
			}
			catch(Exception e){
				throw new CompilerException(e);
			}
		}
		else{
			try {
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.NumberElement", "plugins.NumberPlugin.NumberElement");
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/time/NowFunctionElement.java", this), EntryType.FUNCTION, NowFunctionElement.NOW_FUNC_NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/time/StepCountFunctionElement.java", this), EntryType.FUNCTION_CAPI, StepCountFunctionElement.FUNC_NAME));
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
		return "TimePlugin";
	}
}
