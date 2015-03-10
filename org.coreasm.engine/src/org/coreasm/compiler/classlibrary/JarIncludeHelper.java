package org.coreasm.compiler.classlibrary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;

public class JarIncludeHelper {
	private final JarIncludeHelper previous;
	private final MainFileEntry entry;
	private final int size;
	private final CompilerEngine engine;
	private final CompilerPlugin plugin;
	
	public JarIncludeHelper(CompilerEngine engine, CompilerPlugin plugin){
		previous = null;
		entry = null;
		size = 0;
		this.engine = engine;
		this.plugin = plugin;
	}
	
	private JarIncludeHelper(JarIncludeHelper previous, MainFileEntry entry){
		this.previous = previous;
		this.entry = entry;
		this.size = previous.size + 1;
		this.engine = previous.engine;
		this.plugin = previous.plugin;
	}

	public JarIncludeHelper includeStatic(String className, EntryType entryType) throws EntryAlreadyExistsException{
		return includeStatic(className, entryType, "");
	}
	
	public JarIncludeHelper includeStatic(String className, EntryType entryType, String name) throws EntryAlreadyExistsException{
		return include(engine.getOptions().enginePath, className, entryType, name, LibraryEntryType.STATIC);
	}
	
	public JarIncludeHelper includeStatic(File jarFile, String className, EntryType entryType, String name) throws EntryAlreadyExistsException{
		return include(jarFile, className, entryType, name, LibraryEntryType.STATIC);
	}
	
	public JarIncludeHelper includeDynamic(String className, EntryType entryType) throws EntryAlreadyExistsException{
		return includeDynamic(className, entryType, "");
	}
	
	public JarIncludeHelper includeDynamic(String className, EntryType entryType, String name) throws EntryAlreadyExistsException{
		return include(engine.getOptions().enginePath, className, entryType, name, LibraryEntryType.DYNAMIC);
	}
	
	public JarIncludeHelper includeDynamic(File jarFile, String className, EntryType entryType, String name) throws EntryAlreadyExistsException{
		return include(jarFile, className, entryType, name, LibraryEntryType.DYNAMIC);
	}
	
	private JarIncludeHelper include(File jarFile, String className, EntryType entryType, String name, LibraryEntryType libraryType) throws EntryAlreadyExistsException{
		JarInclude incl = new JarInclude(engine, jarFile, className, plugin.getName(), LibraryEntryType.STATIC);
		engine.getClassLibrary().addEntry(incl);
		MainFileEntry entry = new MainFileEntry(incl, entryType, name);
		
		return new JarIncludeHelper(this, entry);
	}
	
	private void build(List<MainFileEntry> result){
		if(entry != null){
			previous.build(result);
			
			result.add(entry);
		}
	}
	
	public List<MainFileEntry> build(){
		List<MainFileEntry> result = new ArrayList<MainFileEntry>(size);
		this.build(result);
		return result;
	}
}
