package org.coreasm.compiler.plugins.chooserule;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.chooserule.ChooseRulePlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.chooserule.code.rcode.PickRuleHandler;
import org.coreasm.compiler.plugins.chooserule.code.ucode.ChooseRuleHandler;

/**
 * Provides the choose rule.
 * The choose rule allows to execute a rule with a randomly choosen
 * variable assignment.
 * The full form is:
 * choose x1 in l1, x2 in l2, ... with guard do R1 ifnone R2
 * If any of the lists l1...ln is empty or the guard cannot be fulfilled
 * with any assignment for x1...xn, the ifnone rule is executed with
 * x1 = x2...=xn = undef
 * 
 * This plugin also provides the pick expression, which picks a random value
 * out of an {@link Enumerable}
 * @author Spellmaker
 *
 */
public class CompilerChooseRulePlugin extends CompilerCodePlugin implements CompilerPlugin{

	private Plugin interpreterPlugin;
	
	/**
	 * Initializes a new plugin
	 * @param parent The interpreter version
	 */
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

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}
}
