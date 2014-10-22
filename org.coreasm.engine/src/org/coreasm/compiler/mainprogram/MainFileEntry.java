package org.coreasm.compiler.mainprogram;

import org.coreasm.compiler.classlibrary.LibraryEntry;

import org.coreasm.compiler.mainprogram.EntryType;

/**
 * Wraps the information of an entry to the main file.
 * A main file entry will be included in the initialization
 * of the main class, given it is not of the type INCLUDEONLY
 * @author Markus Brenner
 *
 */
public class MainFileEntry {
	LibraryEntry classFile;
	EntryType entryType;
	String entryName;
	
	/**
	 * Builds a new MainFileEntry.
	 * Note that the LibraryEntry needs to be imported in the ClassLibrary
	 * separately, as the MainFile won't handle this.
	 * @param classFile The library entry of the entry
	 * @param entryType The type of entry, decides how the class is included in the main file
	 * @param entryName With which name the entry will be included, given it is relevant
	 */
	public MainFileEntry(LibraryEntry classFile, EntryType entryType, String entryName){
		this.classFile = classFile;
		this.entryType = entryType;
		this.entryName = entryName;
	}
}
