/*
 * ConditionalRulePlugin.java 1.0 $Revision: 243 $
 * 
 * 
 * Copyright (C) 2006 George Ma
 * Copyright (c) 2007 Roozbeh Farahbod
 * 
 * Licensed under the Academic Free License version 3.0
 * http://www.opensource.org/licenses/afl-3.0.php
 * http://www.coreasm.org/afl-3.0.php
 */

package org.coreasm.engine.plugins.conditionalrule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.conditionalrule.CompilerConditionalRulePlugin;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorContributor;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.parser.ParserTools.ArrayParseMap;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;

/**
 * Plugin for conditional rule
 * 
 * @author George Ma, Roozbeh Farahbod
 * 
 */
public class ConditionalRulePlugin extends Plugin
		implements ParserPlugin, InterpreterPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 9, 1, "");

	public static final String PLUGIN_NAME = ConditionalRulePlugin.class.getSimpleName();

	private final String[] keywords = { "if", "then", "else", "endif" };
	private final String[] operators = {};

	private Map<String, GrammarRule> parsers = null;

	private final CompilerPlugin compilerPlugin = new CompilerConditionalRulePlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
	@Override
	public String[] getKeywords() {
		return keywords;
	}

	@Override
	public String[] getOperators() {
		return operators;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.coreasm.engine.Plugin#interpret(org.coreasm.engine.interpreter.Node)
	 */
	@Override
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) {

		if (pos instanceof ConditionalRuleNode) {
			ConditionalRuleNode conditionalNode = (ConditionalRuleNode) pos;
			
			if (!conditionalNode.getGuard().isEvaluated()) {
				return conditionalNode.getGuard();
			}
			else {
				if (!(conditionalNode.getGuard().getValue() instanceof BooleanElement))
					capi.error("Element used as guard within \"if\" is not a boolean.",
							conditionalNode.getGuard(),
							interpreter);
				if (conditionalNode.getGuard().getValue().equals(BooleanElement.TRUE)) {
					if (!conditionalNode.getIfRule().isEvaluated()) {
						return conditionalNode.getIfRule();
					}
					else {
						pos.setNode(null, conditionalNode.getIfRule().getUpdates(), null);
						return pos;
					}
				}
				else { // guard is false
					if (conditionalNode.getElseRule() == null) { // there is no else 
						pos.setNode(null, new UpdateMultiset(), null);
						return pos;
					}
					else {
						if (!conditionalNode.getElseRule().isEvaluated()) {
							return conditionalNode.getElseRule();
						}
						else {
							pos.setNode(null, conditionalNode.getElseRule().getUpdates(), null);
							return pos;
						}
					}
				}
			}
		}
		else if (pos instanceof ConditionalTermNode) {
			ConditionalTermNode conditionalTerm = (ConditionalTermNode)pos;
			if (!conditionalTerm.getCondition().isEvaluated())
				return conditionalTerm.getCondition();
			if (!(conditionalTerm.getCondition().getValue() instanceof BooleanElement))
				throw new CoreASMError("The value of the condition of a conditional term must be a BooleanElement but was " + conditionalTerm.getCondition().getValue() + ".", conditionalTerm.getCondition());
			BooleanElement value = (BooleanElement)conditionalTerm.getCondition().getValue();
			if (value.getValue()) {
				if (!conditionalTerm.getIfTerm().isEvaluated())
					return conditionalTerm.getIfTerm();
				pos.setNode(null, new UpdateMultiset(), conditionalTerm.getIfTerm().getValue());
			}
			else {
				if (!conditionalTerm.getElseTerm().isEvaluated())
					return conditionalTerm.getElseTerm();
				pos.setNode(null, new UpdateMultiset(), conditionalTerm.getElseTerm().getValue());
			}
		}
		else {
			return null;
		}
		return pos;
	}

	@Override
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	/**
	 * @return <code>null</code>
	 */
	@Override
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}

	@Override
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel").getPluginInterface();

			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> guardParser = kernel.getGuardParser();
			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);

			Parser<Node> condRuleParser = Parsers.array(
					new Parser[] {
							pTools.getKeywParser("if", PLUGIN_NAME),
							guardParser,
							pTools.getKeywParser("then", PLUGIN_NAME),
							ruleParser,
							Parsers.array(
									pTools.getKeywParser("else", PLUGIN_NAME),
									ruleParser).optional(),
							pTools.getKeywParser("endif", PLUGIN_NAME).optional()
					}).map(new ConditionalParseMap());
			parsers.put("Rule",
					new GrammarRule("ConditionalRule",
							"'if' Guard 'then' Rule ('else' Rule )? ('endif')?",
							condRuleParser, PLUGIN_NAME));
			
			Parser<Node> condTermParser = Parsers.array(pTools.getKeywParser("if", PLUGIN_NAME),
					termParser,
					pTools.getKeywParser("then", PLUGIN_NAME),
					termParser,
					pTools.getKeywParser("else", PLUGIN_NAME),
					termParser).map(new ArrayParseMap(PLUGIN_NAME) {
				public Node map(Object[] vals) {
					Node node = new ConditionalTermNode(((Node)vals[0]).getScannerInfo());
					addChildren(node, vals);
					return node;
				}
			});
			parsers.put("BasicTerm", new GrammarRule("ConditionalTerm", "'if' Term 'then' Term 'else' Term",
					condTermParser, PLUGIN_NAME));

		}
		return parsers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.engine.Plugin#initialize()
	 */
	@Override
	public void initialize() {

	}

	@Override
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public static class ConditionalParseMap extends ArrayParseMap {

		String nextChildName = "guard";

		public ConditionalParseMap() {
			super(PLUGIN_NAME);
		}

		@Override
		public Node map(Object[] vals) {
			nextChildName = "guard";
			Node node = new ConditionalRuleNode(((Node) vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}

		@Override
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode)
				parent.addChild(nextChildName, child);
			else {
				String token = child.getToken();
				if (token.equals("then"))
					nextChildName = "rule";
				//super.addChild(parent, child);
				parent.addChild(child);
			}
		}

	}
}
