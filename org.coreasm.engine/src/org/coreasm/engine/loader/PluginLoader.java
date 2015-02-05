package org.coreasm.engine.loader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginLoader {
	private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);
	public static final String PLUGIN_CLASSPATH_PROPERTY_NAME = "classpath";
	public static final String PLUGIN_CLASSPATH_SEPARATOR = ":";
	public static final String PLUGIN_ID_FILE_NAME = "CoreASMPlugin.id";
	public static final String PLUGIN_ID_PROPERTY_NAME = "mainclass";
	
	//Constants for plugin loading
	public static final String PLUGIN_PROPERTIES_FILE_NAME = "CoreASMPlugin.properties";
	
	/** Map of available plugin names to available plugins. */
	private Map<String, Plugin> allPlugins;
	/** List of operator rules gathered from plugins */
	private ArrayList<OperatorRule> operatorRules = null;

	private ControlAPI capi;
	/** An optional class loader used to load plugins */
	private ClassLoader classLoader = null;
	/** Set of loaded plugins */
	private PluginDB loadedPlugins;
	
	public PluginLoader(ControlAPI capi){
		init(capi, null);
	}
	
	public PluginLoader(ControlAPI capi, ClassLoader classLoader){
		init(capi, classLoader);
	}
	
	public void clear(){
		loadedPlugins.clear();
		operatorRules.clear();
		//not sure yet, appearently the original code never
		//cleared the allplugins mapping
		//allPlugins.clear();
	}
	
	/**
	 * Loads plugin catalog. This method looks for all available plugins, and
	 * creates a map of plugin names to plugin objects. This method does NOT
	 * initialize any plugin.
	 *
	 * @throws IOException
	 *
	 */
	public void loadCatalog() throws IOException {
		logger.debug(
				"Loading plugin catalog...");

		try {
			loadCatalog(getClass(), "plugins/");
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// looking to the extended plugin folders
		String pluginFolders = capi.getProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY);
		if (pluginFolders != null) {
			for (String pluginFolder : Tools.tokenize(pluginFolders, EngineProperties.PLUGIN_FOLDERS_DELIM)) {
				File folder = new File(pluginFolder);
				if (folder.isDirectory()) {
					for (File file : folder.listFiles())
						loadPluginClasses(file);
				}
			}
		}
	}

	private void loadCatalog(Class<?> clazz, String dir) throws URISyntaxException, IOException {
		URL dirURL = clazz.getClassLoader().getResource(dir);
		//1st case: file
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			/* A file path: easy enough */
			File[] files = new File(dirURL.toURI()).listFiles();
			for (File file : files)
				loadPluginClasses(file);
		}
		else {
			if (dirURL == null) {
				/*
				 * In case of a jar file, we can't actually find a directory.
				 * Have to assume the same jar as clazz.
				 */
				String me = clazz.getName().replace(".", "/") + ".class";
				dirURL = clazz.getClassLoader().getResource(me);
			}
	
			//2nd case: jar
			if (dirURL.getProtocol().equals("jar")) {
				/* A JAR path */
				String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
				JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
				Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				Set<JarEntry> result = new HashSet<JarEntry>(); //avoid duplicates in case it is a subdirectory
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.startsWith(dir) && !name.substring(dir.length()).contains("/")) { //filter according to the path
						if (result.add(entry))
							loadPluginClasses(jar, entry);
					}
				}
			}
			//3rd case: bundle resource
			else if (dirURL.getProtocol().equals("bundleresource")) {
				/* A JAR path */
				if (System.getProperty(Tools.COREASM_ENGINE_LIB_PATH) == null)
					throw new NullPointerException("path to engine library has not been set.");
				JarFile jar = new JarFile(System.getProperty(Tools.COREASM_ENGINE_LIB_PATH));
				Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				Set<JarEntry> result = new HashSet<JarEntry>(); //avoid duplicates in case it is a subdirectory
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.startsWith(dir) && !name.substring(dir.length()).contains("/")) { //filter according to the path
						if (result.add(entry))
							loadPluginClasses(jar, entry);
					}
				}
			}
			else
				throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
		}
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
	
	/*
	 * Loads a single plugin
	 */
	private void loadPluginClasses(File file) {
		try {
			// checking if the path points to a directory (folder)
			if (file.isDirectory())
				DirectoryLoader.loadPluginClassesFromDirectory(file, this);
			else
			// checking if the path points to a ZIP file
			if (file.getName().toUpperCase().endsWith(".ZIP"))
				ZipLoader.loadPluginClassesFromZipFile(file);
			else
			// checking if the path points to a JAR file
			if (file.getName().toUpperCase().endsWith(".JAR"))
				JarLoader.loadPluginClassesFromJarFile(file, this);
			else
				throw new EngineException("Cannot detect plugin.");
		} catch (EngineException e) {
			logger.error("Cannot load plugin '{}'. Skipping this plugin. Error: {}", file.getName(), e.getMessage());
		}
	}
	
	/*
	 * Loads a single plugin
	 */
	private void loadPluginClasses(JarFile jar, JarEntry entry) {
		try {
			// checking if the path points to a directory (folder)
			if (entry.isDirectory())
				;
			//				loadPluginClassesFromDirectory(entry);
			else
			// checking if the path points to a ZIP file
			if (entry.getName().toUpperCase().endsWith(".ZIP"))
				ZipLoader.loadPluginClassesFromZipFile(entry);
			else
			// checking if the path points to a JAR file
			if (entry.getName().toUpperCase().endsWith(".JAR"))
				JarLoader.loadPluginClassesFromJarFile(jar, entry, this);
			else
				throw new EngineException("Cannot detect plugin.");
		}
		catch (EngineException e) {
			logger.error("Cannot load plugin '{}'. Skipping this plugin. Error: {}", entry.getName(), e.getMessage());
		}
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

	/*
	 * Loads a single plugin class from the given list of resources.
	 */
	public void loadPlugin(String pName, String className, File... resources) throws EngineException {
		URL[] urls = new URL[resources.length];
		try {
			for (int i=0; i < resources.length; i++)
				urls[i] = resources[i].toURI().toURL();
		} catch (MalformedURLException e) {
			throw new EngineException("Cannot locate plugin.");
		}
		loadPlugin(pName, className, urls);
	}

	/*
	 * Loads a single plugin class from the given list of resources.
	 */
	public void loadPlugin(String pName, String className, URL... urls) throws EngineException {
		URLClassLoader loader = null;
		if (this.classLoader == null)
			loader = new URLClassLoader(urls);
		else
			loader = new URLClassLoader(urls, this.classLoader);

		Object o = null;
		Class<?> pc = null;
		try {
			logger.debug( "Loading plugin: {}", className);
			pc = loader.loadClass(className);
			/*
			MinimumEngineVersion minVersion = (MinimumEngineVersion)pc.getAnnotation(MinimumEngineVersion.class);
			Object[] annots = pc.getAnnotations();
			if (minVersion != null) {
				VersionInfo vreq = VersionInfo.valueOf(minVersion.value());
				if (vreq != null && vreq.compareTo(this.VERSION_INFO) > 0) {
					Logger.log(Logger.ERROR, Logger.controlAPI,
							"Cannot load plugin '" + fullName +
							"' as it is built for a more recent version" +
							"of the engine (" + vreq + " and above). Skipping this plugin.");
					continue;
				}
			}
			*/
			o = pc.newInstance();
		} catch (Exception e) {
			logger.error("Cannot load plugin '{}'. Skipping this plugin. Error: {}", pName, e.getMessage());
			return;
		}
		if (o instanceof Plugin) {
			Plugin p = (Plugin) o;
			p.setControlAPI(this.capi);
			logger.debug("Plugin '{}' is usable.", p.getName());
			allPlugins.put(p.getName(), p);
		} else
			logger.error(
						"Invalid plugin '{}'. This class does not extend the CoreASM Plugin class.", className);
	}

	public Collection<? extends OperatorRule> getOperatorRules() {
		List<OperatorRule> ops = operatorRules;
		operatorRules = new ArrayList<OperatorRule>();
		return ops;
	}
}
