package org.coreasm.compiler.plugins.blockrule.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class BlockRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		result.appendLine("");
	
		if(node.getAbstractChildNodes().size() <= 0) throw new CompilerException("empty BlockRule");
		
		for(int i = 0; i < node.getAbstractChildNodes().size(); i++){
			result.appendFragment(engine.compile(node.getAbstractChildNodes().get(i), CodeType.U));
		}
		
		result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=new CompilerRuntime.UpdateList();\n");
		for(int i = 0; i < node.getAbstractChildNodes().size(); i++){
			result.appendLine("@ulist@.addAll((CompilerRuntime.UpdateList)evalStack.pop());\n");
		}
		
		result.appendLine("evalStack.push(@ulist@);\n");
	}

}
