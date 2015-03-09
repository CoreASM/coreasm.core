package org.coreasm.compiler.backend;

import java.io.File;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;

public interface CompilerPacker {
	public boolean packFiles(List<File> files, CompilerEngine engine);
}
