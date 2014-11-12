package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;

public interface CompilerCodeHandler {	
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine) throws CompilerException;
}
