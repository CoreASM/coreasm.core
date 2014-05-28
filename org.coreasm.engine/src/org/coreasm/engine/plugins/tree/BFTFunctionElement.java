/*	
 * BFTFunctionElement.java
 * 
 * Copyright (C) 2010 Dipartimento di Informatica, Universita` di Pisa, Italy.
 *
 * Author: Franco Alberto Cardillo 		(facardillo@gmail.com)
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.engine.plugins.tree;

import java.util.List;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.list.ListBackgroundElement;
import org.coreasm.engine.plugins.list.ListElement;

/** 
 * Function returning an enumeration of the values contained in the tree performing a breadth first traversal
 *   
 * @author  Franco Alberto Cardillo (facardillo@gmail.com)
 */
public class BFTFunctionElement extends FunctionElement {

	public static final String BFT_FUNC_NAME = TreePlugin.TREE_PREFIX + "BFT";
	public static final String BFT_NODES_FUNC_NAME = TreePlugin.TREE_PREFIX + "BFTN";

	protected Signature signature = null;

	/* If valuesOnly is set to true, the BFT returns the values contained
	 * in the nodes. If set to false, the BFT returns the node themselves.
	 */
	protected boolean valuesOnly;

	public BFTFunctionElement(boolean valuesOnly) {
		this.valuesOnly = valuesOnly;
		setFClass(FunctionClass.fcMonitored);
	} // constructor

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!checkArguments(args))
			throw new CoreASMError("Illegal arguments for " + (valuesOnly ? BFT_FUNC_NAME : BFT_NODES_FUNC_NAME) + ".");
		
		TreeNodeElement node = (TreeNodeElement) args.get(0);

		// Enumeration
		if(valuesOnly)
			return new ListElement(node.BFT());
		else
			return new ListElement(node.BFTNodes());
	}

	@Override
	public Signature getSignature() {
		if (signature == null) {
			signature = new Signature();
			signature.setDomain(TreeBackgroundElement.TREE_BACKGROUND_NAME);
			signature.setRange(ListBackgroundElement.LIST_BACKGROUND_NAME);
		}
		return signature;
	}

	/*
	 * Checks the arguments of the function
	 */
	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 1) && (args.get(0) instanceof TreeNodeElement);
	}

} // BFTFunctionElement.java
