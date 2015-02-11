/*	
 * CoreModuleParseMap.java 	$Revision: 243 $
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
 
package org.coreasm.engine.plugins.modularity;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.parser.ParserTools;

/** 
 * A parser map for CoreASM module declarations.
 *   
 * @author Roozbeh Farahbod
 * 
 */
public class CoreModuleParseMap extends ParserTools.ArrayParseMap {

	public CoreModuleParseMap() {
		super(ModularityPlugin.PLUGIN_NAME);
	}
	
	public Node map(Object[] vals) {
		ScannerInfo info = null;
		
		// consider the possibility of starting with a 
		// comment or whitespace
		if (vals[0] != null && ((Node)vals[0]).getToken().equals("CoreModule"))
			info = ((Node)vals[0]).getScannerInfo();
		else
			info = ((Node)vals[1]).getScannerInfo();
		
		ASTNode rootNode = new ASTNode(
				ModularityPlugin.PLUGIN_NAME, 
				ASTNode.DECLARATION_CLASS, 
				"CoreModule", 
				null, 
				info
				);
		rootNode.setParent(null);

		addChildren(rootNode, vals);
		
		return rootNode;
	}

}
