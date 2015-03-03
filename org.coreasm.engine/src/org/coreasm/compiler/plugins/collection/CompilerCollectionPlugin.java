package org.coreasm.compiler.plugins.collection;

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
import org.coreasm.compiler.plugins.collection.code.ucode.AddToHandler;
import org.coreasm.compiler.plugins.collection.code.ucode.RemoveFromHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.collection.FilterFunctionElement;
import org.coreasm.engine.plugins.collection.MapFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerCollectionPlugin extends CompilerCodePlugin implements CompilerFunctionPlugin, CompilerVocabularyExtender, CompilerPlugin {

	private Plugin interpreterPlugin;
	
	public CompilerCollectionPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	//private final String FilterName = "filter";
	private final String FoldName = "fold";
	private final String FoldLName = "foldl";
	private final String FoldRName = "foldr";
	//private final String MapName = "map";

	@Override
	public String getName() {
		return "CollectionPlugin";
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		ClassLibrary library = engine.getClassLibrary();
		
		
		File enginePath = engine.getOptions().enginePath;
		if(enginePath == null){
			engine.getLogger().error(getClass(), "loading classes from a directory is currently not supported");
			throw new CompilerException("could not load classes");
		}
		else{			
			try {
				//add package replacements for imported classes which can be used by other plugins
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractBagElement", "plugins.CollectionPlugin.AbstractBagElement");
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractListElement", "plugins.CollectionPlugin.AbstractListElement");
				library.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableIndexedCollection", "plugins.CollectionPlugin.ModifiableIndexedCollection");
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractMapElement", "plugins.CollectionPlugin.AbstractMapElement");
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractSetElement", "plugins.CollectionPlugin.AbstractSetElement");
				library.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableCollection", "plugins.CollectionPlugin.ModifiableCollection");
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.CollectionFunctionElement", "plugins.CollectionPlugin.CollectionFunctionElement");
				
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/AbstractBagElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/AbstractListElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/ModifiableIndexedCollection.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/AbstractMapElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/AbstractSetElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/ModifiableCollection.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/CollectionFunctionElement.java", this), EntryType.INCLUDEONLY, ""));
				
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/FilterFunctionElement.java", this), EntryType.FUNCTION_CAPI, FilterFunctionElement.NAME));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/FoldFunctionElement.java", this), EntryType.FUNCTION_CAPI, FoldLName));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/FoldFunctionElement.java", this), EntryType.FUNCTION_CAPI, FoldName));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/FoldrFunctionElement.java", this), EntryType.FUNCTION_CAPI, FoldRName));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/MapFunctionElement.java", this), EntryType.FUNCTION_CAPI, MapFunctionElement.NAME));
			} catch (IncludeException e) {
				throw new CompilerException(e);
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}
		return result;
	}

	@Override
	public List<String> getCompileFunctionNames() {
		List<String> result = new ArrayList<String>();
		// result.add("map");
		// result.add("filter");
		// result.add("fold");
		// result.add("foldr");
		// result.add("foldl");
		return result;
	}

	@Override
	public CodeFragment compileFunctionCall(ASTNode n) throws CompilerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new AddToHandler(), CodeType.U, "Rule", "AddToCollectionRule", null);
		register(new RemoveFromHandler(), CodeType.U, "Rule", "RemoveFromCollectionRule", null);
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}
}
