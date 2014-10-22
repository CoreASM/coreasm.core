package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
/**
 * Interface for lur-code providing plugins.
 * @author Markus Brenner
 *
 */
public interface CompilerCodeLURPlugin {
	/**
	 * Compiles the node n into a piece of code.
	 * The execution of the code will leave a location, an UpdateList and a value on the stack,
	 * where the value is the topmost item of the stack, the next item is the UpdateList and
	 * beneath it the location.
	 * @param n A node in the parse tree
	 * @return A piece of code
	 * @throws CompilerException If an error occurred during the compilation
	 */
	public CodeFragment lurCode(ASTNode n) throws CompilerException;
}
