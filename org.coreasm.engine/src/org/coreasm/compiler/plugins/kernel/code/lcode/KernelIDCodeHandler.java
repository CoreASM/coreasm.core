package org.coreasm.compiler.plugins.kernel.code.lcode;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles ids as locations
 * @author Spellmaker
 *
 */
public class KernelIDCodeHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine) {
		result.appendLine("evalStack.push(new @RuntimePkg@.Location(\""
				+ node.getToken()
				+ "\", new java.util.ArrayList<@RuntimePkg@.Element>()));\n");
	}

}
