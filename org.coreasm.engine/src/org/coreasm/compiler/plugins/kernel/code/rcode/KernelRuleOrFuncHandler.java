package org.coreasm.compiler.plugins.kernel.code.rcode;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.preprocessor.Information;
import org.coreasm.compiler.components.preprocessor.Preprocessor;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles rule or func nodes
 * @author Spellmaker
 *
 */
public class KernelRuleOrFuncHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		try {
			String name = node.getAbstractChildNodes().get(0).getToken();
								
			//get rule names
			Preprocessor prep = engine.getPreprocessor();
			Information inf = prep.getGeneralInfo().get("RuleDeclaration");
			
			if (inf.getChildren().contains(name)) {
				// if it is a rule, return the rule element
				result.appendLine(
						"@decl(@RuntimePkg@.Rule, tmprule)=new @RulePkg@."
								+ name + "();\n");
				result.appendLine("@tmprule@.initRule(new java.util.ArrayList<@RuntimePkg@.RuleParam>(), null);\n");
				result.appendLine("evalStack.push(@tmprule@);\n");
			} else {
				// otherwise get the function element from the abstract
				// storage
				result.appendLine(
						"evalStack.push(@RuntimeProvider@.getStorage().getFunction(\""
								+ name + "\"));\n");
			}

		} catch (Exception e) {
			throw new CompilerException(e);
		}
	}

}
