package org.coreasm.eclipse.editors;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.coreasm.eclipse.engine.CoreASMEngineFactory;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.CoreASMWarning;
import org.coreasm.engine.Engine;
import org.coreasm.engine.EngineObserver;
import org.coreasm.engine.InconsistentUpdateSetException;
import org.coreasm.engine.Specification;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.State;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterListener;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.Parser;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.PackagePlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.PluginServiceInterface;
import org.coreasm.engine.plugin.ServiceProvider;
import org.coreasm.engine.plugin.ServiceRequest;
import org.coreasm.engine.plugins.modularity.ModularityPlugin;
import org.coreasm.engine.scheduler.Scheduler;

/**
 * This class is a special implementation of the CoreASM ControlAPI interface.
 * It is used by the editor to load plugins. The editor uses this class instead
 * of the regular CoreASM engine because it doesn't load all plugins from the
 * file system each time a new instance is created.
 * 
 * Instead, the class once creates a static "full engine", which is a regular CoreASM
 * engine. This engine is fed with a dummy specification containing use clauses
 * for all plugins, so it loads all plugins. All SlimEngine instances, which get
 * initialized with a partial set of these plugins, load their plugins from this
 * full engine.
 *   
 * @author Markus M�ller
 */
public class SlimEngine implements ControlAPI {

	private static ControlAPI fullEngine = null;
	
	private Parser parser;
	
	private Set<Plugin> plugins;	// plugins which are available through this engine;
	private Set<ExtensionPointPlugin> parsingSpecSrcModePlugins = new HashSet<ExtensionPointPlugin>();
	private Set<ExtensionPointPlugin> parsingSpecTargetModePlugins = new HashSet<ExtensionPointPlugin>();
	
	private List<CoreASMWarning> warnings = new ArrayList<CoreASMWarning>();
	private List<CoreASMError> errors = new ArrayList<CoreASMError>();
	
	private Specification specification;
	
	public SlimEngine(Parser parser, Set<String> plugins) {
		super();

		if (fullEngine == null)
			createFullEngine();
		
		this.parser = parser;
		this.plugins = new HashSet<Plugin>();
		Set<String> pluginnames = new HashSet<String>(plugins); 
		Set<Plugin> tmpPlugins = new HashSet<Plugin>();
		
		// search for package plugins and unpack them
		for (String name: plugins) {
			Plugin p = getPluginFromEngine(name, fullEngine);
			if (p!=null && p instanceof PackagePlugin) {
				PackagePlugin pp = (PackagePlugin) p;
				pluginnames.addAll(pp.getEnclosedPluginNames());
				tmpPlugins.add(p);
			}
		}
		
		// get plugin objects from full engine
		for (String name: pluginnames) {
			Plugin p = getPluginFromEngine(name, fullEngine); 
			if (p == null) {
				continue;
			}
			if (p instanceof PackagePlugin) {
				continue;
			}
			tmpPlugins.add(p);
		}
		
		// create new instance for each plugin which is bound to this engine
		for (Plugin p: tmpPlugins) {
			Plugin p2 = null;
			try {
				p2 = p.getClass().newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			p2.setControlAPI(this);
			this.plugins.add(p2);
			if (p2 instanceof ExtensionPointPlugin) {
				ExtensionPointPlugin extensionPointPlugin = (ExtensionPointPlugin)p2;
				if (extensionPointPlugin.getSourceModes() != null && extensionPointPlugin.getSourceModes().containsKey(EngineMode.emParsingSpec))
					parsingSpecSrcModePlugins.add(extensionPointPlugin);
				if (extensionPointPlugin.getTargetModes() != null && extensionPointPlugin.getTargetModes().containsKey(EngineMode.emParsingSpec)) {
					if (!(extensionPointPlugin instanceof ModularityPlugin))
						parsingSpecTargetModePlugins.add(extensionPointPlugin);
				}
			}
		}
	}
	
	/**
	 * Notify the engine that parsing the specification is about to start.
	 */
	public void notifyEngineParsing() {
		for (ExtensionPointPlugin plugin : parsingSpecTargetModePlugins) {
			try {
				plugin.fireOnModeTransition(EngineMode.emIdle, EngineMode.emParsingSpec);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * Notify the engine that parsing the specification has finished.
	 */
	public void notifyEngineParsingFinished() {
		for (ExtensionPointPlugin plugin : parsingSpecSrcModePlugins) {
			try {
				plugin.fireOnModeTransition(EngineMode.emParsingSpec, EngineMode.emIdle);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * Gets a plugin object identified by its name. Also checks if one of the
	 * suffixed "Plugin" or "Plugins" have been omitted.
	 */
	@Override
	public Plugin getPlugin(String name) {
		String nameP = name + "Plugin";
		String namePP = name + "Plugins";
		
		for (Plugin p: plugins) {
			String pName = p.getName();
			if (pName.equals(name) || pName.equals(nameP) || pName.equals(namePP))
				return p;
		}
		return null;
	}

	/**
	 * Returns a set with all plugins managed by this plugin.
	 */
	@Override
	public Set<Plugin> getPlugins() {
		return new HashSet<Plugin>(plugins);
	}
	
	/**
	 * Gets a plugin object from another engine. Also checks if one of the
	 * suffixed "Plugin" or "Plugins" have been omitted.
	 */
	private Plugin getPluginFromEngine(String pluginname, ControlAPI engine)
	{
		Plugin p = null;
		p = engine.getPlugin(pluginname);
		if (p == null)
			p = engine.getPlugin(pluginname + "Plugin");
		if (p == null)
			p = engine.getPlugin(pluginname + "Plugins");
		return p;
	}
	
	/**
	 * Returns a reference to the Full Engine. If the Full Engine isn't existing
	 * yet, it is created.
	 * @return
	 */
	public static synchronized ControlAPI getFullEngine()
	{
		if (fullEngine == null)
			createFullEngine();
		return fullEngine;
	}
	
	/**
	 * Creates the full engine and feeds it with a dummy specification which
	 * contains a use clause for each existing plugin, so the engine will load
	 * all existing plugins.
	 */
	private static void createFullEngine()
	{
		fullEngine = (ControlAPI) CoreASMEngineFactory.createCoreASMEngine();
		Set<Plugin> plugins = new HashSet<Plugin>();
		
		// get the private allPlugins map from the engine
		try {
			Field tmp = Engine.class.getDeclaredField("pluginLoader");//Engine.class.getMethod(name, parameterTypes)
			tmp.setAccessible(true);
			Object loader = tmp.get(fullEngine);
			Field f = loader.getClass().getDeclaredField("allPlugins");
			//Field f = Engine.class.getDeclaredField("allPlugins");
			f.setAccessible(true);
			Object o = f.get(loader);//f.get(fullEngine);
			@SuppressWarnings("unchecked")
			Map<String,Plugin> m = (Map<String, Plugin>) o;

			plugins = new HashSet<Plugin>(m.values());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// build a specification with an use clause for each plugin
		StringBuilder strSpec = new StringBuilder();
		strSpec.append("CoreASM InitEngine\n");
		for (Plugin p: plugins)
			strSpec.append("use ").append(p.getName()).append("\n");
		strSpec.append("init main\n");
		strSpec.append("rule main = skip");
		
		// load all plugins
		getSpec(strSpec.toString(), true, (Engine) fullEngine);
		fullEngine.terminate();
	}
	
	// COPIED FROM EngineDriver
	private static synchronized Specification getSpec(String text, boolean loadPlugins, Engine engine)
	{
		engine.waitWhileBusy();
		if (engine.getEngineMode() == EngineMode.emError) {
			engine.recover();
			return null;
		}
		engine.parseSpecificationHeader(new StringReader(text), loadPlugins);
		engine.waitWhileBusy();
		if (engine.getEngineMode() == EngineMode.emError) {
			engine.recover();
			return null;
		} else
			return engine.getSpec();
	}
	
	public void setSpec(Specification specification) {
		this.specification = specification;
	}
	
	@Override
	public Specification getSpec() {
		return specification;
	}
	
	@Override
	public Parser getParser() {
		return parser;
	}
	
	@Override
	public List<CoreASMWarning> getWarnings() {
		List<CoreASMWarning> warnings = new ArrayList<CoreASMWarning>(this.warnings);
		this.warnings.clear();
		return warnings;
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
	public void warning(CoreASMWarning w) {
		w.setContext(parser, specification);
		warnings.add(w);
	}
	
	@Override
	public void error(String msg) {
		error(msg, null, null);
	}

	@Override
	public void error(Throwable e) {
		error(e, null, null);
	}

	@Override
	public void error(String msg, Node errorNode, Interpreter interpreter) {
		CoreASMError error; 
		if (interpreter != null)
			error = new CoreASMError(msg, interpreter.getCurrentCallStack(), errorNode);
		else
			error = new CoreASMError(msg, errorNode);
		this.error(error);
	}

	@Override
	public void error(Throwable e, Node errorNode, Interpreter interpreter) {
		CoreASMError error; 
		if (interpreter != null)
			error = new CoreASMError(e, interpreter.getCurrentCallStack(), errorNode);
		else
			error = new CoreASMError(e, null, errorNode);
		this.error(error);
	}

	@Override
	public void error(CoreASMError e) {
		e.setContext(parser, specification);
		errors.add(e);
	}

	@Override
	public boolean hasErrorOccurred() {
		return !errors.isEmpty();
	}
	
	public List<CoreASMError> getErrors() {
		List<CoreASMError> errors = new ArrayList<CoreASMError>(this.errors);
		this.errors.clear();
		return errors;
	}
	
	// ====================================================
	// UNIMPLEMENTED METHODS:
	// ====================================================
	
	@Override
	public void initialize() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void terminate() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void recover() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void loadSpecification(String specFileName) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void loadSpecification(Reader src) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void loadSpecification(String name, Reader src) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void parseSpecification(String specFileName) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void parseSpecification(Reader src) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void parseSpecification(String name, Reader src) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void parseSpecificationHeader(String specFileName) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void parseSpecificationHeader(String specFileName,
			boolean loadPlugins) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void parseSpecificationHeader(Reader src) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void parseSpecificationHeader(Reader src, boolean loadPlugins) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void parseSpecificationHeader(String name, Reader src) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void parseSpecificationHeader(String name, Reader src,
			boolean loadPlugins) {
		throw new UnsupportedOperationException();

	}

	@Override
	public State getState() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public State getPrevState(int i) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public Set<Update> getUpdateSet(int i) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public UpdateMultiset getUpdateInstructions() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void updateState(Set<Update> update)
			throws InconsistentUpdateSetException, InvalidLocationException {
		throw new UnsupportedOperationException();

	}

	@Override
	public Set<? extends Element> getAgentSet() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public Properties getProperties() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setProperties(Properties newProperties) {
		throw new UnsupportedOperationException();

	}

	@Override
	public String getProperty(String property) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public String getProperty(String property, String defaultValue) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public boolean propertyHolds(String property) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setProperty(String property, String value) {
	}

	@Override
	public EngineMode getEngineMode() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public PluginServiceInterface getPluginInterface(String pName) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void hardInterrupt() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void softInterrupt() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void step() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void run(int i) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void addObserver(EngineObserver observer) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void removeObserver(EngineObserver observer) {
		throw new UnsupportedOperationException();

	}

	@Override
	public Collection<EngineObserver> getObservers() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void waitForIdleOrError() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void waitWhileBusy() {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean isBusy() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public Set<? extends Element> getLastSelectedAgents() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public ClassLoader getClassLoader() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setClassLoader(ClassLoader classLoader) {
		throw new UnsupportedOperationException();

	}

	@Override
	public Map<String, VersionInfo> getPluginsVersionInfo() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public int getStepCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public VersionInfo getVersionInfo() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void addServiceProvider(String type, ServiceProvider provider) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void removeServiceProvider(String type, ServiceProvider provider) {
		throw new UnsupportedOperationException();

	}

	@Override
	public Set<ServiceProvider> getServiceProviders(String type) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public Map<String, Object> serviceCall(ServiceRequest sr,
			boolean withResults) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public Scheduler getScheduler() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public AbstractStorage getStorage() {
		//throw new UnsupportedOperationException();
		return null;
		
	}

	@Override
	public Interpreter getInterpreter() {
		return null;
	}

	@Override
	public void addInterpreterListener(InterpreterListener listener) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void removeInterpreterListener(InterpreterListener listener) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public List<InterpreterListener> getInterpreterListeners() {
		throw new UnsupportedOperationException();
		
	}

}
