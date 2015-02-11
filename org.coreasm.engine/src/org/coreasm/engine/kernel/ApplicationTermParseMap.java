/*
 * ApplicationTermParseMap.java 		$Revision: 80 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.kernel;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.ParseMapN;

/**
 * Parse map for application term parser.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class ApplicationTermParseMap extends ParseMapN<Node> {

	public ApplicationTermParseMap() {
		super(Kernel.PLUGIN_NAME);
	}
	
	public Node map(Object... vals) {
		Node node = new ApplicationTermNode((Node)vals[0]);
		node.addChild("alpha", (Node)vals[0]); // FunctionRuleTerm
		
		for (int i=1; i < vals.length; i++) {
			if (vals[i] != null && vals[i] instanceof ASTNode) {
				// Then it should be a TupleTerm
				for (Node n: ((Node)vals[i]).getChildNodes())
					if (n instanceof ASTNode) 
						node.addChild("lambda", n);
					else 
						node.addChild(n);
			}
		}
		return node;
	}

}
