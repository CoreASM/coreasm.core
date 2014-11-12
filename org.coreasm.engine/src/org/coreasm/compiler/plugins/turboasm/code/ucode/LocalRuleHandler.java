package org.coreasm.compiler.plugins.turboasm.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class LocalRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		CodeFragment id = engine.compile(node.getAbstractChildNodes().get(0), CodeType.L);
		CodeFragment rule = engine.compile(node.getAbstractChildNodes().get(1), CodeType.U);
		result.appendFragment(id);
		result.appendFragment(rule);
		result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
		result.appendLine("@decl(CompilerRuntime.Location,loc)=(CompilerRuntime.Location)evalStack.pop();\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList,result)=new CompilerRuntime.UpdateList();\n");
		result.appendLine("for(@decl(CompilerRuntime.Update,u): @ulist@){\n");
		result.appendLine("if(!@u@.loc.name.equals(@loc@.name)){\n");
		result.appendLine("@result@.add(@u@);\n");
		result.appendLine("}\n");
		result.appendLine("}\n");
		result.appendLine("evalStack.push(@result@);\n");
	}

}
