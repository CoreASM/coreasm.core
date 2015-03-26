package org.coreasm.compiler.plugins.number.code.rcode;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles number creation
 * @author Spellmaker
 *
 */
public class NumberHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		String numberelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberElement", "NumberPlugin");
		result.appendLine("evalStack.push(" + numberelement + ".getInstance("
				+ Double.parseDouble(node.getToken()) + "));\n");
	}

}
