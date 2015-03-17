package org.coreasm.compiler.plugins.turboasm.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles the iterate rule
 * @author Spellmaker
 *
 */
public class IterateRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {

		result.appendLine("@decl(@RuntimePkg@.AbstractStorage,storage)=@RuntimeProvider@.getStorage();\n");
		result.appendLine("@decl(@RuntimePkg@.UpdateList,composed)=new @RuntimePkg@.UpdateList();\n");
		result.appendLine("@storage@.pushState();\n");
		result.appendFragment(engine.compile(node.getAbstractChildNodes().get(0), CodeType.U));
		result.appendLine("@decl(@RuntimePkg@.UpdateList,current)=(@RuntimePkg@.UpdateList)evalStack.pop();\n");
		result.appendLine("while(!@current@.isEmpty()){\n");
		result.appendLine("@decl(@RuntimePkg@.UpdateList,aggreg)=@storage@.performAggregation(@current@);\n");
		result.appendLine("@composed@ = @storage@.compose(@composed@,@current@);\n");
		result.appendLine("if(@storage@.isConsistent(@aggreg@)){\n");
		result.appendLine("@storage@.apply(@aggreg@);\n");
		result.appendFragment(engine.compile(node.getAbstractChildNodes().get(0), CodeType.U));
		result.appendLine("@current@ = (@RuntimePkg@.UpdateList) evalStack.pop();\n");
		result.appendLine("}\n");
		result.appendLine("else{\n");
		result.appendLine("break;\n");
		result.appendLine("}\n");
		result.appendLine("}\n");
		result.appendLine("@storage@.popState();\n");
		result.appendLine("evalStack.push(@composed@);\n");
	}

}
