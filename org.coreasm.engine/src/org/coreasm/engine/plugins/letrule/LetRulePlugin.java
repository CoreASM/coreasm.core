/*
 * LetRulePlugin.java
 *
 * Copyright (C) 2006 George Ma
 * Copyright (C) 2015 Marcel Dausend
 *
 * Last modified on $Date: 2015-03-09 11:25:21 +0200 (Mo, 9 Mrz 2015) $ by
 * $Author: Marcel Dausend $
 *
 * Licensed under the Academic Free License version 3.0
 * http://www.opensource.org/licenses/afl-3.0.php
 * http://www.coreasm.org/afl-3.0.php
 */

package org.coreasm.engine.plugins.letrule;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;

/**
 * Plugin for Let Rule
 *
 * @author George Ma, Marcel Dausend
 *
 */
public class LetRulePlugin extends Plugin implements ParserPlugin,
		InterpreterPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 9, 3, "");

	public static final String PLUGIN_NAME = LetRulePlugin.class.getSimpleName();

	private Map<String, GrammarRule> parsers = null;

	private final static String KEYWORD_LET = "let";
	private final static String KEYWORD_in = "in";
	private final static String OPERATOR_ASSIGN = "=";
	private final static String OPERATOR_COLON = ",";

	private final String[] keywords = { KEYWORD_LET, KEYWORD_in };
	private final String[] operators = { OPERATOR_ASSIGN, OPERATOR_COLON };

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.coreasm.engine.Plugin#interpret(org.coreasm.engine.interpreter.Node)
	 */
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) {
		if (pos instanceof LetRuleNode) {
			LetRuleNode letNode = (LetRuleNode) (pos);
			// variables to look up values and variables
			Map<ASTNode, String> variableMap = null;
			List<ASTNode> letTerms = null;// lexically ordered

			// select terms for interpretation in lexical order and write extend
			// the local environment based corresponding to the let expression
			try {
				// init lookup constructs
				variableMap = letNode.getVariableMap();
				letTerms = letNode.getLetTermList();
			}
			catch (Exception e) {
				capi.error(e.getMessage(), pos, interpreter);
				return pos;
			}
			if (!letNode.getInRule().isEvaluated()) {
				for (ASTNode term : letTerms) {
					String var = variableMap.get(term);
					if (term.isEvaluated()) {
						letNode.addToEnvironment(var, term, interpreter);
					}
					if (!term.isEvaluated()) {
						return term;
					}

				}
				return letNode.getInRule();
			} else {
				// get the updates
				pos.setNode(null, letNode.getInRule().getUpdates(), null);
				// clear environment variables
				letNode.clearEnvironment(interpreter);
				return pos;
			}
		}
		return pos;
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
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();

			Parser<Node> letRuleParser = Parsers
					.array(new Parser[] {
							pTools.getKeywParser(KEYWORD_LET, PLUGIN_NAME),
							pTools.csplus(pTools.seq(idParser, pTools.getOprParser(OPERATOR_ASSIGN), termParser)),
							pTools.getKeywParser(KEYWORD_in, PLUGIN_NAME), ruleParser })
					.map(new LetRuleParseMap());
			parsers.put("Rule", new GrammarRule("LetRule",
					"'let' ID '=' Term (',' ID '=' Term )* 'in' Rule",
					letRuleParser, PLUGIN_NAME));
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

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public static class LetRuleParseMap // extends ParseMapN<Node> {
			extends ParserTools.ArrayParseMap {

		public LetRuleParseMap() {
			super(PLUGIN_NAME);
		}

		String nextChildName = "alpha";

		public Node map(Object[] vals) {
			nextChildName = "alpha";
			Node node = new LetRuleNode(((Node) vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}

		@Override
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode) {
				parent.addChild(nextChildName, child);
			} else {
				parent.addChild(child);
				if (child.getToken().equals(OPERATOR_ASSIGN)) // Term
					nextChildName = "beta";
				else if (child.getToken().equals(OPERATOR_COLON)) // ID
					nextChildName = "alpha";
				else if (child.getToken().equals(KEYWORD_in)) // Rule
					nextChildName = "gamma";
			}
		}

	}
}
