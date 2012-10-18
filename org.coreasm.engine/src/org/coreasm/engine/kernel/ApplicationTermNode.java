/*
 * ApplicationTermNode.java 		$Revision: 80 $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.util.Logger;

/**
 * Node for application terms.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class ApplicationTermNode extends ASTNode {

	private static final long serialVersionUID = 1L;

	private List<ASTNode> argsList = null;

	public ApplicationTermNode(ApplicationTermNode node) {
		super(node);
	}
	
	public ApplicationTermNode(Node firstNode) {
		super(Kernel.PLUGIN_NAME,
				ASTNode.FUNCTION_RULE_CLASS,
				"ApplicationTerm",
				null,
				firstNode.getScannerInfo());
	}
	
	/**
	 * @return the function-rule term part of this node
	 */
	public ASTNode getFunctionRuleNode() {
		return getFirst();
	}

	/**
	 * Returns the list of arguments in a <code>List</code> object.
	 * This method assumes that the node structure does not change after the 
	 * first call to this method.
	 */
	public List<ASTNode> getArguments() {
		if (argsList == null) {
			List<Node> args = getChildNodes("lambda");
			if (args.size() == 0)
				argsList = Collections.emptyList();
			else {
				argsList = new ArrayList<ASTNode>();
				
				for (Node n: args) 
					if (n instanceof ASTNode)
						argsList.add((ASTNode)n);
					else
						Logger.log(Logger.WARNING, Logger.parser, "Bad argument node in a FunctionRuleTerm!");
			}
		}
		return argsList;
	}

}
