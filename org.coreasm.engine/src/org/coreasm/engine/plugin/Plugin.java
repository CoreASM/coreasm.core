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

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.VersionInfoProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** 
 *	Superclass of CoreASM engine plugins.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public abstract class Plugin implements VersionInfoProvider {

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

	/**
	 * Returns the name of this Plug-in,
	 * which is the name of the runtime class of this plugin.
	 * 
	 * @return name of the plugin
	 */
	public final String getName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * Sets the Control API of the instance of the engine which
	 * this plugin is associated with.
	 * 
	 * @param capi The <code>ControlAPI</code> of this instance
	 * of the engine.
	 */
	public void setControlAPI(ControlAPI capi) {
		this.capi = capi;
	}
	
	/**
	 * Initializes this plugin. This includes registration 
	 * of operators in the engine.
	 */
	public abstract void initialize() throws InitializationFailedException;

	/**
	 * Initializes this plugin. This includes registration 
	 * of operators in the engine. This method also sets
	 * the reference to the ControlAPI instance.
	 */
	public final void initialize(ControlAPI capi) throws InitializationFailedException {
		setControlAPI(capi);
		initialize();
	}

	/**
	 * Finalizes the activities of this plugin. Will be called by the engine, 
	 * when the engine is terminated.
	 */
	public void terminate() {	}

	/**
	 * Provides a set of the <b>names</b> of other plugins that 
	 * this plugin depends on. It should return an empty Set if
	 * there is no dependency requirement for this plugin.
	 * 
	 * @return a set of plugin names 
	 */
	public Set<String> getDependencyNames() {
		return Collections.emptySet();
	}
	
	/**
	 * Provides a map of <b>(name -> versionInfo)</b> of other plugins that 
	 * this plugin depends on. The versionInfo is the minimum 
	 * version the required plugin should have.
	 * 
	 * This method should return an empty map 
	 * ({@link Collections#emptyMap()}) if
	 * there is no dependency requirement for this plugin.
	 * 
	 * By default, this method will return the dependency names
	 * provided by {@link #getDependencyNames()} with a version info 
	 * of 0.0.0. 
	 * 
	 * @return a set of plugin names
	 * @see VersionInfo
	 */
	public Map<String,VersionInfo> getDependencies() {
		Map<String,VersionInfo> result = new HashMap<String,VersionInfo>();
		
		for (String name: getDependencyNames()) {
			result.put(name, new VersionInfo(0, 0, 0, ""));
		}
		
		return result;
	}
	
	/**
	 * @return some information about this plugin in form of a <code>PluginInfo</code> object
	 */
	public PluginInfo getInfo() {
		return null;
	}

	/**
	 * Returns an interface to provide services to engine's environment (GUIs, tools, etc.).
	 * By default this method returns <code>null</code>, but can be overridden by 
	 * plugins.
	 */
	public PluginServiceInterface getPluginInterface() {
		return null;
	}

	/**
	 * Returns the suggested loading priority of this plug-in.
	 * Zero (0) is the lowest priority and 100 is the highest loading 
	 * priority. The engine will consider this priority when 
	 * loading plug-ins. All plug-ins with the same priority level will
	 * be loaded in a non-deterministic order.
	 * 
	 * @see #DEFAULT_LOAD_PRIORITY
	 */
	public double getLoadPriority() {
		return DEFAULT_LOAD_PRIORITY;
	}
	
	/**
	 * Packs plugin info 
	 * 
	 * @author Roozbeh Farahbod
	 * 
	 */
	public static class PluginInfo {
		public final String author;
		public final String version;
		public final String description;
		
		public PluginInfo(String author, String version, String description) {
			this.author = author;
			this.version = version;
			this.description = description;
		}
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
}
