/*	
 * EnumerateTreeFunctionElement.java
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
 * Function returning an enumeration of the nodes/values in the tree.
 * The traversal mode is the default one or the one specified by the user via the Options plugin.
 *   
 * @author  Franco Alberto Cardillo (facardillo@gmail.com)
 */
public class EnumerateTreeFunctionElement extends FunctionElement {

	// valuesOnly: if set to true, the returned collection
	// will contains the values in the nodes of the tree. If set to
	// false, the enumeration will contain the nodes themselves.
	protected boolean valuesOnly;

	public static final String ENUM_NODES_FUNC_NAME = TreePlugin.TREE_PREFIX + "Nodes";
	public static final String ENUM_VALUES_FUNC_NAME = TreePlugin.TREE_PREFIX + "Values";

	protected Signature signature = null;



	public EnumerateTreeFunctionElement(boolean valuesOnly) {
		this.valuesOnly = valuesOnly;
		setFClass(FunctionClass.fcMonitored);
	} // constructor

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!checkArguments(args))
			throw new CoreASMError("Illegal arguments for " + (valuesOnly ? ENUM_VALUES_FUNC_NAME : ENUM_NODES_FUNC_NAME) + ".");
		
		TreeNodeElement node = (TreeNodeElement) args.get(0);

		// Enumeration
		if(valuesOnly)
			return new ListElement(node.getValues());
		else 
			return new ListElement(node.getNodes());
	}


	@Override
	public Signature getSignature() {
		if (signature == null) {
			signature = new Signature();
			signature.setDomain(TreeBackgroundElement.TREE_BACKGROUND_NAME);
			signature.setRange(ListBackgroundElement.LIST_BACKGROUND_NAME);
		}
		return signature;
	} // getSignature

	/*
	 * Checks the arguments of the function
	 */
	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 1) && (args.get(0) instanceof TreeNodeElement);
	}

} // EnumerateTreeFunctionElement.java
