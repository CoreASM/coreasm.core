package org.coreasm.compiler;
import java.util.List;

import org.coreasm.compiler.exception.NotCompilableException;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerExtensionPointPlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerInitCodePlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPathPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerPreprocessorPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
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
	
	/**
	 * Retrieves a list of all plugins identified to be CompilerVocabularyExtender Plugins
	 * @return A list of all CompilerVocabularyExtender Plugins
	 */
	public List<CompilerVocabularyExtender> getVocabularyExtenderPlugins();

	/**
	 * Retrieves a list of all plugins identified to be ExtensionPoint Plugins
	 * @return A list of all ExtensionPoint Plugins
	 */
	public List<CompilerExtensionPointPlugin> getExtensionPointPlugins();

	/**
	 * Retrieves a list of all plugins identified to be InitCode Plugins
	 * @return A list of all InitCode Plugins
	 */
	public List<CompilerInitCodePlugin> getInitCodePlugins();

	/**
	 * Retrieves a list of all plugins identified to be Operator Plugins
	 * @return A list of all Operator Plugins
	 */
	public List<CompilerOperatorPlugin> getOperatorPlugins();

	/**
	 * Retrieves a list of all plugins identified to be Function Plugins
	 * @return A list of all Function Plugins
	 */
	public List<CompilerFunctionPlugin> getFunctionPlugins();

	/**
	 * Retrieves a list of all plugins identified to be Preprocessor Plugins 
	 * @return A list of all Preprocessor Plugins
	 */
	public List<CompilerPreprocessorPlugin> getPreprocessorPlugins();
	
	/**
	 * Retrieves a list of all plugins identified to be Code providing plugins
	 * @return A list of all CompilerCode Plugins
	 */
	public List<CompilerCodePlugin> getCompilerCodePlugins();
	
	public List<CompilerPathPlugin> getCompilerPathPlugins();
}
