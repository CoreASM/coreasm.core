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

import java.util.ArrayList;

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
		
		if (v[1] instanceof Node) {
			final ASTNode firstId = (ASTNode)v[1];
			node.addChild(firstId); 	// ID
			if (v[2] instanceof ArrayList) {
				@SuppressWarnings("unchecked")
				ArrayList<Object[]>furtherIds = (ArrayList<Object[]>)v[2];
				for (int i = 0; i < furtherIds.size();i++) {
					final Object[] tuple = (Object[])furtherIds.get(i);
					if (tuple[1] instanceof ASTNode) {
						node.addChild((ASTNode)tuple[1]); 	// ID
					}
				}
			}
		}

		final Object rule = v[v.length - 1];
		if (rule != null && rule instanceof ASTNode) {
			node.addChild("beta", (ASTNode)rule); 	// Rule
		}
		return node;
	}
	
}
