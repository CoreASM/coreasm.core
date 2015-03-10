package org.coreasm.compiler;
import java.util.List;

import org.coreasm.compiler.exception.NotCompilableException;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.engine.Engine;

/**
 * Plugin Loader interface. Loads, stores and categorizes plugins
 * required by the current specification for the compiler.
 * @author Markus Brenner
 *
 */
public interface PluginLoader {
	/**
	 * Loads the plugins used by the specification of the engine.
	 * This method needs to check, whether all required plugins are compilable.
	 * It might use some optimizations to find out, which plugins and elements
	 * are actually required.
	 * @param cae The CoreASM Engine which has parsed the specification
	 * @throws NotCompilableException Thrown when a plugin is identified as required, but is not considered compilable.
	 * A plugin is not considered compilable, if it (directly or indirectly) implements the CompilerPlugin interface
	 */
	public void loadPlugins(Engine cae) throws NotCompilableException;
	/**
	 * Looks up a plugin with the given name in the plugin loader
	 * @param name The name of the desired plugin
	 * @return A CompilerPlugin with the given name, or null, if no such plugin can be found
	 */
	public CompilerPlugin getPlugin(String name);
	
	public List<CompilerPlugin> getPluginByType(Class<?> type);
}
