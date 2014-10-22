package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Interface for lr-code providing plugins.
 * @author Markus Brenner
 *
 */
public interface CompilerCodeLRPlugin {
	/**
	 * Compiles the node n into a piece of code.
	 * The execution of the code will leave a location and a value on the stack,
	 * where the value is the topmost item of the stack and the location beneath it.
	 * @param n A node in the parse tree
	 * @return A piece of code
	 * @throws CompilerException If an error occurred during the compilation
	 */
	public CodeFragment lrCode(ASTNode n) throws CompilerException;
}
