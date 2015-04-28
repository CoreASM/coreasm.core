package org.coreasm.compiler.components.classlibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;

/**
 * Module which stores {@link LibraryEntry} instances.
 * This class holds all files in their respective LibraryEntry form in memory
 * during the compilation process of CoreASMC.
 * It provides a method to finalize the entries before the compiler
 * converts them into files on the hard drive and can be used to find
 * specific entries for further use.
 * @author Spellmaker
 *
 */
public class ClassLibrary {
	private List<LibraryEntry> entries;
	private List<RuleClassFile> rules;
	private CompilerEngine engine;
	private Map<String, String> packageReplacements;
	
	/**
	 * Constructs a new ClassLibrary
	 * @param engine The compiler engine supervising the compilation process
	 */
	public ClassLibrary(CompilerEngine engine){
		this.entries = new ArrayList<LibraryEntry>();
		this.rules = new ArrayList<RuleClassFile>();
		this.engine = engine;
		this.packageReplacements = new HashMap<String, String>();
	}
	
	/**
	 * Adds a new entry to the class library
	 * @param entry A new library entry
	 * @throws EntryAlreadyExistsException If an entry e with e.equals(entry) == true already exists in the library
	 */
	public void addEntry(LibraryEntry entry) throws EntryAlreadyExistsException{
		if(entries.contains(entry)){
			engine.addError("Entry (" + entry.toString() + " already exists in the library");
			throw new EntryAlreadyExistsException(entry.toString());
		}
		
		entries.add(entry);
		if(entry instanceof RuleClassFile){
			rules.add((RuleClassFile)entry);
		}
	}
	
	/**
	 * Selects all rules contained in the class library
	 * @return A list of {@link RuleClassFile} instances
	 */
	public List<RuleClassFile> getRules(){
		return Collections.unmodifiableList(rules);
	}
	
	/**
	 * Finds a LibraryEntry matching the given filters.
	 * If a filter is set to null, the search will match all values to it.
	 * If no entry matching the filter is found, the method will return null.
	 * @param name The name of the entry, or null
	 * @param source The source of the entry, or null
	 * @param type The type of the entry, or null
	 * @return An entry matching the filter, or null, if there is none
	 */
	public LibraryEntry findEntry(String name, String source, LibraryEntryType type){
		List<LibraryEntry> result = new ArrayList<LibraryEntry>();
		for(LibraryEntry l : entries){
			if((name == null || l.getName().equals(name)) && (source == null || l.getSource().equals(source)) && (type == null || l.getType().equals(type))){
				result.add(l);
			}
		}
		//TOOD: better error handling; if there is more than one entry, that should actually be an error
		if(result.size() >= 1){
			return result.get(0);
		}
		return null;
	}
	
	/**
	 * Adds a global package replacement.
	 * A global package replacement will be applied to all LibraryEntry instances of type JarInclude
	 * upon finalizing the class library.
	 * This will replace the package declarations in the includes.
	 * @param original The original package declaration or a part of a package declaration
	 * @param replacement The replacement string for the original declaration
	 */
	public void addPackageReplacement(String original, String replacement){
		packageReplacements.put(original, replacement);
	}
	
	/**
	 * Finalizes the class library.
	 * This will execute final modifications to the library entries.
	 * Currently only adds package replacements to jar includes.
	 * @return A list of the library entries of this class library
	 */
	public List<LibraryEntry> buildLibrary(){
		//propagate global package replacements
		for(LibraryEntry l : entries){
			if(l instanceof JarInclude){
				for(Entry<String, String> e : packageReplacements.entrySet())
					((JarInclude) l).addPackageReplacement(e.getKey(), e.getValue());
			}
		}
		return Collections.unmodifiableList(entries);
	}
}
