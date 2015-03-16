package org.coreasm.compiler.plugins.conditionalrule;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.conditionalrule.ConditionalRulePlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.conditionalrule.code.rcode.ConditionalTermHandler;
import org.coreasm.compiler.plugins.conditionalrule.code.ucode.ConditionalRuleHandler;

public class CompilerConditionalRulePlugin extends CompilerCodePlugin implements CompilerPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerConditionalRulePlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	@Override
	public String getName() {
		return ConditionalRulePlugin.PLUGIN_NAME;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new ConditionalRuleHandler(), CodeType.U, "Rule", "ConditionalRule", null);
		register(new ConditionalTermHandler(), CodeType.R, "Expression", "ConditionalTerm", null);
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}
}
