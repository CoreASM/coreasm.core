package org.coreasm.compiler.plugins.turboasm.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class ReturnRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		//Note: The current definition of return result
		//is a bit strange. Even though it returns an expression,
		//it can't be used as one and can only be used as the top
		//level node of a rule, making that rule return a value.
		
		CodeFragment exp = engine.compile(node.getAbstractChildNodes().get(0), CodeType.R);
		CodeFragment rule = engine.compile(node.getAbstractChildNodes().get(1), CodeType.U);
		
		result.appendFragment(rule);
		result.appendLine("@decl(CompilerRuntime.UpdateList,updates)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
		result.appendLine("@decl(CompilerRuntime.AbstractStorage,storage)=CompilerRuntime.RuntimeProvider.getRuntime().getStorage();\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList,aggreg)=@storage@.performAggregation(@updates@);\n");
		result.appendLine("if(@storage@.isConsistent(@aggreg@)){\n");
		result.appendLine("@storage@.pushState();\n");
		result.appendLine("@storage@.apply(@aggreg@);\n");
		result.appendFragment(exp);
		result.appendLine("@storage@.popState();\n");
		result.appendLine("}\n");
		result.appendLine("else{\n");
		result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
		result.appendLine("}\n");
	}

}
