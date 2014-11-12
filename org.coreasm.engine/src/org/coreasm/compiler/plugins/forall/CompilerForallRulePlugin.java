package org.coreasm.compiler.plugins.forall;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.forallrule.ForallRulePlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.forall.code.ucode.ForallRuleHandler;

public class CompilerForallRulePlugin extends CompilerCodePlugin implements CompilerPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerForallRulePlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	@Override
	public String getName() {
		return ForallRulePlugin.PLUGIN_NAME;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new ForallRuleHandler(), CodeType.U, "Rule", "ForallRule", null);
	}
}
