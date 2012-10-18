/*	
 * ChildNodesList.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author$ on $Date$.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.Node.NameNodeTuple;

/**
 * Maintains the list of child nodes and protects the list from invalid
 * modification.
 * 
 * @author Roozbeh Farahbod, Marcel Dausend
 * 
 */
public class ChildNodesList {
	private LinkedListTuple firstChild;
	private LinkedListTuple lastChild;
	private final HashMap<Node, LinkedListTuple> nodeWrappers;
	private final Node parent;
	private List<Node> childList = null;
	private List<NameNodeTuple> childTupleList = null;

	public ChildNodesList(Node parent) {
		this.firstChild = null;
		this.lastChild = null;
		this.nodeWrappers = new HashMap<Node, LinkedListTuple>();
		this.parent = parent;
	}

	public NameNodeTuple getFirst() {
		if (firstChild != null)
			return firstChild.nodeTuple;
		else
			return null;
	}

	public NameNodeTuple getNameNodeTuple(Node node) {
		return nodeWrappers.get(node).nodeTuple;
	}

	/**
	 * Adds a child node with the default name after the given node. If node is
	 * null, it adds it to the end of the list.
	 */
	public void add(Node node, Node after) {
		add(Node.DEFAULT_NAME, node, after);
	}

	/**
	 * Removes the given node from the hash-map.
	 * 
	 * @author Marcel Dausend
	 */
	public void remove(Node node) {

		if (nodeWrappers.containsKey(node)) {
			// delete first element
			if (firstChild.nodeTuple.node == node) {
				if (nodeWrappers.size() == 1) {
					firstChild = null;
					lastChild = null;
				} else if (nodeWrappers.size() > 1) {
					firstChild = firstChild.next;
				}
			}// delete last element
			else if (lastChild.nodeTuple.node == node) {
				LinkedListTuple preLast = firstChild;
				while (preLast.next.nodeTuple.node != node) {
					preLast = preLast.next;
				}
				preLast.next = null;
				lastChild = preLast;
			}// delete element from the middle
			else {
				LinkedListTuple predecessorOfRemove = firstChild;
				while (predecessorOfRemove.next.nodeTuple.node != node) {
					predecessorOfRemove = predecessorOfRemove.next;
				}
				predecessorOfRemove.next = nodeWrappers.get(node).next;
			}
			// remove node from hash-map
			nodeWrappers.remove(node);
			// clear childlist and childtuplelist
			invalidateChildList();
		} else
			throw new CoreASMError("Node to be removed is missing.");
	}

	/**
	 * Adds a child node with the given name after the given node. If node is
	 * null, it adds it to the end of the list.
	 */
	public void add(String name, Node node, Node after) {
		final LinkedListTuple link = new LinkedListTuple(new NameNodeTuple(
				name, node));
		if (after == null) {
			if (firstChild != null) {
				lastChild.next = link;
				lastChild = link;
			} else {
				firstChild = link;
				lastChild = link;
			}
		} else {
			if (firstChild != null) {
				LinkedListTuple afterTuple = nodeWrappers.get(after);
				if (afterTuple != null) {
					link.next = afterTuple.next;
					afterTuple.next = link;
				} else
					throw new CoreASMError("Expected child node is missing.");
			} else {
				throw new CoreASMError(
						"Error adding the same node twice as a parent's child.");
			}
		}
		node.parent = parent;
		if (nodeWrappers.get(node) != null)
			throw new CoreASMError(
					"Error adding the same node twice as a parent's child.");
		nodeWrappers.put(node, link);
		invalidateChildList();
	}

	public List<Node> getChildList() {
		if (childList == null) {
			if (firstChild != null) {
				LinkedListTuple link = firstChild;
				childList = new ArrayList<Node>();
				while (link != null) {
					childList.add(link.nodeTuple.node);
					link = link.next;
				}
			} else
				childList = Collections.emptyList();
		}
		return childList;
	}

	public List<NameNodeTuple> getChildTupleList() {
		if (childTupleList == null) {
			if (firstChild != null) {
				LinkedListTuple link = firstChild;
				childTupleList = new ArrayList<NameNodeTuple>();
				while (link != null) {
					childTupleList.add(link.nodeTuple);
					link = link.next;
				}
			} else
				childTupleList = Collections.emptyList();
		}
		return childTupleList;
	}

	/**
	 * Returns the next sibling of this node. This method is added for
	 * comfortability and it is not efficient. Returns <code>null</code> if this
	 * is the last child.
	 */
	public NameNodeTuple getNext(Node node) {
		if (node.parent != null) {
			final LinkedListTuple nextLink = node.parent.children.nodeWrappers
					.get(node).next;
			if (nextLink != null)
				return nextLink.nodeTuple;
		}
		return null;
	}

	private void invalidateChildList() {
		childList = null;
		childTupleList = null;
	}

	public boolean isEmpty() {
		return firstChild == null;
	}

	private static class LinkedListTuple {
		private NameNodeTuple nodeTuple;
		private LinkedListTuple next;

		protected LinkedListTuple(NameNodeTuple nodeTuple) {
			this.nodeTuple = nodeTuple;
			next = null;
		}
	}

	public void dispose() {
		for (NameNodeTuple c : getChildTupleList())
			c.node.dipose();
		this.nodeWrappers.clear();
		invalidateChildList();
	}

}
