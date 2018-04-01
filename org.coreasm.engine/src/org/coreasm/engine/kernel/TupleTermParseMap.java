/*	
 * TupleTermParseMap.java 	$Revision: 243 $
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
 * A parser map for the TupleTerm grammar rule.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class TupleTermParseMap extends ParseMap<Object[], Node> {

	public TupleTermParseMap() {
		super(Kernel.PLUGIN_NAME);
	}

	@Override
	public Node apply(Object[] v) {
		Node node = new ASTNode(
				null,
				"",
				"TupleTerm",
				null,
				((Node)v[0]).getScannerInfo());
		addChildren(node, v);
		return node;
	}

	public void addChildren(Node parent, Object[] children) {
		for (Object child: children) {
			if (child != null) {
				if (child instanceof Object[])
					addChildren(parent, (Object[])child);
				else
					if (child instanceof ASTNode) 
						parent.addChild("tupleterm", ((ASTNode)child));
					else
						parent.addChild((Node)child);
			}
		}
	}

}
