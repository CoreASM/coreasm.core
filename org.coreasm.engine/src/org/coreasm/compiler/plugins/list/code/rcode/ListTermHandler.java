package org.coreasm.compiler.plugins.list.code.rcode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles the list term.
 * Constructs lists
 * @author Spellmaker
 *
 */
public class ListTermHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {		
		result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,list)=new java.util.ArrayList<@RuntimePkg@.Element>();\n");
		for(ASTNode child : node.getAbstractChildNodes()){
			result.appendFragment(engine.compile(child, CodeType.R));
			result.appendLine("@list@.add((@RuntimePkg@.Element)evalStack.pop());\n");
		}
		String listelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "ListElement", "ListPlugin");
		result.appendLine("evalStack.push(new " + listelement + "(@list@));\n");
	}

}
