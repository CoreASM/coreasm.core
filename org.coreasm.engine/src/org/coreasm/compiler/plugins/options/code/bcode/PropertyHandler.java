package org.coreasm.compiler.plugins.options.code.bcode;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.options.OptionNode;

public class PropertyHandler implements CompilerCodeHandler {	
	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		OptionNode on = (OptionNode) node;
		String name = on.getOptionName();
		String value = on.getOptionValue();
		engine.getOptions().properties.put(name,value);
	}

}
