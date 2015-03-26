package org.coreasm.compiler.components.preprocessor;

/**
 * Stores information about how a parse tree node needs to look like for something to happen.
 * @author Markus Brenner
 *
 */
public class Trigger {
	/**
	 * The plugin name or null, if the field is ignored
	 */
	public String plugin;
	/**
	 * The grammar class or null, if the field is ignored
	 */
	public String grammarClass;
	/**
	 * The grammar rule or null, if the field is ignored
	 */
	public String grammarRule;
	/**
	 * The token or null, if the field is ignored
	 */
	public String token;
	/**
	 * Initializes a new trigger
	 * @param plugin The plugin name or null, if the field is ignored
	 * @param grammarClass The grammar class or null, if the field is ignored
	 * @param grammarRule The grammar rule or null, if the field is ignored
	 * @param token The token or null, if the field is ignored
	 */
	public Trigger(String plugin, String grammarClass, String grammarRule, String token){
		this.plugin = plugin;
		this.grammarClass = grammarClass;
		this.grammarRule = grammarRule;
		this.token = token;
	}
}
