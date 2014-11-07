package org.coreasm.compiler.plugins.map;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.map.MapBackgroundElement;
import org.coreasm.engine.plugins.map.MapToPairsFunctionElement;
import org.coreasm.engine.plugins.map.ToMapFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerMapPlugin implements CompilerPlugin, CompilerCodeRPlugin,
		CompilerVocabularyExtender {

	private Plugin interpreterPlugin;
	
	public CompilerMapPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {
		String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePath == null){
			classLibrary
					.addPackageReplacement(
							"org.coreasm.compiler.dummy.collection.include.AbstractListElement",
							"plugins.CollectionPlugin.AbstractListElement");
			classLibrary
					.addPackageReplacement(
							"org.coreasm.compiler.dummy.collection.include.AbstractMapElement",
							"plugins.CollectionPlugin.AbstractMapElement");
			classLibrary
					.addPackageReplacement(
							"org.coreasm.compiler.dummy.collection.include.ModifiableCollection",
							"plugins.CollectionPlugin.ModifiableCollection");
			classLibrary
					.addPackageReplacement(
							"org.coreasm.compiler.dummy.listplugin.include.ListElement",
							"plugins.ListPlugin.ListElement");
			classLibrary
					.addPackageReplacement(
							"org.coreasm.compiler.dummy.setplugin.include.SetBackgroundElement",
							"plugins.SetPlugin.SetBackgroundElement");
			classLibrary
					.addPackageReplacement(
							"org.coreasm.compiler.dummy.setplugin.include.SetElement",
							"plugins.SetPlugin.SetElement");
	
			try {
				result.add(new MainFileEntry(
						classLibrary
								.includeClass(
										"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\mapplugin\\include\\MapToPairsFunctionElement.java",
										this), EntryType.FUNCTION, "mapToPairs"));
				result.add(new MainFileEntry(
						classLibrary
								.includeClass(
										"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\mapplugin\\include\\ToMapFunctionElement.java",
										this), EntryType.FUNCTION, "toMap"));
				result.add(new MainFileEntry(
						classLibrary
								.includeClass(
										"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\mapplugin\\include\\MapBackgroundElement.java",
										this), EntryType.BACKGROUND, "MAP"));
				result.add(new MainFileEntry(
						classLibrary
								.includeClass(
										"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\mapplugin\\include\\MapElement.java",
										this), EntryType.INCLUDEONLY, ""));
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		
		}
		else{
			try {
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractMapElement", "plugins.CollectionPlugin.AbstractMapElement");
				classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableCollection", "plugins.CollectionPlugin.ModifiableCollection");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.map.MapBackgroundElement", "plugins.MapPlugin.MapBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListElement", "plugins.ListPlugin.ListElement");
				
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/map/MapBackgroundElement.java", this), EntryType.BACKGROUND, MapBackgroundElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/map/MapToPairsFunctionElement.java", this), EntryType.FUNCTION, MapToPairsFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/map/ToMapFunctionElement.java", this), EntryType.FUNCTION, ToMapFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/map/include/MapElement.java", this), EntryType.INCLUDEONLY, ""));
			} catch (IncludeException e) {
				throw new CompilerException(e);
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}

		return result;
	}

	@Override
	public CodeFragment rCode(ASTNode n) throws CompilerException {
		if (n.getGrammarClass().equals("Expression")) {
			CodeFragment result = new CodeFragment("");
			result.appendLine("@decl(java.util.Map<CompilerRuntime.Element,CompilerRuntime.Element>,mp)=new java.util.HashMap<>();\n");
			if (n.getAbstractChildNodes().size() > 0)
				result.appendLine("@decl(CompilerRuntime.Element,tmp)=null;\n");
			if (n.getGrammarRule().equals("MapTerm")) {
				for (ASTNode maplet : n.getAbstractChildNodes()) {
					result.appendFragment(CoreASMCompiler.getEngine().compile(
							maplet.getAbstractChildNodes().get(0), CodeType.R));
					result.appendFragment(CoreASMCompiler.getEngine().compile(
							maplet.getAbstractChildNodes().get(1), CodeType.R));
					result.appendLine("@tmp@=(CompilerRuntime.Element)evalStack.pop();\n");
					result.appendLine("@mp@.put((CompilerRuntime.Element)evalStack.pop(), @tmp@);\n");
				}
				result.appendLine("evalStack.push(new plugins.MapPlugin.MapElement(@mp@));\n");
			}

			return result;
		}

		throw new CompilerException("unhandled code type: (MapPlugin, rCode, "
				+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	@Override
	public String getName() {
		return "MapPlugin";
	}
}
