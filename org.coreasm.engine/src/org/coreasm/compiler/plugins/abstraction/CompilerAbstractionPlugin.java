package org.coreasm.compiler.plugins.abstraction;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.abstraction.AbstractionPlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.abstraction.code.ucode.AbstractionAbstractHandler;

/**
 * Provides the abstract rule.
 * The abstract rule allows the user to leave out an actual implementation.
 * @author Markus Brenner
 *
 */
public class CompilerAbstractionPlugin extends CompilerCodePlugin implements CompilerPlugin {

	private Plugin interpreterPlugin;
	
	public CompilerAbstractionPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}
	
	@Override
	public String getName() {
		return AbstractionPlugin.PLUGIN_NAME;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new AbstractionAbstractHandler(), CodeType.U, "Rule", "AbstractRule", null);
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}
}
