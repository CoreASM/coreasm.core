/*	
 * FunctionRuleTermParseMap.java 	$Revision: 243 $
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

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.ParseMapN;

/** 
 * A parser map for the BasicFunctionRuleTerm grammar rule.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class FunctionRuleTermParseMap extends ParseMapN<Node> {

	private static final long serialVersionUID = 1L;

	public FunctionRuleTermParseMap() {
		super(Kernel.PLUGIN_NAME);
	}
	
	public Node map(Object... v) {
		Node node = new FunctionRuleTermNode(((Node)v[0]).getScannerInfo());
		node.addChild("alpha", (Node)v[0]); // ID
		
		for (int i=1; i < v.length; i++) {
			if (v[i] != null && v[i] instanceof ASTNode) {
				// Then it should be a TupleTerm
				for (Node n: ((Node)v[i]).getChildNodes())
					if (n instanceof ASTNode) 
						node.addChild("lambda", n);
					else 
						node.addChild(n);
			}
		}
		return node;
	}

}
