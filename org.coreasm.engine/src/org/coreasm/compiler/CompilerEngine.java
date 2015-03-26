package org.coreasm.compiler;

import java.util.List;
import java.util.Map;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.ClassLibrary;
import org.coreasm.compiler.components.logging.LoggingHelper;
import org.coreasm.compiler.components.mainprogram.StateMachineFile;
import org.coreasm.compiler.components.pluginloader.PluginLoader;
import org.coreasm.compiler.components.preprocessor.Preprocessor;
import org.coreasm.compiler.components.variablemanager.VarManager;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.paths.CompilerPathConfig;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * The compiler engine interface.
 * Contains helper methods for plugins to help them access
 * compiler components
 * @author Markus Brenner
 *
 */
public interface CompilerEngine {
	/**
	 * Returns a reference to the Logging facility of the compiler
	 * @return The {@link LoggingHelper}
	 */
	public LoggingHelper getLogger();
	/**
	 * Returns a reference to the Plugin Loader for access to other plugins
	 * @return The {@link PluginLoader}
	 */
	public PluginLoader getPluginLoader();
	/**
	 * Returns a reference to the Variable Manager for access to the variable
	 * generation 
	 * @return The {@link VarManager}
	 */
	public VarManager getVarManager();
	/**
	 * Returns a reference to the class library.
	 * @return The {@link Preprocessor}
	 */
	public Preprocessor getPreprocessor();
	/**
	 * Returns a reference to the class library.
	 * Provides methods to include classes
	 * @return The {@link ClassLibrary}
	 */
	public ClassLibrary getClassLibrary();
	/**
	 * Returns the compiler options.
	 * @return The {@link CompilerOptions}
	 */
	public CompilerOptions getOptions();
	/**
	 * Returns the MainFile library entry.
	 * Provides methods to manipulate the main class of the generated
	 * code.
	 * @return The {@link StateMachineFile}
	 */
	public StateMachineFile getMainFile();
	/**
	 * Provides access to the path configuration of the engine
	 * @return The path configuration object used by the engine
	 */
	public CompilerPathConfig getPath();
	
	/**
	 * Same as {@link #compile(ASTNode, CodeType)}, but hides errors.
	 * The compiler will discard all errors generated during the processing
	 * of this method. An exception will still be thrown normally
	 * @param node The node to be compiled
	 * @param type The code type to be generated
	 * @return A CodeFragment for the node
	 * @throws CompilerException If an error occured
	 */
	CodeFragment tryCompile(ASTNode node, CodeType type) throws CompilerException;
	/**
	 * Instructs the compiler engine to generate code for the given node.
	 * Even though plugins could be called directly to compile code,
	 * the compilation process should use this method, so that the compiler
	 * can perform additional operations and logging.
	 * @param node The node to be compiled
	 * @param type What kind of code should be produced and what is expected to be on the stack after execution
	 * @return The compiled code for the node or null, if CodeType was BASE
	 * @throws CompilerException If an error occured while compiling the node
	 */
	public CodeFragment compile(ASTNode node, CodeType type) throws CompilerException;
	/**
	 * Adds an error message to the compiler engine. 
	 * Calling the method with the same error text will not add
	 * another error with the same text.
	 * Errors will be displayed at the end of the compiler run.
	 * @param msg The error message
	 */
	public void addError(String msg);
	/**
	 * Adds a warning message to the compiler engine. 
	 * Calling the method with the same warning text will not add
	 * another warning with the same text.
	 * Warnings will be displayed at the end of the compiler run.
	 * @param msg The warning message
	 */
	public void addWarning(String msg);
	/**
	 * Provides read only access to the errors thrown during
	 * the compilation process
	 * @return A list of errors
	 */
	public List<String> getErrors();
	/**
	 * Provides read only access to the warnings thrown during the
	 * compilation process
	 * @return A list of warnings
	 */
	public List<String> getWarnings();
	/**
	 * Adds a timing to the engine.
	 * A Timing is an information about how long a certain operation took.
	 * @param s The name of the timing
	 * @param l The duration of the operation
	 */
	public void addTiming(String s, long l);
	
	/**
	 * Provides access to the global makros declared for this engine.
	 * See {@link CodeFragment} for more information about makros
	 * @return A mapping representing the registered global makros.
	 */
	public Map<String, String> getGlobalMakros();
}
