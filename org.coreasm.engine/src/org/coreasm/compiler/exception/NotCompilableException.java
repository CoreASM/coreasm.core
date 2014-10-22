package org.coreasm.compiler.exception;

import java.util.List;

/**
 * Thrown, when a plugin used by a specification does not implement CompilerPlugin
 * @author Markus Brenner
 *
 */
public class NotCompilableException extends Exception {
	private static final long serialVersionUID = -2956569025051079022L;
	private List<String> plugin;
	
	/**
	 * Creates a new Exception, specifying the error source
	 * @param pluginsrc The uncompilable plugin name
	 */
	public NotCompilableException(List<String> pluginsrc){
		this.plugin = pluginsrc;
	}
	
	/**
	 * Provides access to the error source
	 * @return The name of the faulty plugin
	 */
	public List<String> getPlugins(){
		return plugin;
	}
}
