package org.coreasm.compiler.plugins.predicatelogic.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class ExistsExpHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		CodeFragment name = engine.compile(
				node.getAbstractChildNodes().get(0), CodeType.L);
		result.appendFragment(name);
		result.appendLine("@decl(@RuntimePkg@.Location,nameloc)=(@RuntimePkg@.Location)evalStack.pop();\n");
		result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");

		CodeFragment source = engine.compile(
				node.getAbstractChildNodes().get(1), CodeType.R);
		CodeFragment guard = engine.compile(
				node.getAbstractChildNodes().get(2), CodeType.R);

		result.appendFragment(source);
		result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,list)=new java.util.ArrayList<@RuntimePkg@.Element>(((@RuntimePkg@.Enumerable)evalStack.pop()).enumerate());\n");
		result.appendLine("for(@decl(int,i)=0;@i@<=@list@.size();@i@++){\n");
		result.appendLine("if(@i@ == @list@.size()){\n");
		result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.FALSE);\n");
		result.appendLine("}\n");
		result.appendLine("else{\n");
		result.appendLine("localStack.pushLayer();\n");
		result.appendLine("localStack.put(@nameloc@.name, @list@.get(@i@));\n");
		result.appendFragment(guard);
		result.appendLine("if(evalStack.pop().equals(@RuntimePkg@.BooleanElement.TRUE)){\n");
		result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.TRUE);\n");
		result.appendLine("break;\n");
		result.appendLine("}\n");
		result.appendLine("localStack.popLayer();\n");
		result.appendLine("}\n");
		result.appendLine("}\n");
	}

}
