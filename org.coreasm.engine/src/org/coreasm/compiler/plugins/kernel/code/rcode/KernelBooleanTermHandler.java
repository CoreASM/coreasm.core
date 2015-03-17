package org.coreasm.compiler.plugins.kernel.code.rcode;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles boolean creation
 * @author Spellmaker
 *
 */
public class KernelBooleanTermHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		result.appendLine("evalStack.push("
						+ ((node.getToken().equals("true")) ? "@RuntimePkg@.BooleanElement.TRUE"
								: "@RuntimePkg@.BooleanElement.FALSE")
						+ ");");
	}

}
