package org.coreasm.compiler.exception;

/**
 * Signals an already existing entry
 * @author Markus Brenner
 *
 */
public class EntryAlreadyExistsException extends Exception {
	private static final long serialVersionUID = 1L;
	private String entryName;
	/**
	 * Builds a new exception signaling a name collision with the given entry
	 * @param name The name of the duplicate entry
	 */
	public EntryAlreadyExistsException(String name){
		super("An entry with the name " + name + " already exists in the library");
		entryName = name;
	}
	
	/**
	 * @return the entryname causing this exception
	 */
	public String getEntryName(){
		return entryName;
	}
}
