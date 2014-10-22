package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Interface for bCode providing plugins
 * @author Markus Brenner
 *
 */
public interface CompilerCodeBPlugin {
	/**
	 * Compiles the given node as a basic object.
	 * The compilation of a basic object results (most of the time) in an library entry
	 * of sorts.
	 * <p>
	 * bCode compilation happens for all direct child nodes of the root of the specification.
	 * @param n A node in the parse tree
	 * @throws CompilerException If an error occurred during the compilation
	 */
	public void bCode(ASTNode n) throws CompilerException;
}
