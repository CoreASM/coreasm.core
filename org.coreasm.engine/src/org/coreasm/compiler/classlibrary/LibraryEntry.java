package org.coreasm.compiler.classlibrary;

import org.coreasm.compiler.classlibrary.AbstractLibraryEntry;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * Basic interface for a class library entry.
 * Every class that somehow wants to be entered into the class library
 * needs to implement this interface.
 * As the writeFile method is not implemented here, using {@link AbstractLibraryEntry}
 * for easier use is recommended.
 * @author Markus Brenner
 *
 */
public interface LibraryEntry {
	/**
	 * Writes the file to its target destination
	 * @throws LibraryEntryException If an error occured
	 */
	public void writeFile() throws LibraryEntryException;
	/**
	 * Provides the full name (with packages) of the class represented by this entry.
	 * @return The full name of the entry
	 */
	public String getFullName();
}
