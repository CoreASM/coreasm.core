package org.coreasm.compiler.plugins.kernel.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles the import rule
 * @author Spellmaker
 *
 */
public class KernelImportRule implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		// import x do {}
		// get the identifier
		result.appendLine("");
		CodeFragment name = engine.compile(
				node.getAbstractChildNodes().get(0), CodeType.L);
		result.appendFragment(name);
		result.appendLine("@decl(@RuntimePkg@.Location,nameloc)=(@RuntimePkg@.Location)evalStack.pop();\n");
		result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");

		result.appendLine("localStack.pushLayer();\n");
		result.appendLine("localStack.put(@nameloc@.name, new @RuntimePkg@.Element());\n");
		result.appendFragment(engine.compile(
				node.getAbstractChildNodes().get(1), CodeType.U));
		result.appendLine("localStack.popLayer();\n");
	}

}
