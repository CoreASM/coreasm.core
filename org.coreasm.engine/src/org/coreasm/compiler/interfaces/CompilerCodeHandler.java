package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Interface for code producing classes.
 * A code handler produces code for exactly one type of nodes in the syntax tree.
 * @author Spellmaker
 *
 */
public interface CompilerCodeHandler {	
	/**
	 * Compiles the given node into a CodeFragment.
	 * Note that the result object cannot be replaced with a new {@link CodeFragment}, as it
	 * is the result of the compilation.
	 * @param result Preconstructed (empty) {@link CodeFragment} for the result of the compilation
	 * @param node The current node in the syntax tree
	 * @param engine The compiler engine, used for further compilation or for other services
	 * @throws CompilerException If the compilation failed
	 */
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine) throws CompilerException;
}
