/**
 * Copyright (C) 2012 Roozbeh Farahbod 
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.parser.Parser;
import org.coreasm.util.Tools;
import org.slf4j.Logger;

/**
 * A collection of some utility functions needed by different engine components. 
 * 
 * @author Roozbeh Farahbod
 *
 */
public class EngineTools {

	/**
	 * Creates a context info for the given collection of updates, appending it to the given StringBuffer.
	 *  
	 * @param indent indentation
	 * @param updates the collection of updates
	 * @param parser a link to the parser 
	 * @param spec a link to the specification
	 */
	public static String getContextInfo(String indent, Collection<Update> updates, Parser parser, Specification spec) {
		StringBuffer result = new StringBuffer();
		if (updates != null && updates.size() > 0) {
			for (Update u: updates) {
				result.append(getContextInfo(indent, u, parser, spec));
			}
		}
		
		return result.toString();
	}

	/**
	 * Creates a context info for the given update, appending it to the given StringBuffer.
	 *  
	 * @param indent indentation
	 * @param update the update
	 * @param parser a link to the parser 
	 * @param spec a link to the specification
	 */
	public static String getContextInfo(String indent, Update update, Parser parser, Specification spec) {
		String result = "";
		if (update != null) {
			result += indent + "  - " + update;
			if (update.sources != null) {
				result += " produced by the following " + ((update.sources.size()>1)?"lines":"line") + ":" + Tools.getEOL();
				for (ScannerInfo info: update.sources) {
					result += indent + "      - " + info.getContext(parser, spec);
				}
			}
			result += Tools.getEOL();
		}
		return result;
	}

	/**
	 * Given a list of nodes, returns the list of values of those nodes.
	 * 
	 * @throws InterpreterException if a node in the list does not have a value
	 */
	public static ElementList getValueList(List<ASTNode> nodes) throws InterpreterException {
		if (nodes.size() == 0)
			return ElementList.NO_ARGUMENT;
		
		ArrayList<Element> vList = new ArrayList<Element>();
		Element value = null;
		for (ASTNode n: nodes) {
			value = n.getValue();
			if (value == null) 
				throw new InterpreterException("Expecting expression as argument.");
			vList.add(n.getValue());
		}
		// avoiding to have too many empty lists
		return ElementList.create(vList);
	}

	/**
	 * Checks if the given node has updates attached to it. 
	 * If not, it calls <code>capi.error(String, Node)</code> with 
	 * an error message. If logger is not <code>null</code>, this method
	 * also logs an error.
	 * 
	 * @param node a node
	 * @param capi the Control API of the plugin that calls this method
	 * @param logger a logger instance ({@link Logger}).
	 * @return <code>true</code> if the node has updates; <code>false</code> otherwise.
	 * 
	 * @see Node
	 * @see Loggers
	 */
	public static boolean hasUpdates(Interpreter interpreter, ASTNode node, ControlAPI capi, Logger logger) {
		if (node.getUpdates() == null) {
			String msg = "Rule provides no updates.";
			if (logger != null)
				logger.error(msg + "[at " + node.getScannerInfo().getPos() + ")");
			capi.error(msg, node, interpreter);
			return false;
		} else
			return true;
	}

}
