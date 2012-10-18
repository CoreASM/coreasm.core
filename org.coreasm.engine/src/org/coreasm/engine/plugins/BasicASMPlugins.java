/*	
 * BasicASMPlugins.java  	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins;

import java.util.HashSet;
import java.util.Set;

import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.plugin.PackagePlugin;
import org.coreasm.engine.plugin.Plugin;

/** 
 * This package plug-in includes all the plug-ins that 
 * together provide the basic ASM rule forms and functionalities.
 * 
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class BasicASMPlugins extends Plugin implements PackagePlugin {

	public static final String PLUGIN_NAME = BasicASMPlugins.class.getSimpleName();
	
	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 0, "");

	private Set<String> names = null;
	
	public BasicASMPlugins() {
		names = new HashSet<String>();
		names.add("BlockRulePlugin");
		names.add("ChooseRulePlugin");
		names.add("ConditionalRulePlugin");
		names.add("ForallRulePlugin");
		names.add("LetRulePlugin");
		names.add("NumberPlugin");
		names.add("PredicateLogicPlugin");
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public Set<String> getEnclosedPluginNames() {
		return names;
	}

}
