package org.coreasm.compiler.interfaces;

import org.coreasm.compiler.codefragment.CodeFragment;

/**
 * Interface for a plugin providing initialization code.
 * Initialization code is added at the very beginning of the runMachine
 * method of the main class.
 * <p>
 * It can be used to initialize function global objects with a specific constructor,
 * that cannot be initialized as a class provided by the vocabulary extender plugin.
 * <p>
 * Use with care, as initialization code can easily brick the whole main class
 * @author Markus Brenner
 *
 */
public interface CompilerInitCodePlugin extends CompilerPlugin{
	/**
	 * Provides the initialization code of this plugin.
	 * @return A piece of initialization code.
	 */
	public CodeFragment getInitCode();
}
