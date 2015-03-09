/*
 * LetRuleNode.java
 * 
 * Copyright (C) 2006 George Ma
 * Copyright (C) 2007 Roozbeh Farahbod
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.ScannerInfo;

/**
 * CondtionalRuleNode is a NodeWrapper for conditional (ifThen) nodes.
 *
 * @author George Ma, Roozbeh Farahbod, Marcel Dausend
 *
 */
public class LetRuleNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	private List<String> letEnvVars = new ArrayList<String>();

	/**
	 * Creates a new LetRuleNode
	 */
	public LetRuleNode(ScannerInfo info) {
		super(LetRulePlugin.PLUGIN_NAME, ASTNode.RULE_CLASS, "LetRule", null, info);
	}

	public LetRuleNode(LetRuleNode node) {
		super(node);
	}

	/**
	 * Returns a map of the variable names to the nodes which represent the
	 * terms that will be aliased
	 *
	 * @throws Exception
	 */
	public Map<ASTNode, String> getVariableMap() throws CoreASMError {
		Map<ASTNode, String> variableMap = new HashMap<ASTNode, String>();

		ASTNode current = getFirst();

		while (current.getNext() != null) {
			if (variableMap.keySet().contains(current.getToken())) {
				throw new CoreASMError("Token \"" + current.getToken() + "\" already defined in let rule.", current);
			}
			else {
				variableMap.put(current.getNext(), current.getToken());
			}
			current = current.getNext().getNext();
		}
		return Collections.unmodifiableMap(variableMap);
	}

	/**
	 * Returns a map of the variable names to the nodes which represent the
	 * terms that will be aliased
	 *
	 * @throws Exception
	 */
	public List<ASTNode> getLetTermList() {
		List<ASTNode> termList = new ArrayList<ASTNode>();
		ASTNode current = getFirst().getNext();

		while (current != null) {
			termList.add(current);
			current = current.getNext().getNext();
		}
		return Collections.unmodifiableList(termList);
	}

	/**
	 * Returns the node representing the 'in' part the let rule.
	 */
	public ASTNode getInRule() {
		return (ASTNode) getChildNode("gamma");
	}

	/**
	 * extends the environment of the given interpreter by the variable of the
	 * given name with the given value
	 *
	 * @param var
	 * @param element
	 * @param interpreter
	 */
	public void addToEnvironment(String var, ASTNode element, Interpreter interpreter) throws CoreASMError {
		if (!letEnvVars.contains(var)) {
			letEnvVars.add(var);
			if (element.getValue() == null)
				throw new CoreASMError("The term for variable " + var + " has not yet been evaluated!", element);
			interpreter.addEnv(var, element.getValue());

		}
	}

	public void clearEnvironment(Interpreter interpreter) throws CoreASMError {
		for (String var : interpreter.getEnvVars().keySet()) {
			if (letEnvVars.contains(var)) {
				letEnvVars.remove(var);
				if (interpreter.getEnvVars().containsKey(var))
					interpreter.removeEnv(var);
				else
					throw new CoreASMError("The variable " + var +
							" cannot be removed from the interpeter's environment because is not contained, yet.", this);
			}
		}
	}

}
