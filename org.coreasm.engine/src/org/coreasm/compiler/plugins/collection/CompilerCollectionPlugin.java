package org.coreasm.compiler.plugins.collection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.components.classlibrary.ClassLibrary;
import org.coreasm.compiler.components.classlibrary.JarIncludeHelper;
import org.coreasm.compiler.components.classlibrary.LibraryEntry;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.components.mainprogram.EntryType;
import org.coreasm.compiler.components.mainprogram.MainFileEntry;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.plugins.collection.code.ucode.AddToHandler;
import org.coreasm.compiler.plugins.collection.code.ucode.RemoveFromHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.collection.FilterFunctionElement;
import org.coreasm.engine.plugins.collection.MapFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

/**
 * Provides basic support for collections.
 * The plugin alone does not actually include any usable collections.
 * It provides operations on collections and abstract base classes.
 * @author Spellmaker
 *
 */
public class CompilerCollectionPlugin extends CompilerCodePlugin implements CompilerVocabularyExtender, CompilerPlugin {

	private Plugin interpreterPlugin;
	
	/**
	 * Constructs a new plugin
	 * @param parent The interpreter version
	 */
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
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractBagElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "AbstractBagElement", "CollectionPlugin"));
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractListElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "AbstractListElement", "CollectionPlugin"));
				library.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableIndexedCollection", engine.getPath().getEntryName(LibraryEntryType.STATIC, "ModifiableIndexedCollection", "CollectionPlugin"));
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractMapElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "AbstractMapElement", "CollectionPlugin"));
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractSetElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "AbstractSetElement", "CollectionPlugin"));
				library.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableCollection", engine.getPath().getEntryName(LibraryEntryType.STATIC, "ModifiableCollection", "CollectionPlugin"));
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.CollectionFunctionElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "CollectionFunctionElement", "CollectionPlugin"));
				
				JarIncludeHelper include = new JarIncludeHelper(engine, this);
				
				result = include.includeStatic("org/coreasm/engine/plugins/collection/AbstractBagElement.java", EntryType.INCLUDEONLY).
					includeStatic("org/coreasm/engine/plugins/collection/AbstractListElement.java", EntryType.INCLUDEONLY).
					includeStatic("org/coreasm/compiler/plugins/collection/include/ModifiableIndexedCollection.java", EntryType.INCLUDEONLY).
					includeStatic("org/coreasm/engine/plugins/collection/AbstractMapElement.java", EntryType.INCLUDEONLY).
					includeStatic("org/coreasm/engine/plugins/collection/AbstractSetElement.java", EntryType.INCLUDEONLY).
					includeStatic("org/coreasm/compiler/plugins/collection/include/ModifiableCollection.java", EntryType.INCLUDEONLY).
					includeStatic("org/coreasm/engine/plugins/collection/CollectionFunctionElement.java", EntryType.INCLUDEONLY).
					includeStatic("org/coreasm/engine/plugins/collection/FilterFunctionElement.java", EntryType.FUNCTION_CAPI, FilterFunctionElement.NAME).
					includeStatic("org/coreasm/compiler/plugins/collection/include/FoldFunctionElement.java", EntryType.FUNCTION_CAPI, FoldLName).
					//includeStatic("org/coreasm/compiler/plugins/collection/include/FoldFunctionElement.java", EntryType.FUNCTION_CAPI, FoldName).
					includeStatic("org/coreasm/compiler/plugins/collection/include/FoldrFunctionElement.java", EntryType.FUNCTION_CAPI, FoldRName).
					includeStatic("org/coreasm/engine/plugins/collection/MapFunctionElement.java", EntryType.FUNCTION_CAPI, MapFunctionElement.NAME).build();
				LibraryEntry foldElement = classLibrary.findEntry("FoldFunctionElement", this.getName(), LibraryEntryType.STATIC);
				result.add(new MainFileEntry(foldElement, EntryType.FUNCTION_CAPI, FoldName));
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}
		return result;
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
