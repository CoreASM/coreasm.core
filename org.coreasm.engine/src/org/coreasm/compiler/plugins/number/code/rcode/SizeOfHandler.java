package org.coreasm.compiler.plugins.number.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles the sizeof operation
 * @author Spellmaker
 *
 */
public class SizeOfHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		CodeFragment en = engine.compile(
				node.getAbstractChildNodes().get(0), CodeType.R);
		result.appendFragment(en);
		result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,list)=new java.util.ArrayList<@RuntimePkg@.Element>();\n");
		result.appendLine("@list@.add((@RuntimePkg@.Element)evalStack.pop());\n");
		result.appendLine("evalStack.push(@RuntimeProvider@.getStorage().getValue(new @RuntimePkg@.Location(\"size\", @list@)));\n");
	}

}
