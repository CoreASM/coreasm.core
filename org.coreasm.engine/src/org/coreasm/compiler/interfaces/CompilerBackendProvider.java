package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.components.backend.CompilerFileWriter;
import org.coreasm.compiler.components.backend.CompilerPacker;

/**
 * A plugin which provides backend handlers for the compiler.
 * Implementing classes can choose to return null for one of
 * the methods, if they do not want to provide a part of the backend.
 * @author Spellmaker
 *
 */
public interface CompilerBackendProvider {
	/**
	 * Gets the File writer supplied by this provider, if any
	 * @return A {@link CompilerFileWriter}, or null
	 */
	public CompilerFileWriter getFileWriter();
	/**
	 * Gets the packer supplied by this provider, if any
	 * @return A {@link CompilerPacker}, or null
	 */
	public CompilerPacker getPacker();
}
