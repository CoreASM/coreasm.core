package org.coreasm.compiler.plugins.turboasm.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class SeqRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		CodeFragment first = engine.compile(node.getAbstractChildNodes().get(0), CodeType.U);
		CodeFragment second = engine.compile(node.getAbstractChildNodes().get(1), CodeType.U);
		
		//obtain the updates of the first rule
		result.appendFragment(first);
		result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
		//aggregate updates on the update set of the first rule
		result.appendLine("@decl(CompilerRuntime.AbstractStorage,storage)=CompilerRuntime.RuntimeProvider.getRuntime().getStorage();\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList,alist)=@storage@.performAggregation(@ulist@);\n");
		result.appendLine("if(@storage@.isConsistent(@alist@)){\n");
		result.appendLine("@storage@.pushState();\n");
		result.appendLine("@storage@.apply(@alist@);\n");
		result.appendFragment(second);
		result.appendLine("@decl(CompilerRuntime.UpdateList,comp)=@storage@.compose(@ulist@,(CompilerRuntime.UpdateList)evalStack.pop());\n");
		result.appendLine("@storage@.popState();\n");
		result.appendLine("evalStack.push(@comp@);\n");
		result.appendLine("}\n");
		result.appendLine("else{\n");
		result.appendLine("evalStack.push(@ulist@);\n");
		result.appendLine("}\n");
	}

}
