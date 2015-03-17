package org.coreasm.compiler.backend;

import java.io.File;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CompilerPathConfig;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.exception.CompilerException;

/**
 * A Module which converts library entries into files on the hard disk.
 * Implementing classes should take a look at the {@link CompilerPathConfig} and {@link LibraryEntry} classes
 * to generate the file structure appropriately.
 * The file writer can be used to introduce additional classes and code or to merge the project
 * with other java code.
 * @author Spellmaker
 *
 */
public interface CompilerFileWriter {
	/**
	 * Writes library entries to the hard drive
	 * @param entries A list of library entries
	 * @param engine The compiler engine supervising the operation
	 * @return A list of files generated from the library entries
	 * @throws CompilerException If an error occurs
	 */
	public List<File> writeEntriesToDisk(List<LibraryEntry> entries, CompilerEngine engine) throws CompilerException;
}
