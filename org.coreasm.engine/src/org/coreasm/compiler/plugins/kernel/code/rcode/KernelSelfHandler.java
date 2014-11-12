package org.coreasm.compiler.plugins.kernel.code.rcode;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class KernelSelfHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		result.appendLine("if(CompilerRuntime.RuntimeProvider.getRuntime().getSelf(Thread.currentThread()) == null)\n"
				+ "evalStack.push(CompilerRuntime.Element.UNDEF);\n"
				+ "else\n" + "evalStack.push(CompilerRuntime.RuntimeProvider.getRuntime().getSelf(Thread.currentThread()));\n");

	}

}
