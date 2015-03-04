/*	
 * RuleDeclarationParseMap.java 	$Revision: 243 $
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
import org.coreasm.engine.parser.ParseMapN;

/** 
 * A parser map for rule declaration nodes.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class RuleDeclarationParseMap extends ParseMapN<Node> {

	public RuleDeclarationParseMap() {
		super(Kernel.PLUGIN_NAME);
	}
	
	public Node map(Object... vals) {
		ScannerInfo info = null;
		info = ((Node)vals[0]).getScannerInfo();
		
		Node node = new ASTNode(
				null,
				ASTNode.DECLARATION_CLASS,
				Kernel.GR_RULEDECLARATION,
				null,
				info
				);

		for (int i=0; i < vals.length; i++) {
			Node child = (Node)vals[i];
			if (child != null)
				// to give proper names to ASTNode children:
				if (child instanceof ASTNode) {
					if (((ASTNode)child).getGrammarClass().equals("RuleSignature"))
						node.addChild("alpha", child);
					else
						node.addChild("beta", child);
				} else
					node.addChild(child);
		}
		
		return node;
	}

}
