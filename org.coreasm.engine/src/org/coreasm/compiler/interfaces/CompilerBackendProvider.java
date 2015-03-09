package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.backend.CompilerFileWriter;
import org.coreasm.compiler.backend.CompilerPacker;

public interface CompilerBackendProvider {
	public CompilerFileWriter getFileWriter();
	public CompilerPacker getPacker();
}
