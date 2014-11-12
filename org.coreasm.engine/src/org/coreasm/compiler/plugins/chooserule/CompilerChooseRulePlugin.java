package org.coreasm.compiler.plugins.chooserule;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.chooserule.ChooseRulePlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.chooserule.code.rcode.PickRuleHandler;
import org.coreasm.compiler.plugins.chooserule.code.ucode.ChooseRuleHandler;

public class CompilerChooseRulePlugin extends CompilerCodePlugin implements CompilerPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerChooseRulePlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}
	
	@Override
	public String getName() {
		return ChooseRulePlugin.PLUGIN_NAME;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new ChooseRuleHandler(), CodeType.U, "Rule", "ChooseRule", null);
		register(new PickRuleHandler(), CodeType.R, "Expression", "PickExp", null);
	}
}
