package org.coreasm.compiler.plugins.turboasm.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class WhileRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {		
		result.appendLine("@decl(CompilerRuntime.AbstractStorage,storage)=CompilerRuntime.RuntimeProvider.getRuntime().getStorage();\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList,composed)=new CompilerRuntime.UpdateList();\n");
		result.appendLine("@decl(boolean,guard)=true;\n");
		result.appendFragment(engine.compile(node.getAbstractChildNodes().get(0), CodeType.R));
		result.appendLine("@guard@=((CompilerRuntime.BooleanElement)evalStack.pop()).getValue();\n");
		result.appendLine("@storage@.pushState();\n");
		result.appendLine("if(@guard@){\n");
		result.appendFragment(engine.compile(node.getAbstractChildNodes().get(1), CodeType.U));
		result.appendLine("@decl(CompilerRuntime.UpdateList,current)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
		result.appendLine("while(!@current@.isEmpty() && @guard@){\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList,aggreg)=@storage@.performAggregation(@current@);\n");
		result.appendLine("@composed@ = @storage@.compose(@composed@,@current@);\n");
		result.appendLine("if(@storage@.isConsistent(@aggreg@)){\n");
		result.appendLine("@storage@.apply(@aggreg@);\n");
		result.appendFragment(engine.compile(node.getAbstractChildNodes().get(0), CodeType.R));
		result.appendLine("@guard@=((CompilerRuntime.BooleanElement)evalStack.pop()).getValue();\n");
		result.appendLine("if(!@guard@){\n");
		result.appendLine("break;\n");
		result.appendLine("}\n");
		result.appendFragment(engine.compile(node.getAbstractChildNodes().get(1), CodeType.U));
		result.appendLine("@current@ = (CompilerRuntime.UpdateList) evalStack.pop();\n");
		result.appendLine("}\n");
		result.appendLine("else{\n");
		result.appendLine("break;\n");
		result.appendLine("}\n");
		result.appendLine("}\n");
		result.appendLine("}\n");
		result.appendLine("@storage@.popState();\n");
		result.appendLine("evalStack.push(@composed@);\n");
	}

}
