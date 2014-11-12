package org.coreasm.compiler.plugins.conditionalrule.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class ConditionalRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		ASTNode cond = node.getAbstractChildNodes().get(0);
		ASTNode ifpart = node.getAbstractChildNodes().get(1);
		ASTNode elsepart = null;
		if(node.getAbstractChildNodes().size() == 3){
			elsepart = node.getAbstractChildNodes().get(2);
		}

		try{
			result.appendFragment(engine.compile(cond, CodeType.R));
			result.appendLine("if(evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
			result.appendFragment(engine.compile(ifpart, CodeType.U));
			result.appendLine("}\n");
			if(elsepart != null){
				result.appendLine("else{\n");
				result.appendFragment(engine.compile(elsepart, CodeType.U));
				result.appendLine("}\n");
			}
			else{
				result.appendLine("else{\n");
				result.appendLine("evalStack.push(new CompilerRuntime.UpdateList());\n");
				result.appendLine("}\n");
			}
		}
		catch(Exception e){
			throw new CompilerException(e);
		}
	}

}
