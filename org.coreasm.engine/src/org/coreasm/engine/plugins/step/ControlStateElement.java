/*	
 * ControlStateElement.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-04-30 01:05:27 +0200 (Fr, 30 Apr 2010) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Control state elements defined by the Step plugin.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class ControlStateElement extends Element {

	final Stack<? extends Object> callStack;
	final ASTNode node;
	final List<ASTNode> nodePathToRoot;
	
	public ControlStateElement(Stack<? extends Object> callStack, ASTNode node) {
		this.callStack = callStack;
		this.node = node;
		ArrayList<ASTNode> list = new ArrayList<ASTNode>();
		ASTNode n = node;
		list.add(n);
		while (n.getParent() != null) {
			list.add(n.getParent());
			n = n.getParent();
		}
		this.nodePathToRoot = Collections.unmodifiableList(list);
	}
	
	public boolean equals(Object o) {
		if (o instanceof ControlStateElement) {
			ControlStateElement cse = (ControlStateElement)o;
			//TODO equality on ASTNodes is not defined. Would it be OK?
			return cse.callStack.equals(this.callStack) && nodePathToRoot.equals(cse.nodePathToRoot);
		} else
			return false;
	}

	public int hashCode() {
		return this.callStack.hashCode() * 8 + this.nodePathToRoot.hashCode();
	}
	
	public boolean isSuperControlStateOf(ControlStateElement substate) {
		boolean result = !substate.equals(this);
		if (!result)
			return result;
		
		if (nodePathToRoot.size() > substate.nodePathToRoot.size())
			return false;
		
		for (int i=1; i <= nodePathToRoot.size(); i++) 
			if (!nodePathToRoot.get(nodePathToRoot.size() - i).equals(substate.nodePathToRoot.get(substate.nodePathToRoot.size() - i))) {
				result = false;
				break;
			}
		return result;
	}
	
	public String toString() {
		return "CSE:" + nodePathToRoot.toString();
	}
}
