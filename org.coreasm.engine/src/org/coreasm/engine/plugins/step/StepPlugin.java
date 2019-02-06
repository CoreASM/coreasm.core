/*	
 * StepPlugin.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-04-30 01:05:27 +0200 (Fr, 30 Apr 2010) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.step;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserFragments;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;

/**
 * Plugin implementing the 'step' rule.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class StepPlugin extends Plugin implements ParserPlugin,
		InterpreterPlugin, VocabularyExtender {

	public static final String PLUGIN_NAME = StepPlugin.class.getSimpleName();
	public static final VersionInfo vinfo = new VersionInfo(1, 0, 1, "alpha");
	
	public static final String CTL_STATE_FUNC_NAME = "stepControlState";
	
	private String[] keywords = {"step", "then", "stepwise"};
	private String[] operators = {};

	private ParserFragments parsers;
	private HashSet<String> functionNames;
	private Map<String,FunctionElement> functions = null;
	
	/* Control states of agents in the system */
	private Map<Element, SystemControlState> controlStates;

	public StepPlugin() {
		functionNames = new HashSet<String>();
		functionNames.add(CTL_STATE_FUNC_NAME);
	}
	
	@Override
	public void initialize() throws InitializationFailedException {
		controlStates = new HashMap<Element, SystemControlState>();
	}

	protected synchronized SystemControlState getControlState(Element agent) {
		SystemControlState result = controlStates.get(agent);
		if (result == null) {
			result = new SystemControlState();
			controlStates.put(agent, result);
		}
		return result;
	}
	
	
	@Override
	public String[] getKeywords() {
		return keywords;
	}

	@Override
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	@Override
	public String[] getOperators() {
		return operators;
	}

	@Override
	public Parser<Node> getParser(String nonterminal) {
		final GrammarRule gr = getParsers().get(nonterminal);
		if (gr != null)
			return gr.parser;
		else
			return null;
	}

	@Override
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new ParserFragments();
			KernelServices kernel = (KernelServices)capi.getPlugin("Kernel").getPluginInterface();
			
			//Parser<Node> termParser = kernel.getTermParser();
			Parser<Node> ruleParser = kernel.getRuleParser();
			
			ParserTools pTools = ParserTools.getInstance(capi);

			final String grName = "StepRule";
			final String blockGRName = "StepBlock";
			
			Parser<Node> stepRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("step", PLUGIN_NAME),
						ruleParser, 
						pTools.getKeywParser("then", PLUGIN_NAME).optional(null),
						ruleParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node apply(Object[] vals) {
							Node node = new StepRuleNode(((Node)vals[0]).getScannerInfo());
							boolean first = true;
							for (Object o: vals) {
								if (o != null) {
									Node n = (Node)o;
									if (n instanceof ASTNode)
										if (first) {
											node.addChild("alpha", n);
											first = false;
										} else
											node.addChild("beta", n);
									else
										node.addChild(n);
								}
							}
							return node;
						}
		
			});
			
			final GrammarRule gr = new GrammarRule(grName, "'step' Rule 'then'? Rule", stepRuleParser, PLUGIN_NAME); 
			parsers.add(gr);
			
			Parser<Node> stepBlockRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("stepwise", PLUGIN_NAME),
						pTools.getOprParser("{"), 
						pTools.plus(ruleParser), 
						pTools.getOprParser("}")
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node apply(Object[] vals) {
							Node node = new StepBlockRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
		
			});
			
			final GrammarRule blockGR = new GrammarRule(blockGRName, "'stepwise' '{' Rule+ '}'", stepBlockRuleParser, PLUGIN_NAME); 
			parsers.add(blockGR);

			final GrammarRule stepRules = new GrammarRule("StepRules", gr.name + " | " + blockGRName, Parsers.or(stepRuleParser, stepBlockRuleParser), PLUGIN_NAME);
			parsers.add(stepRules);
			
			parsers.put("Rule", stepRules);
		}
		
		return parsers;
	}

	@Override
	public ASTNode interpret(Interpreter interpreter, ASTNode pos)
			throws InterpreterException {
		if (pos instanceof StepRuleNode) {
			StepRuleNode node = (StepRuleNode)pos;

			ASTNode alpha = node.getFirstRule();
			ASTNode beta = node.getSecondRule();
			SystemControlState ctlstate = getControlState(interpreter.getSelf());
			
			if (!alpha.isEvaluated() && !beta.isEvaluated()) {
				ControlStateElement ctlstate_alpha = uniqueCtlState(alpha, interpreter);
				if (ctlstate.contains(ctlstate_alpha))
					return alpha;
				else
					if (ctlstate.contains(uniqueCtlState(beta, interpreter)))
						return beta;
					else
						ctlstate.value.add(ctlstate_alpha);
			} else
				if (alpha.isEvaluated() && !beta.isEvaluated()) {
					ControlStateElement ctlstate_alpha = uniqueCtlState(alpha, interpreter);
					if (!substateExists(ctlstate_alpha, ctlstate)) {
						ctlstate.value.remove(ctlstate_alpha);
						ctlstate.value.add(uniqueCtlState(beta, interpreter));
					}
					pos.setNode(null, alpha.getUpdates(), null);
				} else {
					ControlStateElement ctlstate_beta = uniqueCtlState(beta, interpreter);
					if (!substateExists(ctlstate_beta, ctlstate)) 
						ctlstate.value.remove(ctlstate_beta);
					pos.setNode(null, beta.getUpdates(), null);
				}
		} else
			if (pos instanceof StepBlockRuleNode) {

				StepBlockRuleNode node = (StepBlockRuleNode)pos;
				ASTNode lastEvaluatedRule = null;
				SystemControlState ctlstate = getControlState(interpreter.getSelf());

				for (ASTNode cn: node.getAbstractChildNodes()) 
					if (cn.isEvaluated()) {
						lastEvaluatedRule = cn;
						break;
					}
				if (lastEvaluatedRule == null) {
					for (ASTNode cn: node.getAbstractChildNodes()) 
						if (ctlstate.contains(uniqueCtlState(cn, interpreter)))
							return cn;
					// implicit else
					ctlstate.value.add(uniqueCtlState(node.getFirst(), interpreter));
				} else {
					// if (lastEvaluatedRule != null) {
					ControlStateElement ctlstate_last = uniqueCtlState(lastEvaluatedRule, interpreter);
					if (!substateExists(ctlstate_last, ctlstate)) {
						ctlstate.value.remove(ctlstate_last);
						if (lastEvaluatedRule.getNext() != null)
							ctlstate.value.add(uniqueCtlState(lastEvaluatedRule.getNext(), interpreter));
					}
					pos.setNode(null, lastEvaluatedRule.getUpdates(), null);
				}
			}
		return pos;
	}

	private boolean substateExists(ControlStateElement cse, SystemControlState scs) {
		boolean result = false;
		for (ControlStateElement csei: scs.value)
			if (cse.isSuperControlStateOf(csei)) {
				result = true;
				break;
			}
		return result;
	}
	
	@Override
	public Set<String> getBackgroundNames() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, BackgroundElement> getBackgrounds() {
		return Collections.emptyMap();
	}

	@Override
	public Set<String> getFunctionNames() {
		return functionNames;
	}

	@Override
	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();
			functions.put(CTL_STATE_FUNC_NAME, new CtrlStateFunctionElement(this));
		}
		return functions;
	}

	@Override
	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, RuleElement> getRules() {
		return Collections.emptyMap();
	}

	@Override
	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, UniverseElement> getUniverses() {
		return Collections.emptyMap();
	}

	@Override
	public VersionInfo getVersionInfo() {
		return vinfo;
	}

	public ControlStateElement uniqueCtlState(ASTNode node, Interpreter interpreter) {
		return new ControlStateElement(interpreter.getCurrentCallStack(), node);
	}
}
