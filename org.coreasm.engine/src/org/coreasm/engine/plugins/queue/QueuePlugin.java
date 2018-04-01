/*	
 * QueuePlugin.java  	$Revision: 243 $
 * 
 * Copyright (C) 2007-2009 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.queue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.list.ListElement;

/** 
 * This plug-in provides queue operations on lists.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class QueuePlugin extends Plugin implements ParserPlugin,
		InterpreterPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 1, "");
	
	public static final String PLUGIN_NAME = QueuePlugin.class.getSimpleName();
	
	private Map<String, GrammarRule> parsers = null;
	private HashSet<String> dependencyList = new HashSet<String>();

	private final String[] keywords = {"enqueue", "into", "dequeue", "from"};
	private final String[] operators = {};
	
	public QueuePlugin() {
		dependencyList.add("ListPlugin");
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getKeywords()
	 */
	public String[] getKeywords() {
		return keywords;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getOperators()
	 */
	public String[] getOperators() {
		return operators;
	}

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
	 */
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParsers()
	 */
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			
			Parser<Node> enqRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("enqueue", PLUGIN_NAME),
						termParser,
						pTools.getKeywParser("into", PLUGIN_NAME),
						termParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object[] vals) {
							Node node = new EnqueueRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			});
			parsers.put("EnqueueRule",
					new GrammarRule("EnqueueRule", "'enqueue' Term 'into' Term",
							enqRuleParser, PLUGIN_NAME));
			
			//
			Parser<Node> deqRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("dequeue", PLUGIN_NAME),
						termParser,
						pTools.getKeywParser("from", PLUGIN_NAME),
						termParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object[] vals) {
							Node node = new DequeueRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			});
			parsers.put("DequeueRule",
					new GrammarRule("DequeueRule", "'dequeue' Term 'from' Term",
							deqRuleParser, PLUGIN_NAME));

			// Rule : EnqueueRule | DequeueRule
			parsers.put("Rule",
					new GrammarRule("QueueRules", "EnqueueRule | DequeueRule",
							Parsers.or(enqRuleParser, deqRuleParser), PLUGIN_NAME));

		}
		
		return parsers;

	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.InterpreterPlugin#interpret(org.coreasm.engine.interpreter.ASTNode)
	 */
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		
		if (pos instanceof DequeueRuleNode) {
			DequeueRuleNode node = (DequeueRuleNode)pos;
			ASTNode queueNode = node.getQueueNode();
			ASTNode locNode = node.getLocationNode();
			
			// Evaluate elements
			if (!queueNode.isEvaluated()) 
				return queueNode;

			// if the queue element is some kind of a list
			if (queueNode.getValue() instanceof ListElement) {
				
				// if queue can be updated
				if (queueNode.getLocation() != null) {
					ListElement queue = (ListElement)queueNode.getValue();
					
					if (queue.intSize() > 0) {
						// evaluate the location node
						if (!locNode.isEvaluated())
							return locNode;
						
						// if we have a location
						if (locNode.getLocation() != null) {
								
							Update u1 = new Update(locNode.getLocation(), queue.head(), Update.UPDATE_ACTION, interpreter.getSelf(), pos.getScannerInfo());
							Update u2 = new Update(queueNode.getLocation(), queue.tail(), Update.UPDATE_ACTION, interpreter.getSelf(), pos.getScannerInfo());
							
							pos.setNode(null, new UpdateMultiset(u1, u2), null);
						} else
							capi.error("Cannot dequeue into a non-location.", pos, interpreter);
					} else
						capi.error("Cannot dequeue from an empty queue.", pos, interpreter);
				} else
					capi.error("Cannot dequeue from queue constants.", pos, interpreter);
			} else
				capi.error("Cannot dequeue from non-list.", pos, interpreter);
			
		} else
			if (pos instanceof EnqueueRuleNode) {
				EnqueueRuleNode node = (EnqueueRuleNode)pos;
				ASTNode queueNode = node.getQueueNode();
				ASTNode eNode = node.getElementNode();
				
				// Evaluate elements
				if (!queueNode.isEvaluated()) 
					return queueNode;

				// if the queue element is some kind of a list
				if (queueNode.getValue() instanceof ListElement) {
					
					// if queue can be updated
					if (queueNode.getLocation() != null) {
						ListElement queue = (ListElement)queueNode.getValue();
						
						// evaluate the location node
						if (!eNode.isEvaluated())
							return eNode;
						
						if (eNode.getValue() != null) {
							
							Update u1 = new Update(queueNode.getLocation(), 
									queue.concat(new ListElement(new Element[] {eNode.getValue()})), 
									Update.UPDATE_ACTION,
									interpreter.getSelf(),
									pos.getScannerInfo());
							pos.setNode(null, new UpdateMultiset(u1), null);
						} else 
							capi.error("There is no value to insert into queue", eNode, interpreter);
					} else
						capi.error("Cannot cannot modify a constant queue.", pos, interpreter);
				} else
					capi.error("Cannot enqueue into a non-list.", pos, interpreter);
			}
		return pos;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

}
