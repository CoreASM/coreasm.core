/*	
 * ExtendRulePlugin.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006-2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.extendrule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.extendrule.CompilerExtendRulePlugin;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Location;
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

/** 
 * Adds the 'extend U with u do R' rule form.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class ExtendRulePlugin extends Plugin implements ParserPlugin, InterpreterPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 8, 1, "");
	
	public static final String PLUGIN_NAME = ExtendRulePlugin.class.getSimpleName();
	
	public static final String EXTEND_TOKEN = "extend";
	
	private Map<String, GrammarRule> parsers = null;

	private final String[] keywords = {"extend", "with", "do"};
	private final String[] operators = {};

	private final CompilerPlugin compilerPlugin = new CompilerExtendRulePlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
		
	}


	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}
	
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}
	
	/**
	 * @return <code>null</code>
	 */
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}

	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices)capi.getPlugin("Kernel").getPluginInterface();
			
			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> termParser = kernel.getTermParser();
			
			ParserTools pTools = ParserTools.getInstance(capi);

			Parser<Node> extendParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("extend", PLUGIN_NAME),
					termParser,
					pTools.getKeywParser("with", PLUGIN_NAME),
					pTools.getIdParser(),
					pTools.getKeywParser("do", PLUGIN_NAME),
					ruleParser
					}).map( new ExtendParseMap());
			parsers.put("Rule", 
					new GrammarRule("ExtendRule",
							"'extend' Term 'with' ID 'do' Rule", extendParser, PLUGIN_NAME));
		}
		
		return parsers;
	}
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		
		if (pos instanceof ExtendRuleNode) {
			ExtendRuleNode node = (ExtendRuleNode) pos;
			Element domain = node.getUniverseNode().getValue();
			 
			if (!node.getUniverseNode().isEvaluated()) 
				return node.getUniverseNode();
			   
			if (!node.getRuleNode().isEvaluated()) {
				if (domain instanceof UniverseElement) {
					Element e = capi.getStorage().getNewElement();
					interpreter.addEnv(node.getIdNode().getToken(), e);
					return node.getRuleNode();
				} else
					if (domain instanceof BackgroundElement) {
						try {
							Element e = ((BackgroundElement)domain).getNewValue();
							interpreter.addEnv(node.getIdNode().getToken(), e);
							return node.getRuleNode();
						} catch (UnsupportedOperationException e) {
							capi.error("Cannot extend " + node.getUniverseNode().getToken() + ".", node, interpreter);
						}
					} else
						capi.error("Cannot extend a non-universe!", node.getUniverseNode(), interpreter);
			} else {
				UpdateMultiset augU = node.getRuleNode().getUpdates();

				if (domain instanceof UniverseElement) {
					// create an update to add the new element to the universe
					Element newElement = interpreter.getEnv(node.getIdNode().getToken());
					augU.add(new Update(
							new Location(node.getUniverseName(), ElementList.create(newElement)),
							BooleanElement.TRUE, 
							Update.UPDATE_ACTION,
							interpreter.getSelf(),
							pos.getScannerInfo()));
				}
				
				pos.setNode(null, augU, null);
				interpreter.removeEnv(node.getIdNode().getToken());
			}
		}
		
		return pos;
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public static class ExtendParseMap extends ParserTools.ArrayParseMap {

		public ExtendParseMap() {
			super(PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] vals) {
			Node node = new ExtendRuleNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
	}
}
