package org.coreasm.compiler.plugins.abstraction.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles the abstract rule
 * @author Spellmaker
 *
 */
public class AbstractionAbstractHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		
		String iopluginloc = engine.getPath().getEntryName(LibraryEntryType.STATIC, "IOPlugin", "IOPlugin");
		String stringelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "StringElement", "StringPlugin");
		
		result.appendLine("");
		result.appendFragment(engine.compile(node.getAbstractChildNodes().get(0), CodeType.R));
		result.appendLine("@decl(String,msg)=evalStack.pop().toString();\n");
		result.appendLine("@decl(@RuntimePkg@.UpdateList,ulist)=new @RuntimePkg@.UpdateList();\n");
		result.appendLine("@ulist@.add(new @RuntimePkg@.Update(" + iopluginloc + ".OUTPUT_FUNC_LOC , new " + stringelement + "(\"Abstract Call: \" + @msg@), " + iopluginloc + ".PRINT_ACTION, this.getUpdateResponsible(), null));\n");
		result.appendLine("evalStack.push(@ulist@);\n");
	}

}
