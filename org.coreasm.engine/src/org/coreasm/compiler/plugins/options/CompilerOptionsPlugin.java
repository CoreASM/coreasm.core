package org.coreasm.compiler.plugins.options;

import java.util.Map;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.options.OptionNode;
import org.coreasm.engine.plugins.options.OptionsPlugin;

import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeBPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;

public class CompilerOptionsPlugin implements CompilerCodeBPlugin, CompilerPlugin {
	private Map<String, String> options;
	
	@Override
	public String getName() {
		return OptionsPlugin.PLUGIN_NAME;
	}

	@Override
	public void bCode(ASTNode n)
			throws CompilerException {
		if(n.getGrammarClass().equals("Declaration")){
			if(n.getGrammarRule().equals("PropertyOption")){
				OptionNode on = (OptionNode) n;
				String name = on.getOptionName();
				String value = on.getOptionValue();
				CoreASMCompiler.getEngine().getOptions().properties.put(name,value);
				
				return;
			}
		}

		throw new CompilerException("unhandled code type: (OptionsPlugin, bCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	/**
	 * Provides access to the generated options of the specification
	 * @param name The name of the option
	 * @return The value of the option
	 */
	public String getOption(String name){
		return options.get(name);
	}
}
