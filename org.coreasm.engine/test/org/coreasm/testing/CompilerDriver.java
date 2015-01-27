package org.coreasm.testing;

import java.util.HashMap;
import java.util.Map;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.LoggingHelper;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.plugins.conditionalrule.CompilerConditionalRulePlugin;
import org.coreasm.engine.interpreter.ASTNode;
import org.mockito.Mockito;

public class CompilerDriver {
	public Map<String, String> mockCode = new HashMap<String, String>();
	
	public CodeFragment compile(ASTNode root, CodeType type) throws CompilerException{
		CompilerEngine mockCompiler = Mockito.mock(CompilerEngine.class);
		
		for(String s : mockCode.keySet()){
			Mockito.doReturn(new CodeFragment(mockCode.get(s))).when(mockCompiler).compile(Mockito.eq(TestDriver.makeASTNode(s)), Mockito.any(CodeType.class));
		}
		Mockito.doReturn(new LoggingHelper()).when(mockCompiler).getLogger();
		CoreASMCompiler.setEngine(mockCompiler);

		CompilerConditionalRulePlugin sut = new CompilerConditionalRulePlugin(null);
		sut.registerCodeHandlers();
		return sut.compile(type, root);
	}
}
