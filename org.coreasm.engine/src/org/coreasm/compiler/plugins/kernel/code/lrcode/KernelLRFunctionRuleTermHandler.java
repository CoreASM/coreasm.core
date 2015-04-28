package org.coreasm.compiler.plugins.kernel.code.lrcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
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

		result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,args)=new java.util.ArrayList<@RuntimePkg@.Element>();\n");

		if (frtn.hasArguments()) {
			for (ASTNode child : frtn.getArguments()) {
				result.appendFragment(engine.compile(child,
						CodeType.R));
				result.appendLine("@args@.add((@RuntimePkg@.Element)evalStack.pop());\n");
			}
		}

		result.appendLine("@decl(@RuntimePkg@.Location,loc)=new @RuntimePkg@.Location(\""
				+ frtn.getName() + "\", @args@);\n");
		result.appendLine("evalStack.push(@loc@);\n");
		result.appendLine("evalStack.push(@RuntimeProvider@.getStorage().getValue(@loc@));\n");
	}

}
