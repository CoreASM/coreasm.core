/*	
 * VocabularyExtender.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Mashaal Memon
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.compiler.interfaces;

import java.util.List;

import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.mainprogram.MainFileEntry;


/**
 * Interfaces for plugins providing additional classes.
 * A vocabulary extender provides additional classes for the compiled program,
 * such as new Elements, Backgrounds, Schedulers or Aggregators.
 * <p>
 * The caller can assume that all provided classes are already included in the class library
 * @author Markus Brenner
 *
 */
public interface CompilerVocabularyExtender extends CompilerPlugin{	
	/**
	 * Provides a list of MainFileEntries representing the provided classes.
	 * The LibraryEntrys are assumed to be loaded into the class library
	 * @param classLibrary The class library into which classes need to be loaded
	 * @return A list of MainFileEntries
	 * @throws CompilerException If an error occurred while loading classes
	 */
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary) throws CompilerException;
	
}