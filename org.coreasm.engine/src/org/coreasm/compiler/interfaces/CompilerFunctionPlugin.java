package org.coreasm.compiler.interfaces;

import java.util.List;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * An interface for plugins which provide optimized unmodifiable functions.
 * Upon encountering a direct function call, the compiler will try to find
 * a plugin registered for the function name to apply the direct code.
 * If no such plugin can be found, the compiler will fall back to the default
 * implementation.
 * <p>
 * If used, this saves one call to the abstract storage.
 * @author Markus Brenner
 *
 */
public interface CompilerFunctionPlugin {
	/**
	 * Provides the function names compiled by this plugin
	 * @return A list of function names provided by this plugin
	 */
	public List<String> getCompileFunctionNames();
	
	/**
	 * Compiles a function call
	 * @param n The root node of the function call
	 * @return The code for the function call
	 * @exception CompilerException if an error occurred during the compilation
	 */
	public CodeFragment compileFunctionCall(ASTNode n) throws CompilerException;
}
