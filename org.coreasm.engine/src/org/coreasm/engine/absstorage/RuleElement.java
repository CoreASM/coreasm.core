/*	
 * RuleElement.java 	1.0 	$Revision: 243 $
 * 
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
 
package org.coreasm.engine.absstorage;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.coreasm.engine.interpreter.ASTNode;

/** 
 *	This class implements a named ASM rule.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class RuleElement extends Element {

	/**
	 * Declaration node in the parse tree.
	 */
	public final ASTNode node;
	
	/**
	 * The body of this rule
	 */
	private ASTNode body = null;
	
	/**
	 * Parameter tokens of this rule
	 */
	private List<String> param = null;
	
	/** Name of the rule at the time of declaration. */
	public final String name;
	
	/**
	 * Creates a new rule with a 
	 * list of parameter tokens (<code>String</code>)
	 * and a rule body.
	 * 
	 * @param node declaration node of this rule
	 * @param name name of this rule
	 * @param param list of parameter tokens
	 * @param body body of the rule
	 */
	public RuleElement(ASTNode node, String name, List<String> param, ASTNode body) {
		super();
		this.node = node;
		setBody(body);
		setParam(param);
		this.name = name;
	}

	public String getBackground() {
		return RuleBackgroundElement.RULE_BACKGROUND_NAME;
	}
	
	/**
	 * @return the declaration node of this rule in the parse tree
	 */
	public ASTNode getDeclarationNode() {
		return node;
	}
	
	/**
	 * Gets the body of this rule.
	 * 
	 * @return Returns the body.
	 */
	public org.coreasm.engine.interpreter.ASTNode getBody() {
		return body;
	}

	/**
	 * Sets the body of this rule.
	 * 
	 * @param body The body to set.
	 */
	public void setBody(org.coreasm.engine.interpreter.ASTNode body) {
		if (body == null) 
			throw new IllegalArgumentException("Body of a rule cannot be null.");
		else
			this.body = body;
	}

	/** Returns the original name of this rule */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parameter tokens list of this
	 * rule.
	 * 
	 * @return Returns the param.
	 */
	public List<String> getParam() {
		return param;
	}

	/**
	 * Sets the parameter tokens of this rule. 
	 * If <code>param</code> is null, an empty
	 * list will be used instead.
	 * 
	 * @param param The param to set.
	 */
	public void setParam(List<String> param) {
		if (param == null || param.size() == 0)
			this.param = Collections.emptyList();
		else
			this.param = Collections.unmodifiableList(new ArrayList<String>(param));
	}

	public String toString() {
		return "@" + this.getName();
	}
}
