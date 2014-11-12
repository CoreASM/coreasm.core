package org.coreasm.compiler.plugins.kernel.code.bcode;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.compiler.plugins.kernel.CompilerKernelPlugin;
import org.coreasm.engine.interpreter.ASTNode;

public class KernelInitHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		CoreASMCompiler.getEngine().getLogger().debug(CompilerKernelPlugin.class, "extracting initialization rule name");

		// should have exactly one child node which is an id
		String name = node.getAbstractChildNodes().get(0).getToken();

		name = "Rules." + name;

		engine.getMainFile().setInitRule(name);
	}

}
