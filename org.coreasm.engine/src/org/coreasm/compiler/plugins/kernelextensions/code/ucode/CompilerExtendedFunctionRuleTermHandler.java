package org.coreasm.compiler.plugins.kernelextensions.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.kernelextensions.ExtendedFunctionRuleTermNode;

public class CompilerExtendedFunctionRuleTermHandler implements
		CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		if(!(node instanceof ExtendedFunctionRuleTermNode)) throw new CompilerException("expected astnode of type ExtendedFunctionRuleTermNode");
		ExtendedFunctionRuleTermNode n = (ExtendedFunctionRuleTermNode) node;
		result.appendFragment(engine.compile(n.getTerm(), CodeType.R));
		result.appendLine("@decl(Object,o)=evalStack.pop();\n");
		result.appendLine("if(!(@o@ instanceof @RuntimePkg@.FunctionElement)){\n");
		result.appendLine("throw new Exception(\"cannot handle an extended rule call on a non-function element\");\n");
		result.appendLine("}\n");
		result.appendLine("@decl(@RuntimePkg@.FunctionElement,function)=(@RuntimePkg@.FunctionElement) @o@;\n");
		result.appendLine("@decl(java.util.LinkedList<@RuntimePkg@.Element>,params)=new java.util.LinkedList<@RuntimePkg@.Element>();\n");
		for(ASTNode a : n.getArguments()){
			result.appendFragment(engine.compile(a, CodeType.R));
			result.appendLine("@params@.add((@RuntimePkg@.Element)evalStack.pop());\n");
		}
		result.appendLine("@decl(@RuntimePkg@.ElementList,plist)=new @RuntimePkg@.ElementList(@params@);\n");
		result.appendLine("@decl(String,fname)=@RuntimeProvider@.getStorage().getFunctionName(@function@);\n");
		result.appendLine("if(@fname@ != null){\n");
		result.appendLine("evalStack.push(@RuntimeProvider@.getStorage().getValue(new @RuntimePkg@.Location(@fname@, @plist@)));\n");
		result.appendLine("} else {\n");
		result.appendLine("evalStack.push(@function@.getValue(@plist@));\n");
		result.appendLine("}\n");
	}

}
