package org.coreasm.compiler.backend;

import java.io.File;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.exception.CompilerException;

public interface CompilerFileWriter {
	public List<File> writeEntriesToDisk(List<LibraryEntry> entries, CompilerEngine engine) throws CompilerException;
}
