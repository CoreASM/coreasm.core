/*
 * Copyright (C) 2005-2012 Roozbeh Farahbod 
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.engine.registry;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.Configuration;

import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default implementation of the {@link IPluginRegistry} offering a plugin registry for CoreASM.
 * 
 * TODO: This class is not yet used by the engine.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class PluginRegistry implements IPluginRegistry {

	private static Logger logger = LoggerFactory.getLogger(PluginRegistry.class);

	public static final String DEFAULT_PLUGINS_FOLDER_IN_CLASSPATH = "plugins";

	private static IPluginRegistry SINGLETON = null;

	/** Holds a mapping of plugin names to plugins. */
	private Map<String, ICoreASMPlugin> plugins = new HashMap<String, ICoreASMPlugin>();

	private boolean initialized = false;

	/**
	 * Returns a singleton instance of the extension registry.
	 */
	public static IPluginRegistry getSingleton() {
		if (SINGLETON == null)
			SINGLETON = new PluginRegistry();
		return SINGLETON;
	}
	
	@Override
	public Collection<ICoreASMPlugin> getPlugins() {
		Collection<ICoreASMPlugin> result = plugins.values();
		
		if (result != null) {
			return Collections.unmodifiableCollection(result);
		} else
			return Collections.emptySet();
		
	}

	@Override
	public ICoreASMPlugin getPlugin(String name) {
		return plugins.get(name);
	}

	private PluginRegistry() {
		initialize();
	}

	/**
	 * Initializes the plugin registry.
	 */
	private void initialize() {
		if (!initialized) {
			loadPlugins();
			initialized = true;
		} else
			logger.warn("Plugin registry cannot be re-initialized.");
	}

	/**
	 * Loads all the plugins that are available.
	 */ 
	@SuppressWarnings("rawtypes")
	private void loadPlugins() {
		logger.info("Loading CoreASM plugins...");
		
		throw new IllegalAccessError("The new CoreASM plugin registry is still under development.");
	}

}
