package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Interface for lCode providing plugins
 * @author Markus Brenner
 *
 */
public interface CompilerCodeLPlugin {
	/**
	 * Compiles the given node into a piece of code which leaves a location on the stack.
	 * @param n A node in the parse tree
	 * @return A piece of code 
	 * @throws CompilerException If an error occurred during the compilation
	 */
	public CodeFragment lCode(ASTNode n) throws CompilerException;
}
