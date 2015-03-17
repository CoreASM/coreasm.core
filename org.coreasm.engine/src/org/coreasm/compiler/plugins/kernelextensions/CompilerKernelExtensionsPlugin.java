package org.coreasm.compiler.plugins.kernelextensions;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.kernelextensions.code.ucode.CompilerExtendedFunctionRuleTermHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.kernelextensions.KernelExtensionsPlugin;

/**
 * Provides extended rule call possibilities.
 * The current version does not completely implement all
 * operations of the interpreter plugin
 * @author Spellmaker
 *
 */
public class CompilerKernelExtensionsPlugin extends CompilerCodePlugin implements CompilerPlugin {
	private Plugin parent;
	
	/**
	 * Constructs a new plugin
	 * @param parent The interpreter version
	 */
	public CompilerKernelExtensionsPlugin(Plugin parent) {
		this.parent = parent;
	}
	
	@Override
	public String getName() {
		return KernelExtensionsPlugin.PLUGIN_NAME;
	}

	@Override
	public Plugin getInterpreterPlugin() {
		return parent;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		//TODO: Write missing code handlers for the kernel extensions plugin
		register(new CompilerExtendedFunctionRuleTermHandler(), CodeType.R, null, "ExtendedFunctionRuleTermNode", null);
		
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}

}
