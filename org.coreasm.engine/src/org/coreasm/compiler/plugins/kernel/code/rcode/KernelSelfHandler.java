package org.coreasm.compiler.plugins.kernel.code.rcode;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles the self expression
 * @author Spellmaker
 *
 */
public class KernelSelfHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		result.appendLine("if(@RuntimeProvider@.getSelf(Thread.currentThread()) == null)\n"
				+ "evalStack.push(@RuntimePkg@.Element.UNDEF);\n"
				+ "else\n" + "evalStack.push(@RuntimeProvider@.getSelf(Thread.currentThread()));\n");

	}

}
