package org.coreasm.engine.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.EngineProperties;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginManager {
	private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);
	/** Map of available plugin names to available plugins. */
	private Map<String, Plugin> allPlugins;
	/** List of operator rules gathered from plugins */
	private ArrayList<OperatorRule> operatorRules = null;

	private ControlAPI capi;
	/** An optional class loader used to load plugins */
	private ClassLoader classLoader = null;
	/** Set of loaded plugins */
	private PluginDB loadedPlugins;
	
	public PluginManager(ControlAPI capi){
		init(capi, null);
	}
	
	public PluginManager(ControlAPI capi, ClassLoader classLoader){
		init(capi, classLoader);
	}
	
	public void clear(){
		loadedPlugins.clear();
		operatorRules.clear();
		//not sure yet, apparently the original code never
		//cleared the allplugins mapping
		//allPlugins.clear();
	}
	
	public void loadCatalog() throws IOException{
		allPlugins = PluginClassLoader.loadCatalog(capi);
	}
	
	
	
	public ClassLoader getClassLoader(){
		return this.classLoader;
	}
	
	public PluginDB getLoadedPlugins(){
		return loadedPlugins;
	}
	
	public Plugin getPlugin(String name){
		Plugin result = allPlugins.get(name);
		if(result == null) result = allPlugins.get(name + "Plugin");
		if(result == null) result = allPlugins.get(name + "Plugins");
		return result;
	}
	
	public Set<Plugin> getPlugins(){
		return new HashSet<Plugin>(loadedPlugins);
	}
	
	public boolean hasLoadedPlugins(){
		return loadedPlugins.size() > 0;
	}
	
	/**
	 * Loads core plugins.
	 */
	public void loadCorePlugins() {
		logger.debug(
				"Load Core Plugins is called.");

		// Explicitly loading the kernel plugin
		Kernel kernelPlugin = new Kernel();
		logger.debug(
				"Kernel Plugin loaded.");
		allPlugins.put(kernelPlugin.getName(), kernelPlugin); // get plugin
																// uses
																// "allPlugins"
																// hash so add
																// the plugin
																// there
		loadPlugin(kernelPlugin); // plugin is initialized and goes into
								  // "loadedPlugins" collection
	}
	
	/**
	 * Loads plugins identified by the current specification.
	 */
	public void loadSpecPlugins() {
		final List<Plugin> sortedList = new ArrayList<Plugin>();
		
		// 1. get the list of plugins
		final Collection<String> requiredPlugins = new ArrayList<String>(capi.getSpec().getPluginNames());//getSpecPlugins());

		// 2. sort plugins
		for (String s : requiredPlugins) {
			Plugin p = allPlugins.get(s);
			if (p == null)
				throw new EngineError("Cannot load plugin: " + s);
			sortedList.add(p);
		}

		Collections.sort(sortedList, new Comparator<Plugin>() {
			@Override
			public int compare(Plugin o1, Plugin o2) {
				Plugin p1 = (Plugin)o1;
				Plugin p2 = (Plugin)o2;

				if (p1.getLoadPriority() < p2.getLoadPriority())
					return -1;
				else
					if (p1.getLoadPriority() > p2.getLoadPriority())
						return 1;
					else
						return 0;
			}
		});

		// 3. load plugins
		for (Plugin p : sortedList) {
			if (checkPluginDependency(requiredPlugins, p))
				loadPlugin(p);
			else
				break;
		}

	}

	public void setClassLoader(ClassLoader loader){
		this.classLoader = loader;
		PluginClassLoader.classLoader = loader;
	}
	private void init(ControlAPI capi, ClassLoader classLoader){
		this.capi = capi;
		this.classLoader = classLoader;
		this.allPlugins = new HashMap<String, Plugin>();
		this.loadedPlugins = new PluginDB();
		this.operatorRules = new ArrayList<OperatorRule>();
	}
	/*
	 * Checks plugin dependency and set the engine in error mode
	 * if dependency is not satisfied.
	 */
	private boolean checkPluginDependency(Collection<String> usedPlugins, Plugin p) {
		// get all the dependency requirements
		Map<String,VersionInfo> depends = p.getDependencies();

		if (depends != null) {
			// for every plugin in the dependency list
			for (String name : depends.keySet()) {

				// first check if it is listed in the used clause
				if (usedPlugins.contains(name)) {
					// second, see if its version info is equal or
					// grater than what is required
					if (allPlugins.get(name).getVersionInfo().compareTo(depends.get(name)) >= 0)
						continue;

				}
				capi.error(new EngineException(
							"Plugin Dependency Error: " + p.getName()
							+ " requires " + name + " version " + depends.get(name) + " or higher."));
				return false;
			}
		}
		return true;
	}

	/*
	 * Gets the list of plugins that are requested to be loaded by the
	 * engine environment.
	 *
	 * @see EngineProperties.PLUGIN_LOAD_REQUEST
	 */
	public Collection<String> getRequestedPlugins() {
		Collection<String> result = new HashSet<String>();
		String rpList = capi.getProperty(EngineProperties.PLUGIN_LOAD_REQUEST_PROPERTY);
		if (rpList != null) {
			StringTokenizer tokenizer = new StringTokenizer(rpList, EngineProperties.PLUGIN_LOAD_REQUEST_DELIM);
			while (tokenizer.hasMoreTokens())
				result.add(tokenizer.nextToken());
		}
		return result;
	}

	/**
	 * Loads a specific plugin. This involves adding the plugin to the set of
	 * loaded plugins and initializing it.
	 *
	 * @param p
	 *            the <code>Plugin</code> object
	 */
	private void loadPlugin(Plugin p) {
		// Don't load the plugin if it is already loaded
		if (loadedPlugins.contains(p))
			return;

		logger.debug( "initializing {}...",  p.getName());

		try {
			// this plugin is associated with this engine
			// instance
			p.initialize(this.capi);
		} catch (InitializationFailedException e) {
			logger.error( e.getMessage());
			throw new EngineError(e.getMessage());
		}

		// if plugin implements operators, get operator rules
		if (p instanceof OperatorProvider)
			operatorRules.addAll(((OperatorProvider) p).getOperatorRules());

		loadedPlugins.add(p);
	}

	public Collection<? extends OperatorRule> getOperatorRules() {
		List<OperatorRule> ops = operatorRules;
		operatorRules = new ArrayList<OperatorRule>();
		return ops;
	}
}
