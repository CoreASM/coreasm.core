package org.coreasm.compiler.classlibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;

public class ClassLibrary {
	private List<LibraryEntry> entries;
	private CompilerEngine engine;
	private Map<String, String> packageReplacements;
	
	public ClassLibrary(CompilerEngine engine){
		this.entries = new ArrayList<LibraryEntry>();
		this.engine = engine;
		this.packageReplacements = new HashMap<String, String>();
	}
	
	public void addEntry(LibraryEntry entry) throws EntryAlreadyExistsException{
		if(entries.contains(entry)){
			engine.addError("Entry (" + entry.toString() + " already exists in the library");
			throw new EntryAlreadyExistsException(entry.toString());
		}
		
		entries.add(entry);
	}
	
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
	
	public void addPackageReplacement(String original, String replacement){
		packageReplacements.put(original, replacement);
	}
	
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
