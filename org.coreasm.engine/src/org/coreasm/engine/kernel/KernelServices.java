/*	
 * KernelServices.java 	$Revision: 243 $
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
 
package org.coreasm.engine.kernel;

import org.jparsec.Parser;

import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugin.PluginServiceInterface;

/** 
 * Provides kernel-specific services to other plugins.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class KernelServices implements PluginServiceInterface {

	private final Kernel kernel;
	
	protected KernelServices(Kernel kernel) {
		this.kernel = kernel;
	}

	/**
	 * @return the Rule parser hook from the Kernel plugin
	 */
	public Parser<Node> getRuleParser() {
		return kernel.getParser("Rule");
	}

	/**
	 * @return the Guard parser hook from the Kernel plugin
	 */
	public Parser<Node> getGuardParser() {
		return kernel.getParser("Term");
	}

	/**
	 * @return the Term parser hook from the Kernel plugin
	 */
	public Parser<Node> getTermParser() {
		return kernel.getParser("Term");
	}

	/**
	 * @return the BasicTerm parser hook from the Kernel plugin
	 */
	public Parser<Node> getBasicTermParser() {
		return kernel.getParser("BasicTerm");
	}

	/**
	 * @return the BasicExpr parser hook from the Kernel plugin
	 */
	public Parser<Node> getBasicExprParser() {
		return kernel.getParser("BasicExpr");
	}

	/**
	 * @return the ConstantTerm parser hook from the Kernel plugin
	 */
	public Parser<Node> getConstantTermParser() {
		return kernel.getParser("ConstantTerm");
	}

	/**
	 * @return the FunctionRuleTerm parser hook from the Kernel plugin
	 */
	public Parser<Node> getFunctionRuleTermParser() {
		return kernel.getParser("FunctionRuleTerm");
	}

	/**
	 * @return the Header parser hook from the Kernel plugin
	 */
	public Parser<Node> getHeaderParser() {
		return kernel.getParser("Header");
	}

	/**
	 * @return the RuleSignature parser hook from the Kernel plugin
	 */
	public Parser<Node> getRuleSignatureParser() {
		return kernel.getParser("RuleSignature");
	}

	/**
	 * @return the TupleTerm parser hook from the Kernel plugin
	 */
	public Parser<Node> getTupleTermParser() {
		return kernel.getParser("TupleTerm");
	}
	
	/**
	 * Returns the parser component of the kernel that is associated with the 
	 * given grammar rule. Calling this rule makes sense only after
	 * the Kernel has gathered all the plugin components (e.g., after parsing).
	 * 
	 * @param grammarRule name of a grammar rule
	 * @return the parser component of the kernel that is associated with the 
	 * 			given grammar rule
	 */
	public Parser<Node> getParserComponent(String grammarRule) {
		return kernel.getParsers().get(grammarRule).parser;
	}
	
}
