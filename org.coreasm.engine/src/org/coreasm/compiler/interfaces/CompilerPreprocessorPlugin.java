package org.coreasm.compiler.interfaces;

import java.util.List;
import java.util.Map;

import org.coreasm.compiler.preprocessor.InheritRule;
import org.coreasm.compiler.preprocessor.SynthesizeRule;

/**
 * A compiler preprocessor plugin provides components for the preprocessor analysis.
 * The plugin developer has to ensure that rules do not create cycles.
 * @author Markus Brenner
 *
 */
public interface CompilerPreprocessorPlugin {
	/**
	 * Provides the synthesize rules of this plugin
	 * @return A list of synthesize rules
	 */
	public List<SynthesizeRule> getSynthesizeRules();
	/**
	 * Provides the inheritance rules of this plugin
	 * @return A list of inheritance rules
	 */
	public List<InheritRule> getInheritRules();
	/**
	 * Provides all default synthesize rules
	 * @return A mapping of default synthesize rules for informations
	 */
	public Map<String, SynthesizeRule> getSynthDefaultBehaviours();
	/**
	 * Provides all default inheritance rules
	 * @return A mapping of default inheritance rules for informations
	 */
	public Map<String, InheritRule> getInheritDefaultBehaviours();
}
