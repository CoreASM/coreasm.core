/*	
 * StringNode.java  	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.string;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Node for string values.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class StringNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	public StringNode(StringNode node) {
		super(node);
	}
	
	public StringNode(String token, ScannerInfo info) {
		super(
			StringPlugin.PLUGIN_NAME,
			ASTNode.EXPRESSION_CLASS,
			"StringTerm",
			token,
			info,
			Node.LITERAL_NODE);
	}
	
	/**
	 * Overrides unparse to put quotes around the given text.
	 */
	@Override
	public String unparse() {
		return "\"" + super.unparse() + "\"";
	}
	
}
