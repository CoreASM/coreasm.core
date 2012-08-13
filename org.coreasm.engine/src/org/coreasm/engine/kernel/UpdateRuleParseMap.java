/*	
 * UpdateRuleParseMap.java 	$Revision: 243 $
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
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.parser.ParseMap;
import org.coreasm.engine.parser.ParseMap3;
import org.coreasm.engine.parser.ParseMap5;
import org.coreasm.engine.parser.ParseMapN;

/** 
 * A parser map for the update rule form.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class UpdateRuleParseMap extends ParseMap<Object[], Node> {

	private static final long serialVersionUID = 1L;

	public UpdateRuleParseMap() {
		super(Kernel.PLUGIN_NAME);
	}

	public Node map(Object[] v) {
		Node node = new UpdateRuleNode(((Node)v[0]).getScannerInfo());
		
		for (int i=0; i < v.length; i++) {
			if (i == 0)
				node.addChild("alpha", (Node)v[i]); 	// LHS
			else
				if (i == 2) 
					node.addChild("beta", (Node)v[i]); 	// LHS
				else
					if (v[i] != null)
						node.addChild((Node)v[i]);  	// whitespace or ':='
		}
		return node;
	}
	
}
