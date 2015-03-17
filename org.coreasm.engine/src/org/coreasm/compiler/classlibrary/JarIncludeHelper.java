package org.coreasm.compiler.classlibrary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;

/**
 * Helper class to make the inclusion of several files from an archive easier.
 * The helper provides a fluent api, which allows to drop unnecessary parameters
 * to shorten includes.
 * Each include method includes the specified entry into the class library
 * @author Spellmaker
 *
 */
public class JarIncludeHelper {
	private final JarIncludeHelper previous;
	private final MainFileEntry entry;
	private final int size;
	private final CompilerEngine engine;
	private final CompilerPlugin plugin;
	
	/**
	 * Initializes a new JarIncludeHelper, valid for the specified plugin
	 * @param engine The compiler engine supervising the compilation process
	 * @param plugin The plugin, which uses the helper to include classes
	 */
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

	/**
	 * Statically includes a file from the runtime jar with no runtime name
	 * @param className The name (path) of the file
	 * @param entryType The main file type of the include
	 * @return The next helper to be used for the next include
	 * @throws EntryAlreadyExistsException If the specified entry already exists in the library
	 */
	public JarIncludeHelper includeStatic(String className, EntryType entryType) throws EntryAlreadyExistsException{
		return include(engine.getOptions().enginePath, className, entryType, "", LibraryEntryType.STATIC);
	}

	/**
	 * Statically includes a file from the runtime jar with a runtime name
	 * @param className The name (path) of the file
	 * @param entryType The main file type of the include
	 * @param name The runtime name of the include
	 * @return The next helper to be used for the next include
	 * @throws EntryAlreadyExistsException If the specified entry already exists in the library
	 */
	public JarIncludeHelper includeStatic(String className, EntryType entryType, String name) throws EntryAlreadyExistsException{
		return include(engine.getOptions().enginePath, className, entryType, name, LibraryEntryType.STATIC);
	}

	/**
	 * Statically includes a file from the specified jar with no runtime name
	 * @param jarFile The source jar archive
	 * @param className The name (path) of the file
	 * @param entryType The main file type of the include
	 * @return The next helper to be used for the next include
	 * @throws EntryAlreadyExistsException If the specified entry already exists in the library
	 */
	public JarIncludeHelper includeStatic(File jarFile, String className, EntryType entryType) throws EntryAlreadyExistsException{
		return include(jarFile, className, entryType, "", LibraryEntryType.STATIC);
	}

	/**
	 * Statically includes a file from the specified jar with a runtime name
	 * @param jarFile The source jar archive
	 * @param className The name (path) of the file
	 * @param entryType The main file type of the include
	 * @param name The runtime name of the file
	 * @return The next helper to be used for the next include
	 * @throws EntryAlreadyExistsException If the specified entry already exists in the library
	 */
	public JarIncludeHelper includeStatic(File jarFile, String className, EntryType entryType, String name) throws EntryAlreadyExistsException{
		return include(jarFile, className, entryType, name, LibraryEntryType.STATIC);
	}
	

	/**
	 * Dynamically includes a file from the runtime jar with no runtime name
	 * @param className The name (path) of the file
	 * @param entryType The main file type of the include
	 * @return The next helper to be used for the next include
	 * @throws EntryAlreadyExistsException If the specified entry already exists in the library
	 */
	public JarIncludeHelper includeDynamic(String className, EntryType entryType) throws EntryAlreadyExistsException{
		return includeDynamic(className, entryType, "");
	}

	/**
	 * Dynamically includes a file from the runtime jar with a runtime name
	 * @param className The name (path) of the file
	 * @param entryType The main file type of the include
	 * @param name The runtime name of the include
	 * @return The next helper to be used for the next include
	 * @throws EntryAlreadyExistsException If the specified entry already exists in the library
	 */
	public JarIncludeHelper includeDynamic(String className, EntryType entryType, String name) throws EntryAlreadyExistsException{
		return include(engine.getOptions().enginePath, className, entryType, name, LibraryEntryType.DYNAMIC);
	}

	/**
	 * Dynamically includes a file from the specified jar with no runtime name
	 * @param jarFile The source jar archive
	 * @param className The name (path) of the file
	 * @param entryType The main file type of the include
	 * @return The next helper to be used for the next include
	 * @throws EntryAlreadyExistsException If the specified entry already exists in the library
	 */
	public JarIncludeHelper includeDynamic(File jarFile, String className, EntryType entryType) throws EntryAlreadyExistsException{
		return include(jarFile, className, entryType, "", LibraryEntryType.DYNAMIC);
	}

	/**
	 * Dynamically includes a file from the specified jar with a runtime name
	 * @param jarFile The source jar archive
	 * @param className The name (path) of the file
	 * @param entryType The main file type of the include
	 * @param name The runtime name of the file
	 * @return The next helper to be used for the next include
	 * @throws EntryAlreadyExistsException If the specified entry already exists in the library
	 */
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
	
	/**
	 * Builds the helper into a list of {@link MainFileEntry} instances.
	 * The intended use for this method is to generate a list which can be
	 * returned in the {@link CompilerVocabularyExtender#loadClasses(ClassLibrary)} method.
	 * @return A list of main files entries generated from the included classes
	 */
	public List<MainFileEntry> build(){
		List<MainFileEntry> result = new ArrayList<MainFileEntry>(size);
		this.build(result);
		return result;
	}
}
