package org.coreasm.compiler.backend;

import java.io.File;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;

/**
 * A Module which packs generated code files.
 * The Packer is a part of the compiler backend. It takes a list of 
 * compiled source files and finalizes the compilation task.
 * The default implementation contained in the {@link KernelBackend} packs
 * the files into an executable jar archive.
 * @author Spellmaker
 *
 */
public interface CompilerPacker {
	/**
	 * Packs a list of files, finalizing the compilation unit
	 * @param files A list of (existing) files
	 * @param engine The compiler engine supervising the compilation process
	 * @return True, if the packing process was successful
	 */
	public boolean packFiles(List<File> files, CompilerEngine engine);
}
