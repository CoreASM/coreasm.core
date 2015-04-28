package org.coreasm.compiler.plugins.conditionalrule.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.conditionalrule.ConditionalTermNode;

/**
 * Handles the conditional term
 * @author Spellmaker
 *
 */
public class ConditionalTermHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		if(!(node instanceof ConditionalTermNode)) throw new CompilerException("invalid node type in conditionalterm");
		ConditionalTermNode cond = (ConditionalTermNode) node;
		
		result.appendFragment(engine.compile(cond.getCondition(), CodeType.R));
		result.appendLine("if(@RuntimePkg@.BooleanElement.TRUE.equals(evalStack.pop())){\n");
		result.appendFragment(engine.compile(cond.getIfTerm(), CodeType.R));
		result.appendLine("}\n");
		result.appendLine("else{\n");
		if(cond.getElseTerm() == null)
			result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
		else
			result.appendFragment(engine.compile(cond.getElseTerm(), CodeType.R));
		result.appendLine("}\n");
	}

}
