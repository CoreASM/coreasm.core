package org.coreasm.compiler.plugins.letrule.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class LetRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		try {
			CodeFragment name = engine.compile(node.getAbstractChildNodes().get(0), CodeType.L);
			result.appendFragment(name);
			result.appendLine("@decl(CompilerRuntime.Location,nameloc)=(CompilerRuntime.Location)evalStack.pop();\n");
			result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");
			
			CodeFragment val = engine.compile(node.getAbstractChildNodes().get(1), CodeType.R);
			CodeFragment upd = engine.compile(node.getAbstractChildNodes().get(2), CodeType.U);
			
			result.appendFragment(val);
			result.appendLine("localStack.pushLayer();\nlocalStack.put(@nameloc@.name, evalStack.pop());\n");
			result.appendFragment(upd);
			result.appendLine("localStack.popLayer();\n");

		} catch (Exception e) {
			throw new CompilerException(e);
		}
	}

}
