package org.coreasm.compiler.plugins.abstraction.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class AbstractionAbstractHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		result.appendLine("");
		result.appendFragment(engine.compile(node.getAbstractChildNodes().get(0), CodeType.R));
		result.appendLine("@decl(String,msg)=evalStack.pop().toString();\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=new CompilerRuntime.UpdateList();\n");
		result.appendLine("@ulist@.add(new CompilerRuntime.Update(plugins.IOPlugin.IOPlugin.OUTPUT_FUNC_LOC, new plugins.StringPlugin.StringElement(\"Abstract Call: \" + @msg@), plugins.IOPlugin.IOPlugin.PRINT_ACTION, this.getUpdateResponsible(), null));\n");
		result.appendLine("evalStack.push(@ulist@);\n");
	}

}
