package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.engine.plugin.Plugin;

/**
 * Interface for compilable plugins. 
 * Used to check, whether a plugin is compilable and to obtain
 * node information.
 * @author Markus Brenner
 *
 */
public interface CompilerPlugin {
	/**
	 * Initializes the plugin, providing a reference to the compiler
	 * @param engine The compiler engine supervising the compilation process
	 */
	public void init(CompilerEngine engine);
	/**
	 * Provides the name of the plugin
	 * @return The name of the plugin
	 */
	public String getName();
	
	/**
	 * Links the plugin to it's corresponding interpreter version
	 * @return The corresponding interpreter plugin
	 */
	public Plugin getInterpreterPlugin();
}
