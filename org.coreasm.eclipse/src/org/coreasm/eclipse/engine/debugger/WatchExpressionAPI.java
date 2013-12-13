package org.coreasm.eclipse.engine.debugger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.coreasm.eclipse.debug.core.model.ASMStorage;
import org.coreasm.eclipse.debug.ui.views.ASMUpdate;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.CoreASMWarning;
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
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.InterpreterImp;
import org.coreasm.engine.interpreter.InterpreterListener;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.OperatorRegistry;
import org.coreasm.engine.parser.Parser;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.PluginServiceInterface;
import org.coreasm.engine.plugin.ServiceProvider;
import org.coreasm.engine.plugin.ServiceRequest;
import org.coreasm.engine.scheduler.Scheduler;

public class WatchExpressionAPI implements ControlAPI {
	private ASMStorage storage;
	private ControlAPI capi;
	private CoreASMError lastError = null;
	private List<CoreASMWarning> warnings = new ArrayList<CoreASMWarning>();

	public WatchExpressionAPI(ControlAPI capi) {
		this.capi = capi;
	}
	
	public Element evaluateExpression(ASTNode expression, Element agent, ASMStorage storage) throws InterpreterException {
		if (Thread.holdsLock(capi.getInterpreter().getInterpreterInstance()))
			throw new InterpreterException(new CoreASMError("The current thread already holds a lock on the interpreter instance!"));
//		FIXME Find a better way to determine if the current thread is an eclipse thread
		if (Thread.currentThread().isDaemon())
			throw new InterpreterException(new CoreASMError("This method may only be called from an Eclipse Thread!"));
		this.storage = storage;
		copyOprRegFromCapi();
		
		Interpreter interpreter = new InterpreterImp(this);
		
		for (Entry<String, Element> environmentVariable : storage.getEnvVars().entrySet())
			interpreter.addEnv(environmentVariable.getKey(), environmentVariable.getValue());
		
		bindPlugins();
		
		interpreter.setSelf(agent);
		interpreter.setPosition(expression);
		
		lastError = null;
		
		do {
			interpreter.executeTree();
		} while (!(interpreter.isExecutionComplete() || hasErrorOccurred()));
		
		if (hasErrorOccurred())
			throw new InterpreterException(lastError);
		
		unbindPlugins();
		
		return interpreter.getPosition().getValue();
	}
	
	private void bindPlugins() {
		for (Plugin plugin : getPlugins())
			plugin.setControlAPI(this);
	}
	
	private void unbindPlugins() {
		for (Plugin plugin : getPlugins())
			plugin.setControlAPI(capi);
	}
	
	private void copyOprRegFromCapi() {
		OperatorRegistry oprRegCapi = OperatorRegistry.getInstance(capi);
		OperatorRegistry oprReg = OperatorRegistry.getInstance(this);
		oprReg.binOps.clear();
    	oprReg.binOps.putAll(oprRegCapi.binOps);
    	oprReg.unOps.clear();
    	oprReg.unOps.putAll(oprRegCapi.unOps);
    	oprReg.indexOps.clear();
    	oprReg.indexOps.putAll(oprRegCapi.indexOps);
	}
	
	@Override
	public void initialize() {
	}

	@Override
	public void terminate() {
	}

	@Override
	public void recover() {
	}

	@Override
	public void loadSpecification(String specFileName) {
	}

	@Override
	public void loadSpecification(Reader src) {
	}

	@Override
	public void loadSpecification(String name, Reader src) {
	}

	@Override
	public void parseSpecification(String specFileName) {
	}

	@Override
	public void parseSpecification(Reader src) {
	}

	@Override
	public void parseSpecification(String name, Reader src) {
	}

	@Override
	@Deprecated
	public void parseSpecificationHeader(String specFileName) {
	}

	@Override
	public void parseSpecificationHeader(String specFileName, boolean loadPlugins) {
	}

	@Override
	@Deprecated
	public void parseSpecificationHeader(Reader src) {
	}

	@Override
	public void parseSpecificationHeader(Reader src, boolean loadPlugins) {
	}

	@Override
	@Deprecated
	public void parseSpecificationHeader(String name, Reader src) {
	}

	@Override
	public void parseSpecificationHeader(String name, Reader src, boolean loadPlugins) {
	}

	@Override
	public Specification getSpec() {
		return capi.getSpec();
	}
	
	@Override
	public State getState() {
		return storage;
	}

	@Override
	public State getPrevState(int i) {
		return capi.getPrevState(i);
	}

	@Override
	public Set<Update> getUpdateSet(int i) {
		if (i == 0)
			return ASMUpdate.unwrap(storage.getUpdates());
		else
			return null;
	}

	@Override
	public UpdateMultiset getUpdateInstructions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateState(Set<Update> update) throws InconsistentUpdateSetException, InvalidLocationException {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<? extends Element> getAgentSet() {
		return storage.getAgents();
	}

	@Override
	public Properties getProperties() {
		return capi.getProperties();
	}

	@Override
	public void setProperties(Properties newProperties) {
	}

	@Override
	public String getProperty(String property) {
		return capi.getProperty(property);
	}

	@Override
	public String getProperty(String property, String defaultValue) {
		return capi.getProperty(property, defaultValue);
	}

	@Override
	public boolean propertyHolds(String property) {
		return capi.propertyHolds(property);
	}

	@Override
	public void setProperty(String property, String value) {
	}

	@Override
	public EngineMode getEngineMode() {
//		TODO: check
		return capi.getEngineMode();
	}

	@Override
	public PluginServiceInterface getPluginInterface(String pName) {
		return capi.getPluginInterface(pName);
	}

	@Override
	public void hardInterrupt() {
	}

	@Override
	public void softInterrupt() {
	}

	@Override
	public void step() {
	}

	@Override
	public void run(int i) {
	}

	@Override
	public void addObserver(EngineObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeObserver(EngineObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<EngineObserver> getObservers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void waitForIdleOrError() {
	}

	@Override
	public void waitWhileBusy() {
	}

	@Override
	public boolean isBusy() {
		return capi.isBusy();
	}

	@Override
	public Set<? extends Element> getLastSelectedAgents() {
		return storage.getLastSelectedAgents();
	}

	@Override
	public ClassLoader getClassLoader() {
		return capi.getClassLoader();
	}

	@Override
	public void setClassLoader(ClassLoader classLoader) {
	}

	@Override
	public Map<String, VersionInfo> getPluginsVersionInfo() {
		return capi.getPluginsVersionInfo();
	}

	@Override
	public int getStepCount() {
		return storage.getStep();
	}

	@Override
	public List<CoreASMWarning> getWarnings() {
		return warnings;
	}

	@Override
	public VersionInfo getVersionInfo() {
		return capi.getVersionInfo();
	}

	@Override
	public void addServiceProvider(String type, ServiceProvider provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeServiceProvider(String type, ServiceProvider provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<ServiceProvider> getServiceProviders(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> serviceCall(ServiceRequest sr, boolean withResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addInterpreterListener(InterpreterListener listener) {

	}

	@Override
	public void removeInterpreterListener(InterpreterListener listener) {

	}

	@Override
	public List<InterpreterListener> getInterpreterListeners() {
		return Collections.emptyList();
	}

	@Override
	public Scheduler getScheduler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractStorage getStorage() {
		return storage;
	}

	@Override
	public Interpreter getInterpreter() {
		// TODO check
		return capi.getInterpreter();
	}

	@Override
	public Parser getParser() {
		// TODO check
		return capi.getParser();
	}

	@Override
	public Plugin getPlugin(String name) {
		return capi.getPlugin(name);
	}

	@Override
	public Set<Plugin> getPlugins() {
		return capi.getPlugins();
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
		if (lastError != null)
			return;
		
		lastError = e;

		e.setContext(getParser(), getSpec());
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
	public void warning(String src, String msg, Node node, Interpreter interpreter) {
		CoreASMWarning warning; 
		if (interpreter != null)
			warning = new CoreASMWarning(src, msg, interpreter.getCurrentCallStack(), node);
		else
			warning = new CoreASMWarning(src, msg, node);
		this.warning(warning);
	}

	@Override
	public void warning(String src, Throwable e, Node node, Interpreter interpreter) {
		CoreASMWarning warning; 
		if (interpreter != null)
			warning = new CoreASMWarning(src, e, interpreter.getCurrentCallStack(), node);
		else
			warning = new CoreASMWarning(src, e, null, node);
		this.warning(warning);
	}

	@Override
	public void warning(CoreASMWarning w) {
		w.setContext(getParser(), getSpec());
		warnings.add(w);
	}

	@Override
	public boolean hasErrorOccurred() {
		return lastError != null;
	}

}
