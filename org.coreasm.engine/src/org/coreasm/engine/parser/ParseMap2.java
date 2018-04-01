package org.coreasm.engine.parser;

import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugin.Plugin;

import java.util.function.BiFunction;

/** 
 * Specialized version that gets a plug-in name as well.
 *   
 * @author Roozbeh Farahbod
 * 
 */

public abstract class ParseMap2 implements BiFunction<Node, Node, Node> {

	public final String pluginName;
	
	public ParseMap2(String pluginName) {
		this.pluginName = pluginName;
	}

	public ParseMap2(Plugin plugin) {
		this.pluginName = plugin.getName();
	}
}

