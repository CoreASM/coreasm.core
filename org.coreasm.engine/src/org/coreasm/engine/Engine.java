/*
 * Engine.java
 *
 * Copyright (C) 2005-2012 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.HashStorage;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.State;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterImp;
import org.coreasm.engine.interpreter.InterpreterListener;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.JParsecParser;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.Parser;
import org.coreasm.engine.parser.ParserException;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.PackagePlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.PluginServiceInterface;
import org.coreasm.engine.plugin.ServiceProvider;
import org.coreasm.engine.plugin.ServiceRequest;
import org.coreasm.engine.scheduler.Scheduler;
import org.coreasm.engine.scheduler.SchedulerImp;
import org.coreasm.util.Tools;

/**
 * This class provides the actual implementation of a CoreASM engine. It
 * implements {@link ControlAPI} and has
 * four components: a {@link Parser}, a {@link Scheduler}, an
 * {@link Interpreter}, and an {@link AbstractStorage}.
 * 
 * @author Roozbeh Farahbod, Michael Stegmaier, Marcel Dausend
 * 
 */
public class Engine implements ControlAPI {

	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 6, 4, "beta");

	private static final Logger logger = LoggerFactory.getLogger(Engine.class);

	private static final String PLUGIN_PROPERTIES_FILE_NAME = "CoreASMPlugin.properties";
	private static final String PLUGIN_ID_FILE_NAME = "CoreASMPlugin.id";
	private static final String PLUGIN_ID_PROPERTY_NAME = "mainclass";
	private static final String PLUGIN_CLASSPATH_PROPERTY_NAME = "classpath";
	private static final String PLUGIN_CLASSPATH_SEPARATOR = ":";

	/** Unique name of the engine. */
	private final String name;

	/** Engine components */
	private final Parser parser;

	private final AbstractStorage storage;

	private final Scheduler scheduler;

	private final Interpreter interpreter;

	/** Map of available plugin names to available plugins. */
	private Map<String, Plugin> allPlugins;

	/** Set of loaded plugins */
	private PluginDB loadedPlugins;

	/** List of grammar rules gathered from plugins */
	private ArrayList<GrammarRule> grammarRules = null;

	/** List of operator rules gathered from plugins */
	private ArrayList<OperatorRule> operatorRules = null;

	/** Last engine identification number */
	private static long lastEngineId = 0;

	/** The execution thread of the engine */
	private final Thread engineThread;

	/** Mode of the engine */
	private volatile EngineMode engineMode = EngineMode.emIdle;

	/** A flag which is on while engine is busy */
	private volatile boolean engineBusy = false;

	/** Cache of EngineMode events */
	private Map<EngineMode, Map<EngineMode, EngineModeEvent>> modeEventCache;

	/** CoreASM Plugin Service Registry */
	private Map<String, Set<ServiceProvider>> serviceRegistry;

	/** User command queue */
//	private Queue<EngineCommand> commandQueue;
	private CommandQueue commandQueue;

	/** Properties of the engine */
	private EngineProperties properties;

	/** Collection of registered observers */
	private Collection<EngineObserver> observers;

	/** List of interpreter listeners */
	private LinkedList<InterpreterListener> interpreterListeners;

	/** Remaining steps of the current run */
	private int remainingRunCount = 0;

	/** Last error occurred in the engine */
	private volatile CoreASMError lastError = null;

	/** An optional class loader used to load plugins */
	private ClassLoader classLoader = null;

	/* the latest loaded specification */
	private Specification specification = null;

	/* Last processed engine command */
	private EngineCommand lastCommand = null;

	private boolean isStateInitialized = false;

	private List<CoreASMWarning> warnings;

	/**
	 * Constructs a new CoreASM engine with the specified properties. This is
	 * mainly for future extensions.
	 *
	 * This method is <code>protected</code>.
	 *
	 * @param properties
	 *            properties of the engine to be created.
	 */
	protected Engine(java.util.Properties properties) {
		name = "CoreASM" + Tools.lFormat(++lastEngineId, 5);
		this.properties = new EngineProperties();
		if (properties != null)
			this.properties.putAll(properties);

		storage = new HashStorage(this);
		scheduler = new SchedulerImp(this);
		parser = new JParsecParser(this);
		interpreter = new InterpreterImp(this);
		engineThread = new EngineThread(name);

		// initializing objects
		commandQueue = new CommandQueue();
		allPlugins = new HashMap<String, Plugin>();
		loadedPlugins = new PluginDB();
		grammarRules = new ArrayList<GrammarRule>();
		operatorRules = new ArrayList<OperatorRule>();
		engineMode = EngineMode.emIdle;
		observers = new HashSet<EngineObserver>();
		interpreterListeners = new LinkedList<InterpreterListener>();
		modeEventCache = new HashMap<EngineMode, Map<EngineMode,EngineModeEvent>>();
		specification = null;
		warnings = new ArrayList<CoreASMWarning>();
		serviceRegistry = new HashMap<String, Set<ServiceProvider>>();

		// Starting the execution thread of the engine
		engineThread.start();
	}

	/**
	 * The default constructor to create a new CoreASM engine. This method calls
	 * <code>Engine(null)</code>.
	 *
	 * This method is <code>protected</code>.
	 *
	 * @see #Engine(java.util.Properties)
	 */
	protected Engine() {
		this(null);
	}

	/**
	 * Resets the engine and its component, making it ready
	 * to load a new specification.
	 */
	private void resetEngine() {
		if (loadedPlugins.size() > 1) {
			serviceRegistry = new HashMap<String, Set<ServiceProvider>>();
			loadedPlugins.clear();
			grammarRules.clear();
			operatorRules.clear();
			isStateInitialized = false;
			lastError = null;
			warnings.clear();
			loadCorePlugins();
		}
	}

	@Override
	public void initialize() {
		commandQueue
				.add(new EngineCommand(EngineCommand.CmdType.ecInit, null));
	}

	@Override
	public void terminate() {
		commandQueue.add(new EngineCommand(EngineCommand.CmdType.ecTerminate,
				null));
	}

	@Override
	public void recover() {
		if (getEngineMode() == EngineMode.emError)
			commandQueue.add(new EngineCommand(EngineCommand.CmdType.ecRecover,
					null));
	}

	@Override
	public void loadSpecification(String specFileName) {
		commandQueue.add(new EngineCommand(EngineCommand.CmdType.ecLoadSpec,
				specFileName));
	}

	@Override
	public void loadSpecification(Reader src) {
		loadSpecification("CoreASM Specification", src);
	}

	@Override
	public void loadSpecification(String name, Reader src) {
		commandQueue.add(new EngineCommand(EngineCommand.CmdType.ecLoadSpec,
				new NamedStringReader(name, src)));
	}

	@Override
	public void parseSpecification(String specFileName) {
		commandQueue.add(new EngineCommand(EngineCommand.CmdType.ecOnlyParseSpec,
				specFileName));
	}

	@Override
	public void parseSpecification(Reader src) {
		parseSpecification("CoreASM Specification", src);
	}

	@Override
	public void parseSpecification(String name, Reader src) {
		commandQueue.add(new EngineCommand(EngineCommand.CmdType.ecOnlyParseSpec,
				new NamedStringReader(name, src)));
	}

	@Deprecated
	@Override
	public void parseSpecificationHeader(String specFileName) {
		parseSpecificationHeader(specFileName, true);
	}

	@Deprecated
	@Override
	public void parseSpecificationHeader(Reader src) {
		parseSpecificationHeader("CoreASM Specification", src, true);
	}

	@Deprecated
	@Override
	public void parseSpecificationHeader(String name, Reader src) {
		parseSpecificationHeader(name, src, true);
	}

	@Override
	public void parseSpecificationHeader(String specFileName, boolean loadPlugins) {
		commandQueue.add(new EngineCommand(EngineCommand.CmdType.ecOnlyParseHeader,
				new ParseCommandData(loadPlugins, specFileName)));
	}

	@Override
	public void parseSpecificationHeader(Reader src, boolean loadPlugins) {
		parseSpecificationHeader("CoreASM Specification", src, loadPlugins);
	}

	@Override
	public void parseSpecificationHeader(String name, Reader src, boolean loadPlugins) {
		commandQueue.add(new EngineCommand(EngineCommand.CmdType.ecOnlyParseHeader,
				new ParseCommandData(loadPlugins, new NamedStringReader(name, src))));
	}

	@Override
	public Specification getSpec() {
		return specification;
	}

	/*
	public Specification getSpec() {
		String[] text = {};
		text = (String[]) parser.getSpecificationLines().toArray(text);

		ASTNode root = parser.getRootNode();
		String name = "";

		if (root != null) {
			if (root.getFirst() != null) {
				name = root.getFirst().getToken();
			}
		}

		try {
			return new Specification(name, text, root);
		} catch (NullPointerException e) {
			return null;
		}
	}
	*/

	/**
	 * Returns the current state of the engine (after the last computation
	 * step). The state is not cloned.
	 *
	 * @see ControlAPI#getState()
	 */
	@Override
	public State getState() {
		return storage;
	}

	/**
	 * This method is not supported by this engine.
	 *
	 * @throws UnsupportedOperationException
	 * @see ControlAPI#getPrevState(int)
	 */
	@Override
	public State getPrevState(int i) {
		throw new UnsupportedOperationException(
				"State history is not supported yet.");
	}

	@Override
	public Set<Update> getUpdateSet(int i) {
		if (i == 0)
			return scheduler.getUpdateSet();
		else
			return null;
	}

	@Override
	public Set<? extends Element> getAgentSet() {
		return scheduler.getAgentSet();
	}

	@Override
	public UpdateMultiset getUpdateInstructions() {
		return scheduler.getUpdateInstructions();
	}

	@Override
	public void updateState(Set<Update> update)
			throws InconsistentUpdateSetException, InvalidLocationException {
		if (getEngineMode() == EngineMode.emIdle)
			if (storage.isConsistent(update))
				storage.fireUpdateSet(update);
			else
				throw new InconsistentUpdateSetException();
		else
			logger.error("Cannot update engine state when engine is not in the idle mode.");
	}

	/**
	 * Returns properties of the engine. The returned object is a (shallow)
	 * clone of engine properties, so changing its structure will not change the
	 * properties of the engine.
	 *
	 * @see ControlAPI#getProperties()
	 */
	@Override
	public Properties getProperties() {
		return (Properties) properties.clone();
	}

	/**
	 * Sets new properties for the engine based on the given properties. This
	 * method uses a shallow clone of the given properties.
	 *
	 * @see ControlAPI#setProperties(Properties)
	 */
	@Override
	public void setProperties(Properties newProperties) {
		if (getEngineMode() == EngineMode.emIdle)
			this.properties = new EngineProperties(newProperties);
		else
			logger.error("Cannot change engine properties when engine is not in the idle mode.");
	}

	@Override
	public String getProperty(String property) {
		return properties.getProperty(property);
	}

	@Override
	public String getProperty(String property, String defaultValue) {
		return properties.getProperty(property, defaultValue);
	}

	@Override
	public void setProperty(String property, String value) {
		//if (getEngineMode() == EngineMode.emIdle)
		properties.setProperty(property, value);
		//else
		//	Logger
		//			.log(Logger.ERROR, Logger.controlAPI,
		//					"Cannot change engine properties when engine is not in the idle mode.");
	}

	@Override
	public boolean propertyHolds(String property) {
		String  value = properties.getProperty(property);
		if (value != null)
			return value.equals(EngineProperties.YES);
		else
			return false;
	}

	@Override
	public EngineMode getEngineMode() {
		return engineMode;
	}

	@Override
	public void hardInterrupt() {
		engineThread.interrupt();
	}

	@Override
	public void softInterrupt() {
		synchronized (engineThread) {
			remainingRunCount = 0;
		}
	}

	@Override
	public void step() {
		commandQueue
				.add(new EngineCommand(EngineCommand.CmdType.ecStep, null));
	}

	@Override
	public void run(int i) {
		commandQueue.add(new EngineCommand(EngineCommand.CmdType.ecRun,
				new Integer(i)));
	}

	@Override
	public void addObserver(EngineObserver observer) {
		synchronized (observers) {
			observers.add(observer);
		}
	}

	@Override
	public void removeObserver(EngineObserver observer) {
		synchronized (observers) {
			observers.remove(observer);
		}
	}

	/**
	 * Returns a copy of the collection of observers in this engine.
	 *
	 * @see ControlAPI#getObservers()
	 */
	@Override
	public Collection<EngineObserver> getObservers() {
		return new HashSet<EngineObserver>(observers);
	}

	/**
	 * Initializes the kernel.
	 */
	private void initKernel() {
		allPlugins.clear();
		loadedPlugins.clear();
		grammarRules.clear();
		operatorRules.clear();
		specification = null;
		isStateInitialized = false;
	}

	/**
	 * Loads plugin catalog. This method looks for all available plugins, and
	 * creates a map of plugin names to plugin objects. This method does NOT
	 * initialize any plugin.
	 *
	 * @throws IOException
	 *
	 */
	private void loadCatalog() throws IOException {
		logger.debug(
				"Loading plugin catalog...");

		try {
			loadCatalog(getClass(), "plugins/");
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// looking to the extended plugin folders
		String pluginFolders = getProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY);
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

	/*
	 * Loads a single plugin
	 */
	private void loadPluginClasses(File file) {
		try {
			// checking if the path points to a directory (folder)
			if (file.isDirectory())
				loadPluginClassesFromDirectory(file);
			else
			// checking if the path points to a ZIP file
			if (file.getName().toUpperCase().endsWith(".ZIP"))
				loadPluginClassesFromZipFile(file);
			else
			// checking if the path points to a JAR file
			if (file.getName().toUpperCase().endsWith(".JAR"))
				loadPluginClassesFromJarFile(file);
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
				loadPluginClassesFromZipFile(entry);
			else
			// checking if the path points to a JAR file
			if (entry.getName().toUpperCase().endsWith(".JAR"))
				loadPluginClassesFromJarFile(jar, entry);
			else
				throw new EngineException("Cannot detect plugin.");
		}
		catch (EngineException e) {
			logger.error("Cannot load plugin '{}'. Skipping this plugin. Error: {}", entry.getName(), e.getMessage());
		}
	}

	/*
	 * Loads a single plugin from a directory
	 */
	private void loadPluginClassesFromDirectory(File file) throws EngineException {
		String[] contents = file.list();
		if (contents == null)
			throw new EngineException("Plugin folder is empty.");
		else {
			// if there is a properties file
			if (Tools.find(PLUGIN_PROPERTIES_FILE_NAME, contents) > -1) {
				Properties properties = new Properties();

				// load the properties
				try {
					properties.load(
							new FileInputStream(
									file.getAbsolutePath() + File.separator + PLUGIN_PROPERTIES_FILE_NAME
									)
							);
				} catch (IOException e) {
					throw new EngineException("Cannot load plugin properties file.");
				}

				// get the main class name
				final String className = properties.getProperty(PLUGIN_ID_PROPERTY_NAME);
				if (className == null | className.length() == 0)
					throw new EngineException("Plugin class file name is invalid.");

				// get the classpath
				final String classpath = properties.getProperty(PLUGIN_CLASSPATH_PROPERTY_NAME);
				ArrayList<File> pathList = new ArrayList<File>();
				for (String folder: Tools.tokenize(classpath, PLUGIN_CLASSPATH_SEPARATOR))
					if (folder.length() != 0)
						pathList.add(new File(file.getAbsolutePath() + File.separator + folder));

				// load the plugin
				loadPlugin(file.getName(), className, pathList.toArray(new File[]{}));

			} else
				if (Tools.find(PLUGIN_ID_FILE_NAME, contents) > -1) {
					String className = "";
					try {
						className = getPluginClassName(
								new FileInputStream(file.getAbsolutePath() + File.separator + PLUGIN_ID_FILE_NAME));
					} catch (IOException e) {
						throw new EngineException("Cannot read plugin identification file.");
					}
					loadPlugin(file.getName(), className, file);
				} else
					throw new EngineException("Cannot detect plugin.");
		}
	}

	/*
	 * Loads a plugin from a Zip file.
	 */
	private void loadPluginClassesFromZipFile(File file) throws EngineException {
		throw new EngineException("Plugin ZIP files are not supported." + file.getName());
	}

	/*
	 * Loads a plugin from a Zip file.
	 */
	private void loadPluginClassesFromZipFile(JarEntry entry) throws EngineException {
		throw new EngineException("Plugin ZIP files are not supported. " + entry.getName());
	}

	/*
	 * Loads a plugin from a Jar file.
	 */
	private void loadPluginClassesFromJarFile(File file) throws EngineException{
		String className = null;
		try {
			className = getJarPluginClassName(new FileInputStream(file));
		} catch (IOException e) {
			throw new EngineException("Cannot load the JAR file.");
		} catch (EngineException e) {
			throw e;
		}
		loadPlugin(file.getName(), className, file);
	}

	/*
	 * Loads a plugin from a Jar file.
	 */
	private void loadPluginClassesFromJarFile(JarFile jar, JarEntry entry) throws EngineException {
		String className = null;
		try {
			className = getJarPluginClassName(jar.getInputStream(entry));
		}
		catch (IOException e) {
			throw new EngineException("Cannot load the JAR file.");
		}
		catch (EngineException e) {
			throw e;
		}
		try {
			loadPlugin(entry.getName(), className, new URL("jar", "", jar.getName() + "!/" + entry.getName()));
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Loads a single plugin class from the given list of resources.
	 */
	private void loadPlugin(String pName, String className, File... resources) throws EngineException {
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
	private void loadPlugin(String pName, String className, URL... urls) throws EngineException {
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
			p.setControlAPI(this);
			logger.debug("Plugin '{}' is usable.", p.getName());
			allPlugins.put(p.getName(), p);
		} else
			logger.error(
						"Invalid plugin '{}'. This class does not extend the CoreASM Pluing class.", className);
	}


	/**
	 * Reads full class name of a plugin from a text file.
	 *
	 * @param stream
	 *            input stream of the text file (plugin identification file)
	 * @return full class name
	 * @throws IOException
	 *             in case of any IO error
	 */
	private String getPluginClassName(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		String name = reader.readLine();
		return name;
	}

	/**
	 * Given a Jar file name, looks for the name of the plugin class.
	 * 
	 * @param inputStream
	 *            input stream from a jar file
	 * @return full class name of the plugin (e.g., "test.plugin.TestPlugin")
	 * @throws IOException
	 *             in case of any IO error
	 * @throws EngineException
	 *             if the plugin does not have an identification file
	 */
	private String getJarPluginClassName(InputStream inputStream) throws IOException,
			EngineException {
		JarInputStream stream = new JarInputStream(inputStream);
		JarEntry jEntry = null;
		boolean found = false;
		do {
			jEntry = stream.getNextJarEntry();
			if (jEntry != null)
				if (jEntry.getName().equals("CoreASMPlugin.id")) {
					found = true;
					break;
				}
		} while (jEntry != null);
		String pluginClassName = null;
		if (found)
			pluginClassName = getPluginClassName(stream);
		stream.close();
		if (pluginClassName == null)
			throw new EngineException("Invalid Plugin package (" + inputStream
					+ "). Cannot find the identification file.");
		return pluginClassName;
	}

	/**
	 * Loads core plugins.
	 */
	private void loadCorePlugins() {
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
	private void loadSpecPlugins() {
		final List<Plugin> sortedList = new ArrayList<Plugin>();

		// 1. get the list of plugins
		final Collection<String> requiredPlugins = new ArrayList<String>(getSpecPlugins());

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

	/*
	 * Gets the list of plugins that are requested to be loaded by the
	 * engine environment.
	 *
	 * @see EngineProperties.PLUGIN_LOAD_REQUEST
	 */
	private Collection<String> getRequestedPlugins() {
		Collection<String> result = new HashSet<String>();
		String rpList = getProperty(EngineProperties.PLUGIN_LOAD_REQUEST_PROPERTY);
		if (rpList != null) {
			StringTokenizer tokenizer = new StringTokenizer(rpList, EngineProperties.PLUGIN_LOAD_REQUEST_DELIM);
			while (tokenizer.hasMoreTokens())
				result.add(tokenizer.nextToken());
		}
		return result;
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
				error(new EngineException(
							"Plugin Dependency Error: " + p.getName()
							+ " requires " + name + " version " + depends.get(name) + " or higher."));
				return false;
			}
		}
		return true;
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
			p.initialize(this);
		} catch (InitializationFailedException e) {
			logger.error( e.getMessage());
			throw new EngineError(e.getMessage());
		}

		// if plugin implements operators, get operator rules
		if (p instanceof OperatorProvider)
			operatorRules.addAll(((OperatorProvider) p).getOperatorRules());

		loadedPlugins.add(p);
	}

	/**
	 * Notifies the environment of a successful step.
	 *
	 */
	private void notifySuccess() {
		// TODO no notification is sent
		logger.debug("Last update succeeded.");
        scheduler.incrementStepCount();
	}

	/**
	 * Notifies the environment of a failed step.
	 *
	 */
	private void notifyFailure() {
		String reason = "";
		if (storage.getLastInconsistentUpdate() != null) {
			reason = "Incosistent updates: " + Tools.getEOL()
					+ EngineTools.getContextInfo("", storage.getLastInconsistentUpdate(), getParser(), getSpec());
		}
		EngineEvent event = new StepFailedEvent(reason);
		for (EngineObserver observer : observers) {
			if (observer instanceof EngineStepObserver)
				observer.update(event);
		}
		logger.warn("Last update failed.");
	}

	/**
	 * Given a set of plugin names, expands the set of names
	 * to a new set that als contains all the enclosed plugins
	 * of any package plugin.
	 *
	 * @param pluginNames Plugin names
	 * @return new set of plugin names with package plugins expanded
	 * to their enclosed plugins
	 */
	protected Set<String> expandPackagePlugins(Set<String> pluginNames) {
		Set<String> newNames = new HashSet<String>(pluginNames);

		// unpack package plugins
		for (String pName: pluginNames) {
			Plugin plugin = allPlugins.get(pName);
			if (plugin instanceof PackagePlugin)
				newNames.addAll(((PackagePlugin)plugin).getEnclosedPluginNames());
		}

		return newNames;
	}

	protected Set<String> getSpecPlugins() {
		final Set<String> plugins = new HashSet<String>();
		final Set<String> pNames = new HashSet<String>(parser.getRequiredPlugins());

		pNames.addAll(getRequestedPlugins());

		// Adding kernel plugin names
		for (String name: CoreASMEngine.KERNEL_PLUGINS)
			pNames.add(name);

		for (String pName: pNames) {
			Plugin p = allPlugins.get(pName);

			// If cannot find the plugin, try adding "Plugins" or "Plugin"
			// to its name
			if (p == null) {
				p = allPlugins.get(pName + "Plugins");
				if (p == null)
					p = allPlugins.get(pName + "Plugin");
			}

			if (p != null) {
				plugins.add(p.getName());

				// expanding package plugins
				if (p instanceof PackagePlugin) {
					plugins.addAll(((PackagePlugin)p).getEnclosedPluginNames());
				}
			} else
				plugins.add(pName);
		}

		return plugins;
	}

	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}

	@Override
	public AbstractStorage getStorage() {
		return storage;
	}

	/**
	 * This will return the global interpreter component of the engine.
	 * It is highly recommended to use a thread-bound instance of
	 * the Interpreter class (see {@link Interpreter#getInterpreterInstance()})‌ and
	 * not to use this global instance for any interpretation purposes.
	 *
	 * @see ControlAPI#getInterpreter()
	 */
	@Override
	public Interpreter getInterpreter() {
		return interpreter;
	}

	@Override
	public Parser getParser() {
		return parser;
	}

	@Override
	public Plugin getPlugin(String name) {
		return allPlugins.get(name);
	}

	@Override
	public Set<Plugin> getPlugins() {
		return new HashSet<Plugin>(loadedPlugins);
	}

	@Override
	public synchronized void error(String msg) {
		error(msg, null, null);
	}

	@Override
	public synchronized void error(Throwable e) {
		error(e, null, null);
	}

	@Override
	public synchronized void error(String msg, Node errorNode, Interpreter interpreter) {
		CoreASMError error;
		if (interpreter != null)
			error = new CoreASMError(msg, interpreter.getCurrentCallStack(), errorNode);
		else
			error = new CoreASMError(msg, errorNode);
		this.error(error);
	}

	@Override
	public synchronized void error(Throwable e, Node errorNode, Interpreter interpreter) {
		CoreASMError error;
		if (interpreter != null)
			error = new CoreASMError(e, interpreter.getCurrentCallStack(), errorNode);
		else
			error = new CoreASMError(e, null, errorNode);
		this.error(error);
	}

	@Override
	public synchronized void error(CoreASMError e) {
		// FIXME why can we get into this method more than once?!

		if (lastError != null)
			return;

		lastError = e;

		e.setContext(parser, specification);

		// Creating an error event and passing it
		// to all the error observers
		EngineErrorEvent event = new EngineErrorEvent(e);
		for (EngineObserver o : observers) {
			if (o instanceof EngineErrorObserver)
				o.update(event);
		}

		if (e.cause != null && getProperty(EngineProperties.PRINT_STACK_TRACE).equals(
				EngineProperties.YES))
			e.cause.printStackTrace();

		logger.error( e.showError(parser, specification));
	}

	@Override
	public void warning(String src, String msg) {
		warning(src, msg, null, null);
	}

	@Override
	public void warning(String src, Throwable e) {
		warning(src, e, null, null);
	}

	@Override
	public void warning(String src, Throwable e, Node node,
			Interpreter interpreter) {
		CoreASMWarning warning;
		if (interpreter != null)
			warning = new CoreASMWarning(src, e, interpreter.getCurrentCallStack(), node);
		else
			warning = new CoreASMWarning(src, e, null, node);
		this.warning(warning);
	}

	@Override
	public void warning(String src, String msg, Node node,
			Interpreter interpreter) {
		CoreASMWarning warning;
		if (interpreter != null)
			warning = new CoreASMWarning(src, msg, interpreter.getCurrentCallStack(), node);
		else
			warning = new CoreASMWarning(src, msg, node);
		this.warning(warning);
	}

	@Override
	public synchronized void warning(CoreASMWarning w) {
		w.setContext(parser, specification);
		warnings.add(w);

		// Creating a warning event and passes it
		// to all the warning observers
		EngineWarningEvent event = new EngineWarningEvent(w);
		for (EngineObserver o : observers) {
			if (o instanceof EngineWarningObserver)
				o.update(event);
		}
		logger.warn(w.showWarning(parser, specification));
	}


	@Override
	public List<CoreASMWarning> getWarnings() {
		return warnings;
	}

	@Override
	public boolean hasErrorOccurred() {
//		final EngineMode engineMode = getEngineMode();
		return (lastError != null) || (engineMode == EngineMode.emError) ;
	}

	@Override
	@Deprecated
	public void waitForIdleOrError() {
		waitWhileBusy();
	}

	@Override
	public void waitWhileBusy() {
		while (isBusy()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.debug("Finished waiting. (Mode: {})",getEngineMode());
	}

	@Override
	public synchronized boolean isBusy() {
		return (engineMode != EngineMode.emTerminated) && (engineBusy || !commandQueue.isEmpty());
	}

	/**
	 * This is the execution thread of a CoreASM engine. This class implements
	 * <code>Runnable</code>.
	 *
	 */
	private class EngineThread extends Thread {

		/** Indicates whether the engine should be terminated. */
		private boolean terminating;

		/**
		 * Constructs a new EngineThread. An engine thread is created when by a
		 * CoreASM engine upon its construction.
		 */
		public EngineThread(String name) {
			terminating = false;
			this.setName(name);
		}

		/**
		 * This is the heart of this thread. This method is responsible for the
		 * execution flow of the engine (based on the control state diagrams of
		 * the engine). Looping until the engine is terminated, this method
		 * calls appropriate methods based on the current mode of the engine and
		 * switches the mode appropriately.
		 *
		 * When the engine is in idle mode, it calls
		 * <code>porcessNextCommand()</code> to respond to user commands. Mode
		 * switching is performed by calling <code>next(newMode)</code>.
		 *
		 * @see Runnable#run()
		 * @see #processNextCommand()
		 * @see #next(EngineMode)
		 */
		@Override
		public void run() {
			try {
				engineBusy = true;

				while (!terminating) {
					try {
						EngineMode engineMode = getEngineMode();

						// if an error is occurred and the engine is not
						// in error mode, go to the error mode
						if (lastError != null
								&& engineMode != EngineMode.emError) {
							next(EngineMode.emError);
						}

						// if engine mode is idle and there is no user command,
						// sleep for a short time (100ms)
						if ((engineMode == EngineMode.emIdle && commandQueue.isEmpty())
									|| engineMode == EngineMode.emError) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								logger.debug( "Engine is forced to stop.");
							}
						}

						engineMode = getEngineMode();

						switch (engineMode) {

						case emIdle:
							processNextCommand();
							engineBusy = (getEngineMode() != EngineMode.emIdle);
							break;

						case emInitKernel:
							initKernel();
							next(EngineMode.emLoadingCatalog);
							break;

						case emLoadingCatalog:
							try {
								loadCatalog();
								next(EngineMode.emLoadingCorePlugins);
							} catch (IOException e) {
								error(e);
							}
							break;

						case emLoadingCorePlugins:
							loadCorePlugins();
							next(EngineMode.emIdle);
							break;

						case emParsingHeader:
							resetEngine();
							parser.parseHeader();
							specification.setRootNode(parser.getRootNode());
							specification.setPluginNames(getSpecPlugins());
							if (lastCommand.type.equals(EngineCommand.CmdType.ecOnlyParseHeader)
									&& !((ParseCommandData)lastCommand.metaData).loadPlugins)
								next(EngineMode.emIdle);
							else
								next(EngineMode.emLoadingPlugins);
							break;

						case emLoadingPlugins:
							loadSpecPlugins();
							if (lastCommand.type.equals(EngineCommand.CmdType.ecOnlyParseHeader)) {
								next(EngineMode.emIdle);
							} else
								next(EngineMode.emParsingSpec);
							break;

						case emParsingSpec:
							parser.parseSpecification();
							specification.setRootNode(parser.getRootNode());
							if (lastCommand.type.equals(EngineCommand.CmdType.ecOnlyParseSpec)) {
								next(EngineMode.emIdle);
							} else
								next(EngineMode.emInitializingState);
							break;

						case emInitializingState:
							storage.initAbstractStorage();
							next(EngineMode.emPreparingInitialState);
							break;

						case emPreparingInitialState:
							scheduler.prepareInitialState();
							isStateInitialized = true;
							next(EngineMode.emIdle);
							break;

						case emTerminating:
							terminating = true;
							next(EngineMode.emTerminated);
							break;

						case emStartingStep:
							if (!isStateInitialized
									|| specification == null
									|| specification.getRootNode() == null)
								throw new EngineError("Engine cannot make a step " +
										"before the specification is properly loaded.");
							warnings.clear();
							scheduler.startStep();
							scheduler.retrieveAgents();
							next(EngineMode.emSelectingAgents);
							break;

						case emSelectingAgents:
							if (scheduler.selectAgents())
								next(EngineMode.emRunningAgents);
							else
								next(EngineMode.emStepSucceeded);
							break;

						case emRunningAgents:
							if (scheduler.getSelectedAgentSet().size() == 0)
								next(EngineMode.emAggregation);
							else {
								scheduler.executeAgentPrograms();
								next(EngineMode.emAggregation);
							}
							break;

							/*
						case emChoosingAgents:
							scheduler.chooseAgent();
							if (scheduler.getChosenAgent() == null)
								next(EngineMode.emAggregation);
							else
								next(EngineMode.emInitializingSelf);
							break;

						case emChoosingNextAgent:
							scheduler.accumulateUpdates();
							next(EngineMode.emChoosingAgents);
							break;
							*/

						case emUpdateFailed:
							if (scheduler.isSingleAgentInconsistent())
								next(EngineMode.emStepFailed);
							else {
								scheduler.handleFailedUpdate();
								if (scheduler.agentsCombinationExists())
									next(EngineMode.emSelectingAgents);
								else
									next(EngineMode.emStepFailed);
							}
							break;

							/*
						case emInitiatingExecution:
							scheduler.initiateExecution();
							next(EngineMode.emProgramExecution);
							break;
							*/

						case emStepSucceeded:
							notifySuccess();
							next(EngineMode.emIdle);
							break;

						case emStepFailed:
							notifyFailure();
							next(EngineMode.emIdle);
							break;

							/*
						case emInitializingSelf:
							storage.setChosenAgent(scheduler.getChosenAgent());
							storage.getChosenProgram();
							next(EngineMode.emInitiatingExecution);
							break;
							*/

						case emAggregation:
							storage.aggregateUpdates();
							if (storage.isConsistent(scheduler.getUpdateSet())) {
								storage.fireUpdateSet(scheduler.getUpdateSet());
								next(EngineMode.emStepSucceeded);
							} else
								next(EngineMode.emUpdateFailed);
							break;

							/*
						case emProgramExecution:
							interpreter.executeTree();
							if (interpreter.isExecutionComplete())
								next(EngineMode.emChoosingNextAgent);
							else
								next(EngineMode.emProgramExecution);
							break;
							*/

						case emError:
							// Throw out all commands except a recovery command
							while (!commandQueue.isEmpty()) {
								EngineCommand cmd = commandQueue.remove(0);
								if (cmd != null) {
									if (cmd.type == EngineCommand.CmdType.ecTerminate) {
										next(EngineMode.emTerminating);
										lastError = null;
										logger.debug("Engine terminated by user command.");
										break;
									}
									else if (cmd.type == EngineCommand.CmdType.ecRecover) {
										// Recover by going to the idle mode
										next(EngineMode.emIdle);
										lastError = null;
										logger.debug("Engine recovered from error by user command.");
										break;
									}
								}
							}
							engineBusy = false;
						case emTerminated:
						break;
						default:
						break;

						}
					} catch (CoreASMError ce) {
						error(ce);
						logger.error( "Error occured: {}", ce.showError());
					} catch (Throwable e) {
						if (e instanceof ParserException)
							error(new CoreASMError((ParserException)e));
						else
							error(e);
						logger.error("Exception occured. ", e);
						// StackTraceElement[] trace = e.getStackTrace();
						// for (StackTraceElement ste: trace)
						//   logger.error( ste.toString());
					}
				}

				// Terminating plugins
				for (Plugin p: loadedPlugins)
					p.terminate();

				// empty command queue
				commandQueue.clear();


			} catch (Error e) {
				e.printStackTrace();
			}

			engineBusy = false;
		}

		/**
		 * Switches the engine mode to a new mode.
		 *
		 * @param newMode
		 *            new mode of the engine
		 */
		private void next(EngineMode newMode) throws EngineException {
			engineBusy = true;

			EngineMode oldMode = null;

			synchronized (engineMode) {
				oldMode = engineMode;
				engineMode = newMode;
			}

			Map<EngineMode, EngineModeEvent> map = null;
			EngineModeEvent event = null;
			// notifying all engine mode observers
			for (EngineObserver o : observers)
				if (o instanceof EngineModeObserver) {
					if (event == null) {
						// create the mode-change event
						map = modeEventCache.get(oldMode);
						if (map == null) {
							map = new HashMap<EngineMode, EngineModeEvent>();
							modeEventCache.put(oldMode, map);
						}
						event = map.get(newMode);
						if (event == null) {
							event = new EngineModeEvent(oldMode, newMode);
							map.put(newMode, event);
						}
					}
					o.update(event);
				}

			for (Entry<ExtensionPointPlugin, Integer> p: loadedPlugins.getSrcModePlugins(oldMode))
				p.getKey().fireOnModeTransition(oldMode, newMode);
			for (Entry<ExtensionPointPlugin, Integer> p: loadedPlugins.getTrgModePlugins(newMode))
				p.getKey().fireOnModeTransition(oldMode, newMode);
		}

		/**
		 * If more steps needs to be performed, fakes a new step command;
		 * otherwise it fetches another command from the command queue and
		 * switches the engine mode based on that command. It uses
		 * <code>next(EngineMode)</code> to change the engine mode.
		 *
		 * @see #next(EngineMode)
		 */
		private void processNextCommand() throws EngineException {
			// This is here to avoid leaving the engine in
			// a state that is right before changing its
			// state
			boolean shouldRemoveFirstCommand = false;
			synchronized (engineMode) {
				EngineCommand cmd = null;
				int rrc = 0;
				String tempMsg = null;

				synchronized (this) {
					rrc = remainingRunCount--;
				}
				if (rrc > 0)
					cmd = new EngineCommand(EngineCommand.CmdType.ecStep, null);
				else if (!commandQueue.isEmpty()) {
					cmd = commandQueue.get(0);
					shouldRemoveFirstCommand = true;
				}

				if (cmd == null)
					return;
				else
					lastCommand = cmd;

				switch (cmd.type) {

				case ecTerminate:
					next(EngineMode.emTerminating);
					break;

				case ecInit:
					next(EngineMode.emInitKernel);
					break;

				case ecLoadSpec:
					tempMsg = "Loading specification file";
				case ecOnlyParseSpec:
					tempMsg = "Parsing specification file";
				case ecOnlyParseHeader:
					tempMsg = "Parsing the header of the specification file";
					ParseCommandData cmdData = null;
					if (cmd.metaData instanceof ParseCommandData)
						cmdData = (ParseCommandData)cmd.metaData;
					else {
						cmdData = new ParseCommandData(true, cmd.metaData);
					}
					if (cmdData.specInfo instanceof String) {
						try {
							specification = new Specification(Engine.this, new File((String)cmdData.specInfo));
							parser.setSpecification(specification);
							logger.debug("{}: {}", tempMsg, cmdData.specInfo);
							next(EngineMode.emParsingHeader);
						} catch (FileNotFoundException e) {
							error("Specification file is not found (" + cmdData.specInfo + ")\n. Nothing is loaded.");
						} catch (IOException e) {
							error("Specification file cannot be read from (" + cmdData.specInfo + ")\n. Nothing is loaded.");
						}
					} else
						if (cmdData.specInfo instanceof NamedStringReader) {
							final NamedStringReader nsrData = (NamedStringReader)cmdData.specInfo;
							try {
								specification = new Specification(Engine.this, nsrData.reader, nsrData.fileName);
								parser.setSpecification(specification);
								logger.debug("{}.", tempMsg);
								next(EngineMode.emParsingHeader);
							} catch (IOException e) {
								error("Specification file cannot be read from (" + nsrData.fileName + ")\n. Nothing is loaded.");
							}
						} else
							error("The specification passed to the engine is invalid.");
					break;

				case ecStep:
					next(EngineMode.emStartingStep);
					break;

				case ecRun:
					if (cmd.metaData instanceof Integer) {
						int i = ((Integer) cmd.metaData).intValue();
						if (i > 0)
							synchronized (this) {
								remainingRunCount = i;
							}
					}
					break;
				case ecRecover:
				break;
				}
				if (shouldRemoveFirstCommand)
					commandQueue.remove(0);
			}
		}
	}

	@Override
	public Set<? extends Element> getLastSelectedAgents() {
		return scheduler.getLastSelectedAgents();
	}

	@Override
	public PluginServiceInterface getPluginInterface(String pName) {
		Plugin p = allPlugins.get(pName);
		if (p != null && loadedPlugins.contains(p)) {
			return p.getPluginInterface();
		}
		return null;
	}

	/**
	 * @return customized class loader that is used
	 * by the engine to load plugins. A <code>null</code>
	 * indicates the default class loader.
	 */
	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * Sets a customized class loader for the engine
	 * (used in loading plugins. If this value is set
	 * to <code>null</code>, the engine will use the
	 * default class loader.
	 */
	@Override
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	@Override
	public Map<String,VersionInfo> getPluginsVersionInfo() {
		Map<String,VersionInfo> list = new HashMap<String,VersionInfo>();

		if (allPlugins != null) {
			for (Plugin p: allPlugins.values())
				list.put(p.getName(), p.getVersionInfo());
		}

		return list;
	}

	@Override
    public int getStepCount() {
        return scheduler.getStepCount();
    }

	@Override
	public void addServiceProvider(String type, ServiceProvider provider) {
		Set<ServiceProvider> set = serviceRegistry.get(type);
		if (set == null) {
			set = new HashSet<ServiceProvider>();
			serviceRegistry.put(type, set);
		}
		set.add(provider);
	}

	@Override
	public Set<ServiceProvider> getServiceProviders(String type) {
		Set<ServiceProvider> set = serviceRegistry.get(type);
		if (set == null)
			return Collections.emptySet();
		return set;
	}

	@Override
	public void removeServiceProvider(String type, ServiceProvider provider) {
		Set<ServiceProvider> set = serviceRegistry.get(type);
		if (set != null)
			set.remove(provider);
	}

	@Override
	public Map<String, Object> serviceCall(ServiceRequest sr, boolean withResults) {
		Set<ServiceProvider> set = serviceRegistry.get(sr.type);

		Map<String, Object> result = null;
		if (withResults)
			result = new HashMap<String, Object>();

		if (set != null && set.size() > 0) {
			for (ServiceProvider sp: set) {
				Object retres = sp.call(sr);
				if (retres != null && result != null)
					result.put(sp.getName(), retres);
			}
		}
		return result;
	}

	@Override
	public void addInterpreterListener(InterpreterListener listener) {
		interpreterListeners.add(listener);
	}

	@Override
	public void removeInterpreterListener(InterpreterListener listener) {
		interpreterListeners.add(listener);
	}

	@Override
	public List<InterpreterListener> getInterpreterListeners() {
		return interpreterListeners;
	}
}

/**
 * Instances of this class represent various engine commands send to CoreASM
 * engine by its environment. This class is only instanciated internally by the
 * engine for its own records.
 *
 * @author Roozbeh Farahbod
 *
 */
class EngineCommand {

	/**
	 * Possible engine command types.
	 *
	 * @author Roozbeh Farahbod
	 */
	public enum CmdType {
		ecTerminate, ecInit, ecLoadSpec, ecOnlyParseSpec, ecOnlyParseHeader, ecStep, ecRun, ecRecover
	};

	/**
	 * Type of this command.
	 */
	public final CmdType type;

	/**
	 * Data associated with this command (e.g., specification to be loaded)
	 */
	public final Object metaData;

	/**
	 * Creates a new engine command with the given type and meta data.
	 */
	public EngineCommand(CmdType type, Object metaData) {
		this.type = type;
		this.metaData = metaData;
	}

}

/**
 * An array list with synchronized methods.
 *
 * @author Roozbeh Farahbod
 *
 */
class CommandQueue extends ArrayList<EngineCommand> {

	private static final long serialVersionUID = 1L;

	@Override
	public synchronized boolean add(EngineCommand e) {
		return super.add(e);
	}

	@Override
	public synchronized void add(int index, EngineCommand element) {
		super.add(index, element);
	}

	@Override
	public synchronized void clear() {
		super.clear();
	}

	@Override
	public synchronized boolean contains(Object o) {
		return super.contains(o);
	}

	@Override
	public synchronized EngineCommand get(int index) {
		return super.get(index);
	}

	@Override
	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public synchronized EngineCommand remove(int index) {
		return super.remove(index);
	}

	@Override
	public synchronized boolean remove(Object o) {
		return super.remove(o);
	}

	@Override
	public synchronized int size() {
		return super.size();
	}

	@Override
	public synchronized String toString() {
		return super.toString();
	}

}

/*
 * Meta-data of a parseHeader command
 */
class ParseCommandData {
	protected final boolean loadPlugins;
	protected final Object specInfo;

	public ParseCommandData(boolean loadPlugins, Object specInfo) {
		this.loadPlugins = loadPlugins;
		this.specInfo = specInfo;
	}
}

/*
 * Binds a name to a StringReader.
 */
class NamedStringReader {

	protected final String fileName;
	protected final Reader reader;

	public NamedStringReader(String fileName, Reader src) {
		this.fileName = fileName;
		this.reader = src;
	}

}

