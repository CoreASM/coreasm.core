package org.coreasm.compiler.plugins.map.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles map creation
 * @author Spellmaker
 *
 */
public class MapHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		result.appendLine("@decl(java.util.Map<@RuntimePkg@.Element,@RuntimePkg@.Element>,mp)=new java.util.HashMap<>();\n");
		if (node.getAbstractChildNodes().size() > 0)
			result.appendLine("@decl(@RuntimePkg@.Element,tmp)=null;\n");
		
		for (ASTNode maplet : node.getAbstractChildNodes()) {
			result.appendFragment(engine.compile(
					maplet.getAbstractChildNodes().get(0), CodeType.R));
			result.appendFragment(engine.compile(
					maplet.getAbstractChildNodes().get(1), CodeType.R));
			result.appendLine("@tmp@=(@RuntimePkg@.Element)evalStack.pop();\n");
			result.appendLine("@mp@.put((@RuntimePkg@.Element)evalStack.pop(), @tmp@);\n");
		}
		String mapelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "MapElement", "MapPlugin");
		result.appendLine("evalStack.push(new " + mapelement + "(@mp@));\n");
	}

}
