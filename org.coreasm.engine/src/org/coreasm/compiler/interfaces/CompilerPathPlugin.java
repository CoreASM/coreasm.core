package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.CompilerPathConfig;
import org.coreasm.compiler.DefaultPaths;

/**
 * Plugin providing an alternative path definition.
 * By default, the compiler will use {@link DefaultPaths} to determine
 * the location of files in the compilation unit.
 * A plugin can provide an alternative definition, e.g. to include
 * the generated code in another project.
 * Only one alternative configuration can be active at a time.
 * @author Spellmaker
 *
 */
public interface CompilerPathPlugin extends CompilerPlugin {
	/**
	 * Gets the path configuration provided by this plugin
	 * @return A path configuration
	 */
	public CompilerPathConfig getPathConfig();
}
