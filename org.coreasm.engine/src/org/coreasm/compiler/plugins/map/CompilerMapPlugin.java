package org.coreasm.compiler.plugins.map;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.classlibrary.JarIncludeHelper;
import org.coreasm.compiler.classlibrary.LibraryEntryType;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.plugins.map.code.rcode.MapHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.map.MapBackgroundElement;
import org.coreasm.engine.plugins.map.MapToPairsFunctionElement;
import org.coreasm.engine.plugins.map.ToMapFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerMapPlugin extends CompilerCodePlugin implements CompilerPlugin,
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
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {
		File enginePath = engine.getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePath == null){
			engine.getLogger().error(getClass(), "loading classes from a directory is currently not supported");
			throw new CompilerException("could not load classes");
		}
		else{
			try {
				//classLibrary.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractMapElement", "plugins.CollectionPlugin.AbstractMapElement");
				//classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableCollection", "plugins.CollectionPlugin.ModifiableCollection");
				//classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListElement", "plugins.ListPlugin.ListElement");
				
				//package replacements for classes accessible from other plugins
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.map.MapBackgroundElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "MapBackgroundElement", "MapPlugin"));
				classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.map.include.MapElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "MapElement", "MapPlugin"));
				
				result = (new JarIncludeHelper(engine, this)).
						includeStatic("org/coreasm/engine/plugins/map/MapBackgroundElement.java", EntryType.BACKGROUND, MapBackgroundElement.NAME).
						includeStatic("org/coreasm/engine/plugins/map/MapToPairsFunctionElement.java", EntryType.FUNCTION, MapToPairsFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/map/ToMapFunctionElement.java", EntryType.FUNCTION, ToMapFunctionElement.NAME).
						includeStatic("org/coreasm/compiler/plugins/map/include/MapElement.java", EntryType.INCLUDEONLY).
						build();
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}

		return result;
	}

	@Override
	public String getName() {
		return "MapPlugin";
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new MapHandler(), CodeType.R, "Expression", "MapTerm", null);
	}
}
