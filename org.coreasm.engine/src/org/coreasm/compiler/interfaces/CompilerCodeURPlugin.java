package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
/**
 * Interface for ur-code providing plugins.
 * @author Markus Brenner
 *
 */
public interface CompilerCodeURPlugin {
	/**
	 * Compiles the node n into a piece of code.
	 * The execution of the code will leave an UpdateList and a value on the stack,
	 * where the value is the topmost item of the stack and the next item is the UpdateList.
	 * @param n A node in the parse tree
	 * @return A piece of code
	 * @throws CompilerException If an error occurred during the compilation
	 */
	public CodeFragment urCode(ASTNode n) throws CompilerException;
}
