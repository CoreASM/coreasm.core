/*	
 * StackPlugin.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.stack;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
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
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.plugins.list.HeadLastFunctionElement;
import org.coreasm.engine.plugins.list.ListElement;

/** 
 * Provides stack operations on Indexed Enumerables (e.g., lists).
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class StackPlugin extends Plugin implements ParserPlugin,
		InterpreterPlugin, VocabularyExtender {

	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 1, "");
	
	public static final String PLUGIN_NAME = StackPlugin.class.getSimpleName();
	
	public static final String PEEK_FUNCTION_NAME = "peek";
	
	private Map<String, GrammarRule> parsers = null;
	private Map<String, FunctionElement> functions = null;
	private HashSet<String> depencyList = new HashSet<String>();

	private final String[] keywords = {"push", "into", "pop", "from"};
	private final String[] operators = {};
	
	public StackPlugin() {
		super();
		depencyList.add("ListPlugin");
	}
	

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	@Override
	public Set<String> getDependencyNames() {
		return depencyList;
	}


	@Override
	public void initialize() {

	}

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

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
			
			Parser<Node> pushRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("push", PLUGIN_NAME),
						termParser,
						pTools.getKeywParser("into", PLUGIN_NAME),
						termParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object[] vals) {
							Node node = new PushRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			});
			parsers.put("StackPushRule",
					new GrammarRule("StackPushRule", "'push' Term 'into' Term",
							pushRuleParser, PLUGIN_NAME));
			
			//
			Parser<Node> popRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("pop", PLUGIN_NAME),
						termParser,
						pTools.getKeywParser("from", PLUGIN_NAME),
						termParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object[] vals) {
							Node node = new PopRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			});
			parsers.put("StackPopRule",
					new GrammarRule("StackPopRule", "'pop' Term 'from' Term",
							popRuleParser, PLUGIN_NAME));

			// Rule : StackPopRule | StackPushRule
			parsers.put("Rule",
					new GrammarRule("StackRules", "StackPopRule | StackPushRule",
							Parsers.or(pushRuleParser, popRuleParser), PLUGIN_NAME));

		}
		
		return parsers;

	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.InterpreterPlugin#interpret(org.coreasm.engine.interpreter.ASTNode)
	 */
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		
		if (pos instanceof PopRuleNode) {
			PopRuleNode node = (PopRuleNode)pos;
			ASTNode stackNode = node.getStackNode();
			ASTNode locNode = node.getLocationNode();
			
			// Evaluate elements
			if (!stackNode.isEvaluated()) 
				return stackNode;

			// if the stack element is some kind of a list
			if (stackNode.getValue() instanceof ListElement) {
				
				// if stack can be updated
				if (stackNode.getLocation() != null) {
					ListElement stack = (ListElement)stackNode.getValue();
					
					if (stack.intSize() > 0) {
						// evaluate the location node
						if (!locNode.isEvaluated())
							return locNode;
						
						// if we have a location
						if (locNode.getLocation() != null) {
								
							Update u1 = new Update(
									locNode.getLocation(), 
									stack.head(), 
									Update.UPDATE_ACTION, 
									interpreter.getSelf(),
									pos.getScannerInfo());
							Update u2 = new Update(
									stackNode.getLocation(), 
									stack.tail(), 
									Update.UPDATE_ACTION,
									interpreter.getSelf(),
									pos.getScannerInfo());
							
							pos.setNode(null, new UpdateMultiset(u1, u2), null);
						} else
							capi.error("Cannot pop into a non-location.", pos, interpreter);
					} else
						capi.error("Cannot pop from an empty stack.", pos, interpreter);
				} else
					capi.error("Cannot pop from stack constants.", pos, interpreter);
			} else
				capi.error("Cannot pop from non-stacks.", pos, interpreter);
			
		} else
			if (pos instanceof PushRuleNode) {
				PushRuleNode node = (PushRuleNode)pos;
				ASTNode stackNode = node.getStackNode();
				ASTNode eNode = node.getElementNode();
				
				// Evaluate elements
				if (!stackNode.isEvaluated()) 
					return stackNode;

				// if the stack element is some kind of a list
				if (stackNode.getValue() instanceof ListElement) {
					
					// if stack can be updated
					if (stackNode.getLocation() != null) {
						ListElement stack = (ListElement)stackNode.getValue();
						
						// evaluate the location node
						if (!eNode.isEvaluated())
							return eNode;
						
						if (eNode.getValue() != null) {
							Update u1 = new Update(
									stackNode.getLocation(), 
									stack.cons(eNode.getValue()), 
									Update.UPDATE_ACTION, 
									interpreter.getSelf(),
									pos.getScannerInfo());
							pos.setNode(null, new UpdateMultiset(u1), null);
						} else 
							capi.error("There is no value to push into stack", eNode, interpreter);
					} else
						capi.error("Cannot push into stack constants.", pos, interpreter);
				} else
					capi.error("Cannot push into non-stacks.", pos, interpreter);
			}
		return pos;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgroundNames()
	 */
	public Set<String> getBackgroundNames() {
		return Collections.emptySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgrounds()
	 */
	public Map<String, BackgroundElement> getBackgrounds() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctionNames()
	 */
	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctions()
	 */
	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();
			
			functions.put(PEEK_FUNCTION_NAME, new HeadLastFunctionElement(capi, true));
		}
		return functions;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getRuleNames()
	 */
	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getRules()
	 */
	public Map<String, RuleElement> getRules() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverseNames()
	 */
	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverses()
	 */
	public Map<String, UniverseElement> getUniverses() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

}
