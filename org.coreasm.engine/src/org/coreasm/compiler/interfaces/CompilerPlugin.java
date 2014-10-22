package org.coreasm.compiler.interfaces;

/**
 * Interface for compilable plugins. 
 * Used to check, whether a plugin is compilable and to obtain
 * node information.
 * @author Markus Brenner
 *
 */
public interface CompilerPlugin {
	/**
	 * Provides the name of the plugin
	 * @return The name of the plugin
	 */
	public String getName();
}
