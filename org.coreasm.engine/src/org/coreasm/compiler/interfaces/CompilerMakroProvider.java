package org.coreasm.compiler.interfaces;

import java.util.Map;

import org.coreasm.compiler.codefragment.CodeFragment;

/**
 * Plugin providing global makros to the compilation process.
 * Global makros will be applied to all {@link CodeFragment} instances
 * upon code generation.
 * Providing an already defined makro will not result in an error, but
 * in a warning.
 * @author Spellmaker
 *
 */
public interface CompilerMakroProvider {
	/**
	 * Gets a map of makros provided by this plugin
	 * @return A mapping of makro to plaintext
	 */
	public Map<String, String> getMakros();
}
