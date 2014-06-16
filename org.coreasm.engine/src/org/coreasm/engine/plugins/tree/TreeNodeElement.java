/*	
 * TreeNodeElement.java
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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.plugins.number.NumberElement;


// implements Enumerable
public class TreeNodeElement extends Element implements Enumerable {

	public static final String TREE_PREFIX = "tree";
	public static final String TREE_PARENT = TREE_PREFIX + "Parent";
	public static final String TREE_VALUE = TREE_PREFIX + "Value";
	public static final String TREE_FIRST = TREE_PREFIX + "First";
	public static final String TREE_NEXT = TREE_PREFIX + "Next";
	
	public static final String UNDEF_STRING = "undef";

	public static final String TREE_TRAVERSAL_OPT_DF = "depth-first";
	public static final String TREE_TRAVERSAL_OPT_BF = "breadth-first";
	public static final String TREE_TRAVERSAL_OPT_DEFAULT = TREE_TRAVERSAL_OPT_DF;

	protected String traversalMode = null;


	public static final String TREE_OUTPUT_STRING_OPT_LONG = "long";
	public static final String TREE_OUTPUT_STRING_OPT_SHORT = "short";
	public static final String TREE_OUTPUT_STRING_OPT_DEFAULT = TREE_OUTPUT_STRING_OPT_SHORT;


	// Used to format the output string 
	protected static final String L_BRACKET = "(";
	protected static final String R_BRACKET = ")";

	// Chosen format for the output strings
	protected  String outputStringFormat = null;

	// (User-defined) Data contained in the node
	// protected Element value;

	
	// Abstract Storage
	protected AbstractStorage storage;
	protected ControlAPI capi;
	
	
	// *************************************************
	protected Map<Location, List<Element>> cachedUpdates;
	protected List<TreeNodeElement> unreachableUpdatedNodes;
	

	/**
	 * Create a new TreeNodeElement with value set to Element.UNDEF and no parent
	 */
	public TreeNodeElement() {
		this(Element.UNDEF);
	} // NodeElement

	/**
	 * Create a new TreeNodeElement with value set to aValue and no parent
	 * @param aValue the Element instance the new node will contain
	 */
	public TreeNodeElement(Element aValue) {
		Element val = (aValue == null) ? Element.UNDEF : aValue;
		this.capi = getCAPI();
		this.storage = getAbstractStorage();
		cachedUpdates = new HashMap<Location, List<Element>>();
		unreachableUpdatedNodes = new LinkedList<TreeNodeElement>();
		if(val != Element.UNDEF)
			setValue(val);
	} // NodeElement


	protected static List<Element> parlist(Element param) {
		List<Element> list = new LinkedList<Element>();
		list.add(param);
		return list;
	}

	
	// **********************************************************************************************
	
	protected ControlAPI getCAPI() {
		return TreePlugin.getCAPI();
	} // getCAPI
	
	protected AbstractStorage getAbstractStorage() {
		return TreePlugin.getAbstractStorage();
	} // getAbstractStorage
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof TreeNodeElement && o==this);
	} // equals
	
	protected boolean isValidIndex(int idx) {
		return idx > 0;
	} // is ValidIndex
	
	protected boolean isValidIndex(Element index) {
		return ( (index instanceof NumberElement) && ((NumberElement) index).intValue() > 0);
	} // isValidIndex

	
//	protected boolean isValidIndex(NumberElement index) { 
//		return index.isNatural() && index.getValue() < Integer.MAX_VALUE;	
//	} // isValidIndex

	
	// **********************************************************************************************
	public String getOutpuStringFormat() {
		if(outputStringFormat == null) {
			outputStringFormat = TREE_OUTPUT_STRING_OPT_DEFAULT;
			if(TreePlugin.optionsReader != null) {
				String optValue = TreePlugin.optionsReader.getOutputStringFormatOption();
				setOutputStringFormat(optValue);
			} // if treePlugin
		} // if outputStringFormat != null
		return outputStringFormat;
	} // getOutputStringFormat;


	public void setOutputStringFormat(String format) {
		outputStringFormat = TREE_OUTPUT_STRING_OPT_DEFAULT;
		
		if(format != null && 
				( format.equals(TREE_OUTPUT_STRING_OPT_LONG ) || format.equals(TREE_OUTPUT_STRING_OPT_SHORT) ) )
			outputStringFormat = format;
	} // setOutputStringFormat


	public String getTraversalMode () {
		if (traversalMode == null) {
			traversalMode = TREE_TRAVERSAL_OPT_DEFAULT;
			if(TreePlugin.optionsReader != null) {
				String optValue = TreePlugin.optionsReader.getTreeTraversalOption();
				setTraversalMode(optValue);
			} // if treePlugin
		} // if
		
		return traversalMode;
	} // getTraversalMode

	public void setTraversalMode(String modeString) {
		traversalMode = TREE_TRAVERSAL_OPT_DEFAULT;
		
		if(modeString != null &&
				(modeString.equals(TREE_TRAVERSAL_OPT_BF) || modeString.equals(TREE_TRAVERSAL_OPT_DF) ) )
			traversalMode = modeString;
	} // setTraversalMode
	
	// **********************************************************************************************
	// **********************************************************************************************
	// **********************************************************************************************
	public void setValue(Element newValue) {
		Location valLoc = InternalUpdate.buildValueLocation(this);
		List<Element> values = cachedUpdates.get(valLoc);
		if(values == null) {
			values = new LinkedList<Element>();
			cachedUpdates.put(valLoc, values);
		}
		values.add(newValue);
	} // setValue

	
	protected Element getTempValue() {
		try {
			// XXX XXX
			Element value;
			Location myLoc = InternalUpdate.buildValueLocation(this);
			List<Element> list = cachedUpdates.get(myLoc);
			if(list == null)
				value = storage.getValue(myLoc);
			else {
				value = list.get(0);
			}
			return value;
		} catch (InvalidLocationException ex) {
			capi.error(ex.getMessage());
			return null;
		} // try ..
		
	} // getTempValue
	
	
	public Element getValue() {
		try {
			Location myLoc = InternalUpdate.buildValueLocation(this);
			return storage.getValue(myLoc);
		} catch (InvalidLocationException ex) {
			capi.error(ex.getMessage());
			return null;
		} // try ... catch
	} // getValue
	
	
	// **********************************************************************************************
	
	public void setParent(TreeNodeElement newParent) {
		Location myLoc = InternalUpdate.buildParentLocation(this);
		
		List<Element> list = cachedUpdates.get(myLoc);
		if(list == null) {
			list = new LinkedList<Element>();
			cachedUpdates.put(myLoc, list);
		}
		list.add(newParent);
		// cachedUpdates.add(u);
	} // setParent

	
	protected TreeNodeElement getTempParent() {
		try {
			Location parentLoc = InternalUpdate.buildParentLocation(this); 
			
			Element parent;
			List<Element> list = cachedUpdates.get(parentLoc); 
			
			if(list == null)
				parent = storage.getValue(parentLoc);
			else
				parent = list.get(0);
			
			// XXX
			if(parent == null || parent == Element.UNDEF)
				return null;
			
			if(! (parent instanceof TreeNodeElement)) {
				throw new InvalidLocationException("TreeNodeElement required, found a " + 
						parent.getClass().getSimpleName());
			} // if

			return ((TreeNodeElement) parent);
		} catch (InvalidLocationException ex) {
			capi.error(ex);
			// XXX
			return null;
		} // try ... catch
	} // getTempParent
	
	
	protected TreeNodeElement getParent() {
		try {
			Location parentLoc = InternalUpdate.buildParentLocation(this);
			Element parent = storage.getValue(parentLoc);
			return parent != Element.UNDEF ? ( (TreeNodeElement) parent ) : null;
		} catch (InvalidLocationException ex) {
			capi.error(ex.getMessage());
			return null;
		} // try ... catch
	} // getParent
	
	
	// **********************************************************************************************
	
	public void setFirst(TreeNodeElement first) {
		Location myLoc = InternalUpdate.buildFirstLocation(this);	
		List<Element> list = cachedUpdates.get(myLoc);
		if(list == null) {
			list = new LinkedList<Element>();
			cachedUpdates.put(myLoc, list);
		}
		list.add(first);
	} // setFirst


	protected TreeNodeElement getTempFirst() {
		try {
			// XXX myLoc can be cached?
			Element firstEl;
			
			Location myLoc = InternalUpdate.buildFirstLocation(this);
			List<Element> list = cachedUpdates.get(myLoc);
			if(list==null) {
				
				firstEl = storage.getValue(myLoc);
				// System.err.println("FIRST in storage: " + firstEl);
				
			} else {
				
				firstEl = list.get(0);
			}
			// XXX
			if (firstEl == Element.UNDEF)
				return null;
			
			if(! (firstEl instanceof TreeNodeElement)) {
				throw new InvalidLocationException("TreeNodeElement required, found a " + 
						firstEl.getClass().getSimpleName());
			} // if
			TreeNodeElement first = (TreeNodeElement) firstEl;
			return first;
		} catch (InvalidLocationException ex) {
			capi.error(ex.getMessage());
			return null;
		} // try ..
		
	} // getTempFirst
	
	public TreeNodeElement getFirst() {
		try {
			Location myLoc = InternalUpdate.buildFirstLocation(this);
			Element first = storage.getValue(myLoc);
			return first != Element.UNDEF ? ( (TreeNodeElement) first) : null;
		} catch (InvalidLocationException ex) {
			capi.error(ex.getMessage());
			return null;
		} // try ... catch
	} // getFirst
	
	// **********************************************************************************************
	
	public void setNext(TreeNodeElement next) {
		Location myLoc = InternalUpdate.buildNextLocation(this);	
		List<Element> list = cachedUpdates.get(myLoc);
		if(list == null) {
			list = new LinkedList<Element>();
			cachedUpdates.put(myLoc, list);
		}
		list.add(next);
	} // setNext

	
	protected TreeNodeElement getTempNext() {
		try {
			// XXX myLoc can be cached?
			Location myLoc = InternalUpdate.buildNextLocation(this);
			
			List<Element> list = cachedUpdates.get(myLoc);
			
			Element nextEl;
			if(list==null) {
				nextEl = storage.getValue(myLoc);
			} else {
				nextEl = list.get(0);
			}
			
			if (nextEl == Element.UNDEF || nextEl == null)
				return null;
			
			if(! (nextEl instanceof TreeNodeElement)) {
				throw new InvalidLocationException("TreeNodeElement required, found a " + 
						nextEl.getClass().getSimpleName());
			} // if
			
			TreeNodeElement next = (TreeNodeElement) nextEl;
			return next;
		} catch (InvalidLocationException ex) {
			capi.error(ex.getMessage());
			return null;
		} // try ..
		
	} // getNext
	
	public TreeNodeElement getNext() {
		try {
			Location myLoc = InternalUpdate.buildNextLocation(this);
			Element next = storage.getValue(myLoc);
			return next != Element.UNDEF ? ( (TreeNodeElement) next) : null;
		} catch (InvalidLocationException ex) {
			capi.error(ex.getMessage());
			return null;
		} // getNext
	} // getNext
	
	// **********************************************************************************************
	// **********************************************************************************************
	// **********************************************************************************************
	
	
	/*
	 * Detach aNode from its parent: 
	 *  [1] if aNode is the first child, produce an update for FIRST of the parent
	 * 
	 *	[2] the next sibling of aNode's previous sibling is updated.
	 */
	
	
	// OPERATIONS ON CURRENT STATE
	protected void detachFromParent() {
		TreeNodeElement parent = getParent();
		
		
		if(parent == null || parent == Element.UNDEF)
			return;
		
		TreeNodeElement firstCh = parent.getFirst();
		
		if(firstCh == this) {
			TreeNodeElement nextCh = getNext();
			parent.setFirst(nextCh);
			unreachableUpdatedNodes.add(parent);
			// System.err.println("detach... now parent is " + parent);
		} else {
			TreeNodeElement previousSibling = parent.getChildBefore(this);
			if(previousSibling != null) {
				previousSibling.setNext(getNext());
				unreachableUpdatedNodes.add(previousSibling);
			} // if previousSibling
		} // if firstCh == this
		
	} // detachNodeFromParent

	
	//
	// In this method operations are performed on TEMP values (used when building a tree by adding several nodes) 
	//
	public void add(TreeNodeElement anotherNode) {

		if(anotherNode == null)
			throw new IllegalArgumentException("Cannot add a null child");
		
		if(anotherNode.getTempParent() == this)
			throw new IllegalArgumentException("Cannot add a node as a child of the same node twice");
		
		
		
		// System.err.println("Adding node with value " + anotherNode.getTempValue());
		
		// Detachfromparent will look into CURRENT STATE
		anotherNode.detachFromParent();
		anotherNode.setParent(this);
		
		// [3] Operations on TEMP state.
		// Update for the last child: its next becomes anotherNode
		TreeNodeElement lastChild = getTempLastChild();
		if(lastChild != null) {
			lastChild.setNext(anotherNode);
		} else {
			setFirst(anotherNode);
		}
		
		
	} // add

	
	public void insert(TreeNodeElement aNode, Element index) {
		if(index == null)
			throw new IllegalArgumentException("Index value cannot be null");

		if(! (index instanceof NumberElement
				&& isValidIndex( (NumberElement) index))) 
			throw new IllegalArgumentException("Illegal index parameter");

		int idx = ((NumberElement) index).intValue();
		insert(aNode, idx);
	} // insert
	
	
	public void insert(TreeNodeElement aNode, int index) {
		// Checks on parameters
		
		if(aNode == null)
			throw new IllegalArgumentException("Cannot add a null child");
		
		if(index < 1)
			throw new IllegalArgumentException("Illegal index value: " + index);
		
		
		if(aNode.getTempParent() == this)
			throw new IllegalArgumentException("Cannot add a node as a child of the same node twice");
		
		
		// [1] detach aNode from its parent and update - if necessary - FIRST of the
		// parent
		// Operation on current state
		aNode.detachFromParent();
		aNode.setParent(this);
		
		// Special case: index == 1 -> aNode is the first child
		if(index == 1) {
			TreeNodeElement currentFirstChild = getTempFirst();
			System.out.println("---- CURRENT FIRST CHILD IS " + currentFirstChild);
			setFirst(aNode);
			
			aNode.setNext(currentFirstChild);
			
		} else {
			TreeNodeElement child1 = getTempFirst();
			
			// No first child and index != 1 -> Exception
			if(child1 == null)
				throw new IllegalArgumentException("Cannot add a node at position " + index + ": the tree has no children");
			
			TreeNodeElement child2 = child1.getTempNext();
			
			int i = 1;
			while(i < index-1 && child2 != null) {
				child1 = child2;
				child2 = child2.getTempNext();
				i++;
			} // while
			
			if(i==index-1) {
				child1.setNext(aNode);
				aNode.setNext(child2);
				
			} else {
				throw new IllegalArgumentException("Cannot add at position " + index);
			}
			
		} // if index		
	} // insert

	
	
	public void removeChild(TreeNodeElement aChild) {
		if(aChild == null)
			throw new IllegalArgumentException("Cannot remove a null child");
		
		aChild.detachFromParent();
		aChild.setParent(null);
		aChild.setNext(null);
	} // removeChild


	public void removeChildAtIndex(Element index) {
		if(index == null)
			throw new IllegalArgumentException("Index value cannot be null");

		// int nChildren = getChildCount().intValue();

		if(! (index instanceof NumberElement
				&& isValidIndex( (NumberElement) index))) 
			throw new IllegalArgumentException("Illegal index parameter");

		int idx = ((NumberElement) index).intValue();
		removeChildAtIndex(idx);
	} // removeChildAtIndex
	
	
	
	
	public void removeChildAtIndex(int index) {
		
		TreeNodeElement child = getTempFirst();

		if(index == 1) {
			if(child == null)
				throw new IllegalArgumentException("No child at position " + index);
			
			// setFirst(child.getTempNext());
			child.detachFromParent();
			child.setParent(null);
			child.setNext(null);
		} else {
			TreeNodeElement prevChild = child;
			child = child.getTempNext();
			int i = 2;
			while(i < index && child != null) {
				prevChild = child;
				child = child.getTempNext();
				i++;
			} // while
			
			if(i == index) {
				
				if(child == null) 
					throw new IllegalArgumentException("No child at position " + index);
				
				
				child.detachFromParent();
				child.setParent(null);
				child.setNext(null);
				// prevChild.setNext(null);
			} // if
			
		} // if ... else
	} // remove	
	
	
	protected List<InternalUpdate> getNodeUpdates() {
		List<InternalUpdate> list = new LinkedList<InternalUpdate>();
		
		for(Location loc : cachedUpdates.keySet()) {
			List<Element> values = cachedUpdates.get(loc);
			for(Element value : values) {
//				System.out.println("GETNODEUPDATES: ");
//				System.out.println("LOC: " + loc);
//				System.out.println("VAL: " + value);
				list.add(new InternalUpdate(loc, value));
			} // int for
		} // ext for
		
		
		for(TreeNodeElement aNode : unreachableUpdatedNodes) {
			list.addAll(aNode.getNodeUpdates());
		}
		
		clearUpdates();
		return list;
		
	} // getNodeUpdates
	
	protected List<InternalUpdate> getTreeUpdates() {
		List<InternalUpdate> list = new LinkedList<InternalUpdate> ();
		TreeNodeElement child = getTempFirst();
		
//		Vector<TreeNodeElement> children = new Vector<TreeNodeElement>();
		
		
		
		
		while (child != null) {
//			System.err.println("Adding updates from child with temp value " + child.getTempValue());
			TreeNodeElement nextChild = child.getTempNext();
			List<InternalUpdate> listFromChild = child.getTreeUpdates();
			list.addAll(listFromChild);
//			 children.add(child);
			child = nextChild;
		} // while
		list.addAll(getNodeUpdates());
		
//		for(TreeNodeElement c : children) {
//			c.clearUpdates();
//		}
		
		return list;
	} // getTreeUpdates
	
	
	protected void clearUpdates() {
		cachedUpdates.clear();
		unreachableUpdatedNodes.clear();
	} // clearUpdates
	
	
	// **********************************************************************************************
	// **********************************************************************************************
	// **********************************************************************************************
	
	  
	public boolean isRoot() {
		return getParent() != null;
	} // isRoot


	public boolean isLeaf() {
		return (getFirst() == null);
	} // isLeaf


	public NumberElement getSiblingCount() {
		int nSiblings = -1;
		TreeNodeElement nextSibling;
		
		do {
			nSiblings++;
			nextSibling = getNext();
		} while (nextSibling != null);
		
		return NumberElement.getInstance(nSiblings);
	} // getSiblingCount


	public TreeNodeElement getRoot() {
		TreeNodeElement root = this;
		TreeNodeElement currentRoot = getParent();
		// if the node has no parent, the method will return this.
		if(currentRoot != null) {
			while(currentRoot != null) {
				root = currentRoot;
				currentRoot = currentRoot.getParent();
			} // while
		}
		return root;
	} // getRoot


	public Collection<? extends Element> getLeaves() {
		List<Element> list = new LinkedList<Element>();
		getLeavesRec(list);
		return list;
	} // getLeaves

	
	protected void getLeavesRec(List<Element> list) {
		TreeNodeElement child = getFirst();
		if(child == null) {
			list.add(this);
		} else {
			while(child != null) {
				child.getLeavesRec(list);
				child = child.getNext();
			} // while
		} // else
	} // getLeavesRec

	
	public Collection<? extends Element> getNodes() {
		// parameter set to false: getNodes returns an enumeration of the nodes, not their values.
		return traverseTree(false);			
	} // getNodes

	
	public Collection<? extends Element> getValues() {
		// parameter set to true: getValues returns an enumeration of the values in the tree.
		return traverseTree(true);			
	} // getNodes


	
	public boolean isNodeChild(TreeNodeElement anotherNode) {
		return ( anotherNode != null && anotherNode.getParent() == this );
	} // isNodeChild


	public boolean isNodeSibling(TreeNodeElement anotherNode) {
		return ( anotherNode != null && anotherNode.getParent() == this.getParent());
	} // isNodeSibling
	

	protected NumberElement getTempChildCount() {
		int nChildren = 0;
		TreeNodeElement child = getTempFirst();
		while (child != null) {
			nChildren = nChildren + 1;
			child = child.getTempFirst();
		} 
		return NumberElement.getInstance(nChildren);
	} // getChildCount

	
	public NumberElement getChildCount() {
		int nChildren = 0;
		TreeNodeElement child = getFirst();
		while (child != null) {
			nChildren = nChildren + 1;
			child = child.getFirst();
		} 
		return NumberElement.getInstance(nChildren);
	} // getChildCount

	
	public boolean isNodeRelated(TreeNodeElement anotherNode) {
		return (anotherNode != null && anotherNode.getRoot() == getRoot());
	} // isNodeRelated

	
	
	public TreeNodeElement getChildAtIndex(NumberElement index) {
		int idx = index.intValue();
		if(! isValidIndex(idx))
			return null;
		
		TreeNodeElement currentChild = getFirst();
		for(int i=1; i<=idx-1 && (currentChild!=null); i++) {
			currentChild = currentChild.getNext();
		} // for
		return currentChild;
	} // getChildAtIndex


	public TreeNodeElement getChildAfter(TreeNodeElement aChild) { 
		// XXX Check 'null' -- defaultValue
		TreeNodeElement child = getFirst();
		
		while(child != null && child != aChild)
			child = child.getNext();
		
		return child.getNext();
	} // getChildAfter


	public TreeNodeElement getFirstChild() {
		return getFirst();
	} // getFirstChild

	protected TreeNodeElement getTempFirstChild() {
		return getTempFirst();
	} // getFirstChild

	
	protected TreeNodeElement getTempLastChild() {
		TreeNodeElement child = getTempFirst();
		TreeNodeElement lastChild = null;
		while (child != null) {
			// System.err.println("Child with value: " + child.getTempValue().toString());
			lastChild = child;
			child = child.getTempNext();
		} // while
		
		
//		String v = (lastChild != null) ?  lastChild.getTempValue().toString() : "no-value";
//		System.err.println("TempLastChild. Last child has value: " + v);
		
		return lastChild;
	} // getTempLastChild

	public TreeNodeElement getLastChild() {
		TreeNodeElement child = getFirst();
		TreeNodeElement lastChild = null;
		while (child != null) {
			lastChild = child;
			child = child.getNext();
		} // while
		return lastChild;
	} // getTempLastChild

	
	
	public NumberElement getIndex(TreeNodeElement aChild) {
		NumberElement result = null;

		int idx = 1;
		TreeNodeElement child = getFirst();
		while(child != null && child != aChild) {
			idx++;
			child = child.getNext();
		} // while
		
		result = NumberElement.getInstance(idx);

		return result;
	} // getIndex

	
	protected TreeNodeElement getTempPreviousSibling() {
		
		TreeNodeElement parent = getTempParent();
		TreeNodeElement previousSibling = null;
		
		if(parent != null) {
			previousSibling = parent.getTempChildBefore(this);
		} // if
		return previousSibling;
	} // getTempPreviousSibling
	
	
	public TreeNodeElement getPreviousSibling() {
		
		TreeNodeElement parent = getParent();
		TreeNodeElement previousSibling = null;
		
		if(parent != null) {
			previousSibling = parent.getChildBefore(this);
		} // if
		return previousSibling;
	} // getPreviousSibling



	protected TreeNodeElement getTempChildBefore(TreeNodeElement aChild) { 
		TreeNodeElement child = getTempFirst();
		TreeNodeElement childBefore = null;
		
		while(child != null && aChild != child) {
			childBefore = child;
			child = child.getTempNext();
		} // while
		
		return childBefore;
	} // getTempChildBefore

	
	public TreeNodeElement getChildBefore(TreeNodeElement aChild) { 
		TreeNodeElement child = getFirst();
		TreeNodeElement childBefore = null;
		
		while(child != null && aChild != child) {
			childBefore = child;
			child = child.getNext();
		} // while
		
		return childBefore;
	} // getChildBefore

	// **********************************************************************************************
	// **********************************************************************************************
	// **********************************************************************************************
	

	
	
		

	public void removeFromParent() {
		detachFromParent();
		setParent(null);
		setNext(null);
	} // removeFromParent

	public void removeAllChildren() {
		TreeNodeElement child = getFirst();
		while(child != null) {
			TreeNodeElement tmp = child.getNext();
			child.detachFromParent();
			child.setParent(null);
			child.setNext(null);
			child = tmp;
		} // while
		setFirst(null);
	} // removeAllChildren

	//
	// PATHS
	//

	public NumberElement getLevel() {
		int l = 0;
		TreeNodeElement parent = this;
		do {
			l++;
			parent = parent.getParent();
		} while (parent != null);

		return NumberElement.getInstance(l);
	} // getLevel


	public boolean isNodeAncestor(TreeNodeElement anotherNode) {
		if(anotherNode == null)
			throw new IllegalArgumentException("Null parameter");

		boolean isAncestor = false;
		TreeNodeElement parent = this;
		do {
			parent = parent.getParent();
			isAncestor = (parent == anotherNode);
		} while (!isAncestor && parent != null);
		return isAncestor;
	} // isNodeAncestor

	public boolean isNodeDescendant(TreeNodeElement anotherNode) {
		if(anotherNode == null)
			throw new IllegalArgumentException("Null parameter");

		boolean isDescendant = false;
		
			isDescendant = anotherNode.isNodeAncestor(this);
		return isDescendant;
	} // isNodeDescendant


	//
	// Interface   E N U M E R A B L E
	// The TreeNodeElement will be treated like a tree.
	// The methods contains, enumerate, and size WILL NOT refer to this node's children but to the tree rooted in this node)
	// 

	@Override
	public boolean contains(Element e) {
		return enumerate().contains(e);
	}

	@Override
	public Collection<? extends Element> enumerate() {
		// parameter set to false: nodes will be added to the List list, not values.
		List<Element> list = traverseTree(false);
		return list;
	}

	@Override
	public List<Element> getIndexedView() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("TreeNodeElement: Indexed view is not supported");
	}

	@Override
	public int size() {
		return enumerate().size();
	}

	@Override
	public boolean supportsIndexedView() {
		return false;
	}


	/*
	 * Returns a List containing the values stored in the tree rooted in this node. The tree is traverse in preorder (depth-first) mode.
	 */
	protected List<Element> traverseTree(boolean onlyValues) {
		List<Element> l = new LinkedList<Element>();

		String travMode = getTraversalMode();

		if( travMode.equals(TREE_TRAVERSAL_OPT_BF) ) {
			l = breadthFirstTraversal(this, onlyValues);
		} else if ( travMode.equals(TREE_TRAVERSAL_OPT_DF) ) {
			l = depthFirstTraversal(this, onlyValues);
		} // if

		return l;		
	} // traverseTree


	public Collection<? extends Element> BFT() {
		// 2nd parameter set to true: add to the Collection only the values contained in the nodes, not the nodes themselves
		return TreeNodeElement.breadthFirstTraversal(this, true);
	} // BFT

	public Collection<? extends Element> DFT() {
		// 2nd parameter set to true: add to the Collection only the values contained in the nodes, not the nodes themselves
		return TreeNodeElement.depthFirstTraversal(this, true);
	} // DFT


	public Collection<? extends Element> DFTNodes() {
		// 2nd parameter set to false: add to the Collection the nodes in the tree, not only the contained values
		return TreeNodeElement.depthFirstTraversal(this, false);
	} // DFTNodes


	public Collection<? extends Element> BFTNodes() {
		// 2nd parameter set to false: add to the Collection the nodes in the tree, not only the contained values
		return TreeNodeElement.breadthFirstTraversal(this, false);
	} // DFTNodes


	protected static List<Element> breadthFirstTraversal(TreeNodeElement node, final boolean onlyValues) {

		Queue<TreeNodeElement> nodes = new LinkedList<TreeNodeElement>();
		List<Element> values = new LinkedList<Element>();

		nodes.offer(node);

		while(! nodes.isEmpty() ) {
			TreeNodeElement head = nodes.poll();			
			if(onlyValues)
				values.add(head.getValue());
			else
				values.add(head);
			
			TreeNodeElement child = head.getFirst();
			while (child != null) {
				nodes.offer(child);
				child = child.getNext();
			} // for
		} // while

		return values;
	} // breadthFirstTraversal


	protected static List<Element> depthFirstTraversal(TreeNodeElement node, final boolean onlyValues) {
		List<Element> l = new LinkedList<Element>();

		if(node != null) {

			if(onlyValues)
				l.add(node.getValue());
			else
				l.add(node);

			TreeNodeElement child = node.getFirst();
			
			while(child != null) {
				List<Element> listFromChild = depthFirstTraversal(child, onlyValues);
				l.addAll(listFromChild);
				child = child.getNext();
			} // for 
		} // if

		return l;
	} // depthFirstTraversal


	static protected String valueToString(Element elem) {
		String res = UNDEF_STRING;
		if(elem != Element.UNDEF) {
			res = elem.toString();
		}
		return res;
	} // valueToString

	@Override
	public String toString() {
		String str = "";

		String strFormat = getOutpuStringFormat();

		if(strFormat.equals(TREE_OUTPUT_STRING_OPT_LONG))
			str = getLongString();
		else if (strFormat.equals(TREE_OUTPUT_STRING_OPT_SHORT))
			str = getShortString();

		return str;
	} // toString


	public String getShortString() {

		// If the node has no children, return the value without parentheses
		if (getTempFirst() == null)
			return valueToString(getValue());

		// The node has some children, we surround the resulting string with parentheses
		String res = L_BRACKET;

		res += getShortStringRec();

		res += R_BRACKET;

		return res;
	} // getShortString

	protected String getShortStringRec() {
		// System.err.println("Using modified short string");
		String res = "";

		// The following conditional is useless:
		// In the current implementation getShortStringRec is called only if the node has children.		
		if(getTempFirst() == null) {
			return valueToString(getValue());
		}

		// there is at least one child

		// Flag used to manage the following case:
		//    this node's value is undef and all of its children are leaves.
		// In that case, closeParenthesis remains false and the method return a,b,c,d,... (values in the leaves)
		// If the node's value is not undef or if it is undef and at least one child is not a tree, the method returns value, (children list)
		// We cannot open the parenthesis here because we need to scan the children list to check the presence of a subtree
		boolean closeParenthesis = false;

		if (getValue() != Element.UNDEF) {
			res = res + valueToString(getValue());
			res = (res +", " + L_BRACKET ); // open list of children
			closeParenthesis = true;
		} // if value != UNDEF

		boolean subTree = false;

		String childrenStr = "";
		TreeNodeElement child = getFirst();
		while (child != null) {
			String tmpStr = child.getShortStringRec(); 

			// If child has children, it returns the string:  value, (childrenlist), 
			// 		we surround the result with parentheses obtaining (value, (childrenlist))
			if(child.getChildCount().intValue() > 0) {  
				subTree = true;
				childrenStr = childrenStr + L_BRACKET + tmpStr + R_BRACKET; 
			} else childrenStr = childrenStr + tmpStr;

			childrenStr += ", ";
			child = child.getNext();
		} // for
		// Remove the final ", " from str
		if(childrenStr.length() > 0)
			childrenStr = childrenStr.substring(0, childrenStr.length()-2);


		boolean ambiguousCase = 
			( (getChildCount().intValue() == 2) && (getValue() == Element.UNDEF) && subTree &&  
					(getChildAt(1).isLeaf()));


		// If the node has two children:
		//   - 1st one is a leaf
		//   - 2nd one a subtree
		// then we are in the ambiguous case, e.g.:
		//     (1, (2, 3) -- this string might be interpreted as a tree with root value = 1 and two leaves
		// We need to use the syntax (UNDEF_STRING, (children list))
		// 	 the previous tree will be represented as (- (1, (2, 3)))

		// XXX Following guard is not correct, remove it
		// if(children.size() == 2 && getValue() == Element.UNDEF && subTree) {
		if(ambiguousCase) {
			res = res + UNDEF_STRING + ", "+ L_BRACKET; // (";
			closeParenthesis = true;
		}

		// At this point res is:
		//    1*   a,b,c,d 				
		//				list of nodes' values if the current node's value is undef and
		// (current node has one or more than two children OR current node has two children with the first
		//     one not being a leaf)		
		//    2*   value, (childrenList	   --- please note: no closing bracket
		//				value of the node and childrenlist without closing parenthesis
		//    3*   UNDEF_STRING, (childrenList   --- please note: no closing bracket
		//				string representing the undef element and children list - ambiguous case

		res = res + childrenStr;

		// In cases 2 and 3, the flag closeParenthesis is true. In case 1 it is false.
		if(closeParenthesis)
			res += R_BRACKET; // close list of children

		// Out of curiosity:
		// System.out.println("> Node " + value + " returning string : " + res);
		return res;
	} // getShortStringRec

	public String getLongString() {
		return getLongStringRec();
	} // getLongString

	protected String getLongStringRec() {
		String res = "";

		res = L_BRACKET + valueToString(getValue()) + ", " + L_BRACKET; 

		TreeNodeElement child = getFirst();
		if(child != null) {
			
			while (child != null) {
				res += child.getLongStringRec();
				res += ", ";
				child = child.getNext();
			} // for

			// Remove the extra ", " added in the last cycle of the previous for loop
			res = res.substring(0, res.length()-2);
		}
		res += (R_BRACKET + R_BRACKET); // "))";

		return res;
	} // getLongStringRec


	public TreeNodeElement getChildAt(NumberElement pos) {
		return getChildAt(pos.intValue());
	} // getChildAt
	
	
	public TreeNodeElement getChildAt(int pos) {
		TreeNodeElement child = getFirst();
		int i = 1;
		while (i < pos && child != null) {
			child = child.getNext();
		} // while
		
		return child;
	} // getChildAt

	
	protected TreeNodeElement getTempChildAt(int pos) {
		TreeNodeElement child = getTempFirst();
		int i = 1;
		while (i < pos && child != null) {
			child = child.getTempNext();
		} // while
		
		return child;
	} // getTempChildAt

	
	
	
	
} // NodeElement.java
