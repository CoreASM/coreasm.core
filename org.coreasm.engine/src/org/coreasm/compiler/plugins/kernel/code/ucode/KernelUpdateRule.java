package org.coreasm.compiler.plugins.kernel.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.kernel.UpdateRuleNode;

public class KernelUpdateRule implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		// rule of the form identifier := value
		if (!(node instanceof UpdateRuleNode))
			throw new CompilerException(
					"Illegal Node found - expected UpdateRuleNode");

		ASTNode location = node.getFirst();
		ASTNode expression = node.getFirst().getNext();

		CodeFragment lhs = engine.compile(location,
				CodeType.L);
		CodeFragment rhs = engine.compile(expression,
				CodeType.R);

		// generates an update
		result.appendLine("\n");
		result.appendFragment(rhs);
		result.appendFragment(lhs);
		result.appendLine("\n@decl(CompilerRuntime.Location,tmplocation)=(CompilerRuntime.Location)evalStack.pop();\n");
		result.appendLine("@decl(CompilerRuntime.Element,tmpvalue)=(CompilerRuntime.Element)evalStack.pop();\n");
		result.appendLine("\n@decl(CompilerRuntime.Update,tmpupdate)=new CompilerRuntime.Update(@tmplocation@, @tmpvalue@, \"updateAction\", this.getUpdateResponsible(), null);\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList,tmplist)=new CompilerRuntime.UpdateList();\n");
		result.appendLine("@tmplist@.add(@tmpupdate@);\n");
		result.appendLine("evalStack.push(@tmplist@);\n");
	}

}
