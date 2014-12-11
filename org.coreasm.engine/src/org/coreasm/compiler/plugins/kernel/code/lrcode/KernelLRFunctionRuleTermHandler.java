package org.coreasm.compiler.plugins.kernel.code.lrcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;

/**
 * Handles l-r code for f(t1, t2, ... tn)
 * @author Spellmaker
 *
 */
public class KernelLRFunctionRuleTermHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		// evaluate the right side of an expression
		// and push the location of the expression and the value to the
		// stack
		
		//TODO: Maybe it should be considered, that the name could be the name of a rule parameter evaluating to a location?

		result.appendLine("");
		FunctionRuleTermNode frtn = (FunctionRuleTermNode) node;

		result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,args)=new java.util.ArrayList<CompilerRuntime.Element>();\n");

		if (frtn.hasArguments()) {
			for (ASTNode child : frtn.getArguments()) {
				result.appendFragment(CoreASMCompiler.getEngine().compile(child,
						CodeType.R));
				result.appendLine("@args@.add((CompilerRuntime.Element)evalStack.pop());\n");
			}
		}

		result.appendLine("@decl(CompilerRuntime.Location,loc)=new CompilerRuntime.Location(\""
				+ frtn.getName() + "\", @args@);\n");
		result.appendLine("evalStack.push(@loc@);\n");
		result.appendLine("evalStack.push(CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(@loc@));\n");
	}

}
