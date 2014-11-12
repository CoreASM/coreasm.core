package org.coreasm.compiler.plugins.chooserule.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class PickRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		CodeFragment loc = engine.compile(node.getAbstractChildNodes().get(0), CodeType.L);
		CodeFragment source = engine.compile(node.getAbstractChildNodes().get(1), CodeType.R);
		
		result.appendLine("");
		result.appendFragment(loc);
		result.appendLine("@decl(String,loc)=((CompilerRuntime.Location)evalStack.pop()).name;\n");
		result.appendFragment(source);
		result.appendLine("@decl(java.util.ArrayList<CompilerRuntime.Element>, src) = new java.util.ArrayList<CompilerRuntime.Element>(((CompilerRuntime.Enumerable)evalStack.pop()).enumerate());\n");
		if(node.getAbstractChildNodes().size() == 3){
			CodeFragment test = engine.compile(node.getAbstractChildNodes().get(2), CodeType.R);
			result.appendLine("for(@decl(int,i)=@src@.size() - 1; @i@ >= 0; @i@--){\n");
			result.appendLine("localStack.pushLayer();\n");
			result.appendLine("localStack.put(@loc@, @src@.get(@i@));\n");
			result.appendFragment(test);
			result.appendLine("localStack.popLayer();\n");
			result.appendLine("if(!evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
			result.appendLine("@src@.remove(@i@);\n");
			result.appendLine("}\n");
			result.appendLine("}\n");
		}
		result.appendLine("evalStack.push(@src@.get(CompilerRuntime.RuntimeProvider.getRuntime().randInt(@src@.size())));\n");
	}

}
