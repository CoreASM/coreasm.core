/*
 * PluginTools.java 	1.0 	$Revision: 243 $
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.util;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.Node;

/**
 * This class provides various static methods that are considered to be
 * helpful in writing plugins.
 * 
 * @author Roozbeh Farahbod
 * 
 */
public class PluginTools {

	/**
	 * Checks if the given node has updates attached to it. 
	 * If not, it calls <code>capi.error(String, Node)</code> with 
	 * an error message. If logger is not <code>null</code>, this method
	 * also logs an error.
	 * 
	 * @param node a node
	 * @param capi the Control API of the plugin that calls this method
	 * @param logger a logger (e.g., {@link Logger#interpreter})
	 * @return <code>true</code> if the node has updates; <code>false</code> otherwise.
	 * 
	 * @see Node
	 * @see Logger
	 */
	public static boolean hasUpdates(Interpreter interpreter, ASTNode node, ControlAPI capi, Logger logger) {
    	if (node.getUpdates() == null) {
			String msg = "Rule provides no updates.";
			if (logger != null)
				Logger.log(Logger.ERROR, logger, msg + "[at " + node.getScannerInfo().getPos() + ")");
			capi.error(msg, node, interpreter);
			return false;
    	} else
    		return true;
	}
}
