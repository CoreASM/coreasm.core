package org.coreasm.compiler.plugins.number.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class NumberRangeHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		CodeFragment start = engine.compile(
				node.getAbstractChildNodes().get(0), CodeType.R);
		CodeFragment end = engine.compile(
				node.getAbstractChildNodes().get(1), CodeType.R);
		CodeFragment step = null;
		if (node.getAbstractChildNodes().size() == 3) {
			step = engine.compile(
					node.getAbstractChildNodes().get(2), CodeType.R);
		}

		result.appendFragment(start);
		result.appendFragment(end);
		if (step != null) {
			result.appendFragment(step);
			result.appendLine("@decl(double,step)=(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue());\n");
		}
		result.appendLine("@decl(double,end)=(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue());\n");
		result.appendLine("@decl(double,start)=(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue());\n");
		if (step != null) {
			result.appendLine("evalStack.push(new plugins.NumberPlugin.NumberRangeElement(@start@,@end@,@step@));\n");
		} else {
			result.appendLine("evalStack.push(new plugins.NumberPlugin.NumberRangeElement(@start@,@end@));\n");
		}
	}

}
