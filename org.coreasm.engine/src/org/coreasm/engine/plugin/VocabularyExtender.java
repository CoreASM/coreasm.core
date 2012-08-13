/*	
 * VocabularyExtender.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Mashaal Memon
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;

/**
 * A interface with plug-ins that extend the vocabular with functions/universes/backgrounds.
 * 
 * @author Mashaal Memon
 * 
 */
public interface VocabularyExtender {
	
	/**
	 * If extending the vocabulary with functions, initialize and return corresponding function elements. 
	 * Otherwise return null.
	 * 
	 * @return <code>Collection\<FunctionElement\></code> of function elements.
	 */
	public Map<String,FunctionElement> getFunctions();
	
	/**
	 * If extending the vocabulary with universe, initialize and return corresponding universe elements. 
	 * Otherwise return null.
	 * 
	 * @return <code>Collection\<FunctionElement\></code> of universe elements.
	 */
	public Map<String,UniverseElement> getUniverses();
	
	/**
	 * If extending the vocabulary with rules, initialize and return corresponding rule elements. 
	 * Otherwise return null.
	 * 
	 * @return mapping of rule names to rule elements.
	 */
	public Map<String,RuleElement> getRules();
	
	/**
	 * If extending the vocabulary with background, initialize and return corresponding background elements. 
	 * Otherwise return null.
	 * 
	 * @return <code>Collection\<BackgroundElement\></code> of background elements.
	 */
	public Map<String,BackgroundElement> getBackgrounds();

	/**
	 * Returns the names of the fuctions that 
	 * are provided by this plugin. 
	 * <p>
	 * The returned value should NOT be <code>null</code>.
	 * Plug-ins should return an empty set if they are not providing any function.
	 * Hint: use {@link Collections#emptySet()}. 
	 * 
	 */
	public Set<String> getFunctionNames();
	
	/**
	 * Returns the names of the universes that 
	 * are provided by this plugin. 
	 * <p>
	 * The returned value should NOT be <code>null</code>.
	 * Plug-ins should return an empty set if they are not providing any universe.
	 * Hint: use {@link Collections#emptySet()}. 
	 */
	public Set<String> getUniverseNames();
	
	/**
	 * Returns the names of the backgrounds that 
	 * are provided by this plugin. 
	 * <p>
	 * The returned value should NOT be <code>null</code>.
	 * Plug-ins should return an empty set if they are not providing any background.
	 * Hint: use {@link Collections#emptySet()}. 
	 */
	public Set<String> getBackgroundNames();

	/**
	 * Returns the names of the rules that 
	 * are provided by this plugin.
	 * <p>
	 * The returned value should NOT be <code>null</code>.
	 * Plug-ins should return an empty set if they are not providing any rule.
	 * Hint: use {@link Collections#emptySet()}. 
	 */
	public Set<String> getRuleNames();
	
}
