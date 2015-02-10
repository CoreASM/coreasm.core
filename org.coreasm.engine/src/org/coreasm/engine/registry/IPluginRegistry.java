/*
 * Copyright (C) 2005-2012 Roozbeh Farahbod 
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.engine.registry;

import java.util.Collection;

/**
 * The interface to the plugin registry of CoreASM.
 *
 * @author Roozbeh Farahbod
 */
public interface IPluginRegistry {

	/**
	 * Returns the set of all available plugins.
	 * 
	 * @return the set of all available plugins
	 */
	public Collection<ICoreASMPlugin> getPlugins();
	
	/**
	 * Returns the loaded plugin with the given name.
	 * 
	 * @param name name of the plugin
	 * @return the plugin instance
	 */
	public ICoreASMPlugin getPlugin(String name);
}
