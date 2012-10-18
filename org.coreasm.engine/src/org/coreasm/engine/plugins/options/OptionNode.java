/*	
 * OptionNode.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.options;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * This node holds Option name-value pairs.
 *   
 * @see OptionsPlugin
 * 
 * @author  Roozbeh Farahbod
 * 
 */
public class OptionNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public OptionNode(ScannerInfo info) {
		super(OptionsPlugin.PLUGIN_NAME,
				ASTNode.DECLARATION_CLASS,
				"PropertyOption",
				null,
				info);
	}
	
	public OptionNode(OptionNode node) {
		super(node);
	}
	
	/**
	 * @return the name of this property/option
	 */
	public String getOptionName() {
		return getFirst().getToken();
	}

	/**
	 * @return the value of this property/option
	 */
	public String getOptionValue() {
		final ASTNode node = getFirst().getNext();
		//FIXME What do we want this node to be? a String? A term?
		if (node != null) {
			if (node.getToken() != null)
				return node.getToken();
			else
				if (node.getGrammarClass().equals(ASTNode.FUNCTION_RULE_CLASS))
					return node.getFirstASTNode().getToken();
				else
					return "";
		}
		else 
			return "";
	}
}
