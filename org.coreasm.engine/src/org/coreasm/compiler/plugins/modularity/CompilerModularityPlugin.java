package org.coreasm.compiler.plugins.modularity;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.engine.plugin.Plugin;

public class CompilerModularityPlugin extends CompilerCodePlugin implements
		CompilerPlugin {

	private Plugin parent;
	
	public CompilerModularityPlugin(Plugin parent) {
		this.parent = parent;
	}
	
	@Override
	public String getName() {
		return parent.getName();
	}

	@Override
	public Plugin getInterpreterPlugin() {
		return parent;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		this.register(new ModularityDummyHandler(), null, null, null, null);
	}

}
