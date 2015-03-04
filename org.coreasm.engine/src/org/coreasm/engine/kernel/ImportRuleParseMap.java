/*	
 * ImportRuleParseMap.java 	$Revision: 243 $
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
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.ParseMap;

/** 
 * A parser map for the import rule form.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class ImportRuleParseMap extends ParseMap<Object[], Node> {

	public ImportRuleParseMap() {
		super(Kernel.PLUGIN_NAME);
	}

	public Node map(Object[] v) {
		Node node = new ASTNode(
				null,
				ASTNode.RULE_CLASS,
				"ImportRule",
				null,
				((Node)v[0]).getScannerInfo()
				);
		
		for (int i=0; i < v.length; i++) {
			if (i == 2)
				node.addChild("alpha", (Node)v[i]); 	// ID
			else
				if (i == 6) 
					node.addChild("beta", (Node)v[i]); 	// Rule
				else
					if (v[i] != null)
						node.addChild((Node)v[i]);  	// whitespace or keywords
		}
		return node;
	}
	
}
