package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.CompilerPathConfig;

public interface CompilerPathPlugin extends CompilerPlugin {
	public CompilerPathConfig getPathConfig();
}
