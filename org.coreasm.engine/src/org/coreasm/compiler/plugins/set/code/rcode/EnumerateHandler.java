package org.coreasm.compiler.plugins.set.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class EnumerateHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		for(ASTNode c : node.getAbstractChildNodes()){
			result.appendFragment(engine.compile(c, CodeType.R));
		}
		result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,slist)=new java.util.ArrayList<CompilerRuntime.Element>();\n");
		for(int i = 0; i < node.getAbstractChildNodes().size(); i++){
			result.appendLine("@slist@.add((CompilerRuntime.Element)evalStack.pop());\n");
		}
		result.appendLine("evalStack.push(new plugins.SetPlugin.SetElement(@slist@));\n");
	}

}
