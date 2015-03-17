package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.classlibrary.LibraryEntry;

/**
 * Backend plugin which provides a main class.
 * The main class is the main entry point for the program.
 * A CompilerMainClassProvider can be used to merge the generated
 * code into a larger project or to modify the startup.
 * @author Spellmaker
 *
 */
public interface CompilerMainClassProvider {
	/**
	 * Gets a LibraryEntry for the main class 
	 * @return The main class as a library entry
	 */
	public LibraryEntry getMainClass();
}
