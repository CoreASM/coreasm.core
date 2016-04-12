/*	
 * Plugin.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2005-2007 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMIssue;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.registry.ICoreASMPlugin;
import org.coreasm.engine.registry.PluginInfo;

/** 
 *	Superclass of CoreASM engine plugins.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public abstract class Plugin implements ICoreASMPlugin {

	/** Default load order of a plug-in */
	public static final double DEFAULT_LOAD_PRIORITY = 50.0;
	
	/** Control API of engine instance this plugin is associated with */
	protected ControlAPI capi;
	
	/** 
	 * set of keywords provided by this plugin 
	 * (should be read through <code>getKeywords()</code>.
	 */
	protected Set<String> keywords = null;
	
	/**
	 */
	public Plugin() {
		super();
	}

	@Override
	public final String getName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public void setControlAPI(ControlAPI capi) {
		this.capi = capi;
	}
	
	@Override
	public abstract void initialize() throws InitializationFailedException;

	@Override
	public final void initialize(ControlAPI capi) throws InitializationFailedException {
		setControlAPI(capi);
		initialize();
	}

	@Override
	public void terminate() {	}

	@Override
	public Set<String> getDependencyNames() {
		return Collections.emptySet();
	}
	
	@Override
	public Map<String,VersionInfo> getDependencies() {
		Map<String,VersionInfo> result = new HashMap<String,VersionInfo>();
		
		for (String name: getDependencyNames()) {
			result.put(name, new VersionInfo(0, 0, 0, ""));
		}
		
		return result;
	}
	
	@Override
	public PluginInfo getInfo() {
		return null;
	}

	@Override
	public PluginServiceInterface getPluginInterface() {
		return null;
	}

	@Override
	public double getLoadPriority() {
		return DEFAULT_LOAD_PRIORITY;
	}
	
	@Override
	public void checkOptionValue(String option, String value) throws CoreASMIssue {
	}
	
	protected String getOptionValue(String option) {
		return capi.getProperty(getName() + "." + option);
	}
	
	protected static String getOptionValue(Class<?> clazz, ControlAPI capi, String option) {
		return capi.getProperty(clazz.getSimpleName() + "." + option);
	}
	
	@Override
	public Set<String> getOptions() {
		return Collections.emptySet();
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return null;
	}
}
