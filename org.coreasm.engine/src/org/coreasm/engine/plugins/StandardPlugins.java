/*	
 * StandardPlugins.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
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
 * The Standard Plugin package.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class StandardPlugins extends Plugin implements PackagePlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 9, 0, "beta");
	
	private Set<String> names = null;

	public StandardPlugins() {
		names = new HashSet<String>();
		names.add("BlockRulePlugin");
		names.add("ChooseRulePlugin");
		names.add("ConditionalRulePlugin");
		names.add("ExtendRulePlugin");
		names.add("ForallRulePlugin");
		names.add("IOPlugin");
		names.add("LetRulePlugin");
		names.add("NumberPlugin");
		names.add("PredicateLogicPlugin");
		names.add("SetPlugin");
		names.add("SignaturePlugin");
		names.add("StringPlugin");
		names.add("TurboASMPlugin");
		names.add("CollectionPlugin");
		names.add("ListPlugin");
		names.add("MapPlugin");
		names.add("AbstractionPlugin");
		names.add("CaseRulePlugin");
		names.add("OptionsPlugin");
		names.add("KernelExtensionsPlugin");
	}
	
	/**
	 * Does nothing.
	 * 
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
		// Nothing.
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.PackagePlugin#getEnclosedPluginNames()
	 */
	public Set<String> getEnclosedPluginNames() {
		return names;
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

}
