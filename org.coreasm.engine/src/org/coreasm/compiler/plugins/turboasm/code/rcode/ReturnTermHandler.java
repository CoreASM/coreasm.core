package org.coreasm.compiler.plugins.turboasm.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles the return expression.
 * This rule has a lot of potential to digress from the interpreter
 * version, as the rule call mechanism is one of the key differences
 * between the interpreter and the compiler.
 * Potential errors need to be examined carefully and might lead to
 * complete overhauls of core mechanisms.
 * @author Spellmaker
 *
 */
public class ReturnTermHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		
		CodeFragment exp = engine.compile(node.getAbstractChildNodes().get(0), CodeType.R);
		CodeFragment rule = engine.compile(node.getAbstractChildNodes().get(1), CodeType.U);
		
		result.appendFragment(rule);
		result.appendLine("@decl(@RuntimePkg@.UpdateList,updates)=(@RuntimePkg@.UpdateList)evalStack.pop();\n");
		result.appendLine("@decl(@RuntimePkg@.AbstractStorage,storage)=@RuntimeProvider@.getStorage();\n");
		result.appendLine("@decl(@RuntimePkg@.UpdateList,aggreg)=@storage@.performAggregation(@updates@);\n");
		result.appendLine("if(@storage@.isConsistent(@aggreg@)){\n");
		result.appendLine("@storage@.pushState();\n");
		result.appendLine("@storage@.apply(@aggreg@);\n");
		result.appendFragment(exp);
		result.appendLine("@storage@.popState();\n");
		result.appendLine("}\n");
		result.appendLine("else{\n");
		result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
		result.appendLine("}\n");
	}

}
