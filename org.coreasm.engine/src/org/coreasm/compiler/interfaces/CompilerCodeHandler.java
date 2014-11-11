package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.engine.interpreter.ASTNode;

public interface CompilerCodeHandler {
	public CodeType getType();
	public String getGrammarClass();
	public String getGrammarRule();
	public String getToken();
	
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine);
}
