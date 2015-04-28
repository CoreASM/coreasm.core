package org.coreasm.testing;

import java.util.HashMap;
import java.util.Map;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.logging.LoggingHelper;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.plugins.conditionalrule.CompilerConditionalRulePlugin;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.testing.value.ParameterProvider;
import org.mockito.Mockito;

public class CompilerMock {
	private Map<String, ParameterProvider> mockCode;
	
	public CompilerMock(){
		mockCode = new HashMap<String, ParameterProvider>();
	}
	
	public CodeFragment compile(ASTNode root, CodeType type) throws CompilerException{
		CompilerEngine mockCompiler = Mockito.mock(CompilerEngine.class);
		
		for(String s : mockCode.keySet()){
			Mockito.doReturn(new CodeFragment(mockCode.get(s).compilerValue())).when(mockCompiler).compile(Mockito.eq(new TestingNode(s)), Mockito.any(CodeType.class));
		}
		Mockito.doReturn(new LoggingHelper()).when(mockCompiler).getLogger();

		CompilerConditionalRulePlugin sut = new CompilerConditionalRulePlugin(null);
		sut.registerCodeHandlers();
		return sut.compile(type, root);
	}
}

