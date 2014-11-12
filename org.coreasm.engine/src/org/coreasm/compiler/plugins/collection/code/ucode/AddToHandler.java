package org.coreasm.compiler.plugins.collection.code.ucode;

import java.util.List;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class AddToHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		List<ASTNode> children = node.getAbstractChildNodes();
		CodeFragment lhs = engine.compile(
				children.get(0), CodeType.R);
		CodeFragment rhs = engine.compile(
				children.get(1), CodeType.L);

		result.appendFragment(lhs);
		result.appendFragment(rhs);
		result.appendLine("@decl(CompilerRuntime.Location, loc)=(CompilerRuntime.Location)evalStack.pop();\n");
		result.appendLine("@decl(CompilerRuntime.Element, el) = (CompilerRuntime.Element) evalStack.pop();\n");
		result.appendLine("@decl(plugins.CollectionPlugin.ModifiableCollection, coll) = (plugins.CollectionPlugin.ModifiableCollection)CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(@loc@);\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList, ul) = new CompilerRuntime.UpdateList();\n");
		result.appendLine("@ul@.addAll(@coll@.computeAddUpdate(@loc@, @el@, this));\n");
		result.appendLine("evalStack.push(@ul@);\n");
	}

}
