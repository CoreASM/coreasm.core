package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
/**
 * Interface for r-code providing plugins.
 * @author Markus Brenner
 *
 */
public interface CompilerCodeRPlugin {
	/**
	 * Compiles the node n into a piece of code.
	 * The execution of the code will leave a value on the stack.
	 * @param n A node in the parse tree
	 * @return A piece of code
	 * @throws CompilerException If an error occurred during the compilation
	 */
	public CodeFragment rCode(ASTNode n) throws CompilerException;
}
