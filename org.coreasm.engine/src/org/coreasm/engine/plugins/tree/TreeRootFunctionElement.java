/*	
 * TreeRootFunctionElement.java
 * 
 * Copyright (C) 2010 Dipartimento di Informatica, Universita` di Pisa, Italy.
 * Author: Franco Alberto Cardillo (facardillo@gmail.com)
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 */
 
package org.coreasm.engine.plugins.tree;

import java.util.List;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Signature;

/** 
 * Function that returns the root node of a tree
 *   
 * @author  Franco Alberto Cardillo (facardillo@gmail.com)
 * 
 */
public class TreeRootFunctionElement extends FunctionElement {
	
	// Name of the function (keyword for the lexer)
	public static final String TREE_ROOT_FUNC_NAME = TreePlugin.TREE_PREFIX + "Root";
	
	// Signature of the function
	protected Signature signature = null;
	
	
	/*
	 * Sole constructor. Creates a new TreeRootFunctionElement
	 */
	public TreeRootFunctionElement() {
		setFClass(FunctionClass.fcDerived);
	} // constructor

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!checkArguments(args))
			throw new CoreASMError("Illegal arguments for " + TREE_ROOT_FUNC_NAME + ".");
		
		TreeNodeElement node = (TreeNodeElement) args.get(0);
		TreeNodeElement root = node.getRoot();
		if(root != null)
			return root;
		return Element.UNDEF;
	}

	@Override
	public Signature getSignature() {
		if (signature == null) {
			signature = new Signature();
			signature.setDomain(TreeBackgroundElement.TREE_BACKGROUND_NAME);
			signature.setRange(TreeBackgroundElement.TREE_BACKGROUND_NAME);
		}
		return signature;
	} // getSignature
	
	/*
	 * Checks the arguments of the function.
	 * The function expects one argument of type TreeNodeElement
	 * 
	 * @param args The values this function is to be evaluated on.
	 */
	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 1) && (args.get(0) instanceof TreeNodeElement);
	} // checkArguments

} // TreeRootFunctionElement.java
