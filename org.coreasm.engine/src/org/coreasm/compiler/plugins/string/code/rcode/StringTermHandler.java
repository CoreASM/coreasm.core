package org.coreasm.compiler.plugins.string.code.rcode;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles string creation
 * @author Spellmaker
 *
 */
public class StringTermHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		String stringelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "StringElement", "StringPlugin");
		result.appendLine("evalStack.push(new " + stringelement + "(\""
						+ replaceEscapeSeq(node.getToken()) + "\"));\n");
	}
	
	private String replaceEscapeSeq(String o){		
		return o.replaceAll("\n", "\\\\n").
				replaceAll("\\\"", "\\\\\"");
	}

}
