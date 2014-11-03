/*	
 * Node.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.interpreter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.Specification;
import org.coreasm.engine.parser.CharacterPosition;
import org.coreasm.engine.parser.Parser;
import org.coreasm.util.Tools;

/** 
 * Root class of nodes in the parse tree. 
 *   
 * @author  Roozbeh Farahbod, Marcel Dausend
 * 
 */
public class Node implements Serializable {

	/**
	 * serial version id 
	 */
	private static final long serialVersionUID = 1L;

	private static long nextId = 1;
	
	/** various syntactic types */
	public static final String DELIMITER_NODE = "delimiter";
	public static final String WHITESPACE_NODE = "whitespace";
	public static final String COMMENT_NODE = "comment";
	public static final String KEYWORD_NODE = "keyword";
	public static final String UNIVERSE_NODE = "id-universe";
	public static final String RULE_NODE = "id-rule";
	public static final String RULE_HEADER_NODE = "id-ruleheader";
	public static final String GENERAL_ID_NODE = "id";
	public static final String OPERATOR_NODE = "operator";
	public static final String BRACKET_NODE = "bracket";
	public static final String LITERAL_NODE = "literal";
	public static final String OTHER_NODE = "other";
	public static final String DEFAULT_CONCRETE_TYPE = OTHER_NODE;

	public static final String DEFAULT_NAME = "_child_";
	
	/** a default node-to-string mapper */
	public static final DefaultFormatStringMapper DEFAULT_FORMAT_STRING_MAPPER = 
			new DefaultFormatStringMapper();
	
	/** Unique id of this node. However, this id is duplicated when the node is duplicated. */
	private long id; 
	
	/** list of child nodes */
	protected final ArrayList<NameNodeTuple> children;
	
	/** link to parent node */
	protected Node parent;
	
	/** name of the plugin associated with this node */
	protected String pluginName;
	
	/** the syntactical token that is represented by this node or <code>null</code> for non-terminals */
	protected String token;
	
    /** info returned by the scanner */
    protected ScannerInfo scannerInfo;
	
	/** concrete type of the node (e.g., keyword, operator, etc.) */
	protected String concreteType = null;

	/** extra properties */ 
	protected Map<String,Object> properties = null;
	
	/** 
     * Creates a new node.
     * 
     * @param pluginName name of the plugin creating this node
	 * @param token token
     * @param scannerInfo information returned by the scanner
     * @param concreteType type of this node
     */
	public Node(String pluginName,  
			String token, ScannerInfo scannerInfo, String concreteType) {
//		children = new ArrayList<NameNodeTuple>();
        this.pluginName = pluginName;
		this.token = token;
		this.scannerInfo = scannerInfo;
		this.concreteType = concreteType;
		this.id = nextId++;
		this.children = new ArrayList<NameNodeTuple>();
	}

    /** 
     * Creates a new node with concrete type of <code>OTHER_NODE</code>.
     * 
     * @param pluginName name of the plugin creating this node
	 * @param token token
     * @param scannerInfo information returned by the scanner
     * 
     * @see #OTHER_NODE
     */
	public Node(String pluginName,  
			String token, ScannerInfo scannerInfo) {
		this(pluginName, token, scannerInfo, DEFAULT_CONCRETE_TYPE);
	}
	
	/**
	 * Creates a new node as a duplicate of the given node. 
	 * <p>
	 * <b>Note:</b> The new has no parent or children. 
	 *  
	 * @param node 
	 */
	public Node(Node node) {
		this(node.pluginName, 
				node.token, node.scannerInfo, node.concreteType);
	}

	/**
	 * Adds a new child to this node. The child will be 
	 * added at the end of all children.
	 * 
	 * @param node node
	 * 
	 * @see #addChild(String, Node)
	 */
	public void addChild(Node node) {
		addChild(DEFAULT_NAME, node);
	}
	
	/**
	 * Adds a new child to this node. The child will be 
	 * added at the end of all children.
	 * 
	 * @param name name of this node 
	 * @param node node
	 */
	public void addChild(String name, Node node) {
		node.parent = this;
		children.add(new NameNodeTuple(name, node));
	}
	
	/**
	 * Adds a new child to this node. The child will be put
	 * after <code>indexNode</code>. If <code>indexNode</code> is
	 * <code>null</code> the node will be added as first child.
	 * 
	 * @param indexNode the child node after which the new node will be added
	 * @param name name of the new child node
	 * @param node new child node
	 * @throws IllegalArgumentException
	 */
	public void addChildAfter(Node indexNode, String name, Node node) 
			throws IllegalArgumentException {
		if (indexNode == null) {
			children.add(0, new NameNodeTuple(name, node));
			node.parent = this;
			return;
		}
		int index = 0;
		for (NameNodeTuple nameNodeTuple : children) {
			if (nameNodeTuple.node == indexNode) {
				children.add(index + 1, new NameNodeTuple(name, node));
				node.parent = this;
				return;
			}
			index++;
		}
		throw new CoreASMError("Expected child node is missing.");
	}

	/**
	 * Returns a list of the children of this node.
	 * The order of the children is preserved.
	 * 
	 * @return list of nodes
	 */
	public List<Node> getChildNodes() {
		LinkedList<Node> children = new LinkedList<Node>();
		for (NameNodeTuple nameNodeTuple : this.children) {
			children.add(nameNodeTuple.node);
		}
		return children;
	}
	
	/**
	 * Returns a list of the children of this node
	 * with their names.
	 * The order of the children is preserved.
	 * 
	 * @return list of (name, node) tuples.
	 * @see NameNodeTuple
	 */
	public List<NameNodeTuple> getChildNodesWithNames() {
		return new LinkedList<NameNodeTuple>(children);
	}
	
	/**
	 * Returns the children of this node with the given name.
	 * The order is preserved but the indices are compressed.
	 * 
	 * @param name of nodes
	 * @return list of nodes
	 */
	public List<Node> getChildNodes(String name) {
		if (children.isEmpty()) 
			return Collections.emptyList();
		else {
			List<Node> result = new ArrayList<Node>();
			

			for (NameNodeTuple tuple : children)
				if (name.equals(tuple.name))
					result.add(tuple.node);
			
			return result;
		}
	}
	
	/**
	 * Returns the first child node with the given name.
	 * 
	 * @param name name of the node
	 * @return one single node; can be <code>null</code>
	 */
	public Node getChildNode(String name) {
		for (NameNodeTuple tuple : children)
			if (name.equals(tuple.name))
				return tuple.node;
		return null;
	}
	
	/** 
	 * Returns the parent of this node.
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * Sets the parent of this node.
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	/**
	 * @return the instance of {@link NameNodeTuple} associated with the given child node.
	 */
	protected NameNodeTuple getNodeTuple(Node node) {
		for (NameNodeTuple nameNodeTuple : this.children) {
			if (nameNodeTuple.node == node)
				return nameNodeTuple;
		}
		return null;
	}
	
	/**
	 * Returns the next sibling of this node. This method 
	 * is added for comfortability and it is not efficient.
	 * Returns <code>null</code> if this is the last child.
	 */
	public Node getNextCSTNode() {
		if (this.getParent() == null)
			return null;
		Iterator<Node> it = this.getParent().getChildNodes().iterator();
		while (it.hasNext()) {
			Node current = it.next();
			if (current == this && it.hasNext())
				return it.next();
		}
		return null;
	}

	/**
	 * Returns the first child of this node. If this
	 * node has no children, returns <code>null</code>.
	 */
	public Node getFirstCSTNode() {
		if (!children.isEmpty())
			return children.get(0).node;
		return null;
	}
	
	/**
	 * Returns the name of the plugin associated with this node.
	 */
	public String getPluginName() {
		return pluginName;
	}
	
	/**
	 * @param pluginName The pluginName to set.
	 */
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

    /**
     * Returns the number of children this node has.
     * @return the number of children this node has
     */
    public int getNumberOfChildren() {
		return children.size();
    }

	/**
	 * Returns the syntactical token represented by
	 * this node.
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * @param token The token to set.
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the syntactic type of this node
	 */
	public String getConcreteNodeType() {
		return concreteType;
	}
	
	/**
	 * Sets the syntactic type of this node
	 */
	public void setConcreteNodeType(String type) {
		concreteType = type;
	}

	/**
	 * Gets the value of a property on this node.
	 * 
	 * @param property a property name in <code>String</code>
	 * @return property value
	 */
	public Object getProperty(String property) {
		if (properties != null)
			return properties.get(property);
		else
			return null;
	}
	
	/**
	 * Sets the value of a property on this node. 
	 * Any plug-in can use this method to set a custom-named
	 * property on this node. 
	 * <p>
	 * It is strongly recommended that any plug-in setting a
	 * custom property value use a prefix for the name of the
	 * property that uniquely identifies the plug-in.
	 *  
	 * @param property a property name
	 * @param value a property value
	 */
	public void setProperty(String property, Object value) {
		if (properties == null)
			properties = new HashMap<String, Object>();
		properties.put(property, value);
	}
	
	/**
	 * Returns a <b>read-only</b> copy of the property values
	 * set on this node.
	 */
	public Map<String,Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
	/**
	 * Creates a clone (duplicate) copy of this node. Parent, children and siblings of this 
	 * node is not duplicated nor are they copied (they will be <code>null</code>). 
	 */
	public final Node duplicate() {
		Node node = null;
		try {
			Class<? extends Node> c = this.getClass();
            node = (Node)c.getConstructor(c).newInstance(this);
            node.id = this.id;
        } catch (Exception e) {
            throw new EngineError("Cannot duplicate node of (" + this.getClass().getName() + ").");
        }
        
        return node;
	}
	
	/**
	 * Unparses this single node. The result does NOT include
	 * the children of this node.
	 */
	public String unparse() {
		if (this.token != null)
			return this.token;
		else
			return "";
	}

	/**
	 * Build a string representation of the concrete syntax tree
	 * that is represented by this node. It uses a default node
	 * to string mapper. 
	 *
	 * @see #unparseTree(NodeToFormatStringMapper)
	 */
	public String unparseTree() {
		return this.unparseTree(DEFAULT_FORMAT_STRING_MAPPER).trim().replaceAll("(\\s)+", "$1");
	}
	
	/**
	 * Build a string representation of the concrete syntax tree
	 * that is represented by this node. 
	 * 
	 * @param map an instance of {@link NodeToFormatStringMapper} that is used to 
	 * get the string representation of the node
	 */
	public String unparseTree(NodeToFormatStringMapper<Node> map) {
		Object[] unparsedPieces = new String[getNumberOfChildren() + 1];
		unparsedPieces[0] = this.unparse();
		
		int i = 1;
		for (Node n: getChildNodes()) {
			unparsedPieces[i] = n.unparseTree(map);
			i++;               
		}
		return String.format(map.getFormatString(this), unparsedPieces);
	}

	/**
	 * Returns a <code>String</code> representation of this node.
	 */
	@Override
	public String toString() {
		String str = "[";
		if (token != null)
			str = str + "'" + Tools.convertToEscapeSqeuence(token) + "':";
		str = str + concreteType;
		str = str + (scannerInfo==null?"":scannerInfo.getPos());
        if (str.length() == 1)
			str = "[GenericNode";
		return str + (scannerInfo==null?"":scannerInfo) + "]";
	}

	/**
	 * Makes a deep copy of the tree represented by this node.
	 * All the children are duplicated.
	 * It then sets the parent of the node to the given parent.
	 * 
	 * @return a deep copy of the tree 
	 */
	public Node cloneTree() {
		Node result = this.duplicate();
		
		for (NameNodeTuple child : children) {
			result.addChild(child.name, child.node.cloneTree());
		}
		
		return result;
	}
	
	/**
	 * Build a string representation of the AST. Attach the string representation of this node
	 * and what is below it to what we have of the AST thus far.
	 * 
	 * @param treeStr a <code>String</code> representation of the tree thus far. For the root node
	 * this should be an empty string.
	 * @param level an <code>int</code> represtening depth of this node in the entire tree. The
	 * root node should have a depth of 0.
	 * 
	 * @return The AST below the current node as a <code>String</code>
	 */
	public String buildTree (String treeStr, int level) {
		// add what the tree built thus far
		String returnStr = treeStr; // string to be returned for this portion of the AST
		 
		// add information regarding the first node
		returnStr += buildBranch(level) + toString() + "\n";
		 
		// if there is a child node, add it to the tree. Note that the
		// child is one level deeper into the tree
		if (getFirstCSTNode() != null)
			returnStr = getFirstCSTNode().buildTree(returnStr, level+1);
		 	
		// if there is a next node, add it to the tree
		if (getNextCSTNode() != null)
			returnStr = getNextCSTNode().buildTree(returnStr, level);
		
		// return the tree build thus far
		return returnStr;
	}
		
	 /**
	  * Build a branch based on the current level of the node
	  *  
	  * @param level an <code>int</code> representing depth of this node in the entire tree. The
	  * root node should have a depth of 0.
	  * @return A branch of the tree at the given level
	  */
	 protected String buildBranch(int level)
	 {
		 String spacerStr = "\t"; // spaces between levels
		 String branchStr = "--"; // the look of a vertical branch
		 String returnStr = ""; // string to be returned for this branch
		 
	     for (int i=0; i < level; i++)
	     {
	    	 returnStr += spacerStr;
	     }
	     if (level > 0)
	    	 returnStr += "("+Integer.toString(level)+")"+branchStr;
	     
	     return returnStr;
	 }

	/**
	 * A default map from nodes to string representaion of the nodes.
	 */
	public final static class DefaultFormatStringMapper implements NodeToFormatStringMapper<Node> {

		@Override
		public String getFormatString(Node node) {
			String result = "%s";
			if (KEYWORD_NODE.equals(node.getConcreteNodeType())
			|| GENERAL_ID_NODE.equals(node.getConcreteNodeType())
			|| node.getParent() instanceof ASTNode && ASTNode.BINARY_OPERATOR_CLASS.equals(((ASTNode)node.getParent()).getGrammarClass()) && node.getParent().getToken().equals(node.getToken()))
				result = " " + result + " ";
			// concatenate all the string representation of 
			// the node itself and all its children 
			for (int i = 0; i < node.getNumberOfChildren(); i++)
				result = result + "%s";
			return result;
		}
		
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (o instanceof Node) {
			Node other = (Node)o;
			boolean result = this.id == other.id;
			// Technically it's enough to check the id
			if (pluginName != null)
				result &= this.pluginName.equals(other.pluginName);
			if (concreteType != null)
				result &= this.concreteType.equals(other.concreteType);
			if (scannerInfo != null)
				result &= this.scannerInfo.equals(other.scannerInfo);
			if (token != null)
				result &= this.token.equals(other.token);
			return result;
		} else
			return false;
	}
	
	@Override
	public int hashCode() {
		return ((Long)this.id).hashCode();
	}

	
	/**
	 * @return scanner info object
	 */
	public ScannerInfo getScannerInfo() {
		return scannerInfo;
	}

	/**
	 * Returns the character position of this node with respect to the given parser.
	 * 
	 * @param parser the Parser component of the engine
	 * @return an instance of CharacterPosition
	 */
	public CharacterPosition getCharPos(Parser parser) {
		if (scannerInfo == null || parser == null)
			return CharacterPosition.NO_POSITION;
		else
			return scannerInfo.getPos(parser.getPositionMap());
	}
	

	public void replaceWith(Node replacement) {
		int index = 0;
		for (NameNodeTuple nameNodeTuple : this.getParent().children) {
			if (nameNodeTuple.node == this) {
				this.getParent().children.remove(index);
				this.getParent().children.add(index, new NameNodeTuple(nameNodeTuple.name, replacement));
				replacement.setParent(this.getParent());
				this.setParent(null);
				return;
			}
			index++;
		}
		throw new CoreASMError("Node to be replaced is missing.");
	}

	public Node removeFromTree(){
		Iterator<NameNodeTuple> it = parent.children.iterator();
		NameNodeTuple prev = null;
		while (it.hasNext()) {
			NameNodeTuple nameNodeTuple = it.next();
			if (nameNodeTuple.node == this) {
				it.remove();
				if (prev == null)
					return null;
				return prev.node;
			}
			prev = nameNodeTuple;
		}
		return null;
	}
	
	/**
	 * Returns the a string representation of the context of this node with respect to the
	 * given parser and specification.
	 * 
	 * @param parser the Parser component of the engine
	 * @param spec the specification
	 * 
	 * @return an instance of CharacterPosition
	 */
	public String getContext(Parser parser, Specification spec) {
		if (scannerInfo == null)
			return "";
		else 
			return scannerInfo.getContext(parser, spec);
	}
	
	/**
	 * Sets the scanner information of this node 
	 * to be equal to the scanner info of the given node. 
	 */
	public void setScannerInfo(Node node) {
		if (node != null)
			this.scannerInfo = node.getScannerInfo();
	}

	/**
	 * An expensive process to help with the garbage collection process.
	 */
	public void dipose() {
		parent = null;
		for (NameNodeTuple nameNodeTuple : this.children) {
			nameNodeTuple.node.dipose();
		}
	}

	/**
	 * A class representing a tuple of the form <code>(name, node)</code>.
	 * 
	 * @author Roozbeh Farahbod
	 */
	public final static class NameNodeTuple implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public final String name;
		public final Node node;
		
		/** 
		 * Create a name-node tuple with the given name and node.
		 */
		public NameNodeTuple(String name, Node node) {
			if (name == null)
				throw new IllegalArgumentException("name must not be null!");
			if (node == null)
				throw new IllegalArgumentException("node must not be null!");
			this.name = name;
			this.node = node;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NameNodeTuple) {
				NameNodeTuple t = (NameNodeTuple)obj;
				// going for reference equality of nodes
				if (t.name.equals(this.name) && t.node == this.node) 
					return true;
				else 
					return false;
			}
			return super.equals(obj);
		}
		
	}

	
}


/* 
 * Removed methods
 *

	/**
	 * Adds a new child to this node. The child will be
	 * added at the given index in the given name series. 
	 * The index of the first position in the series is zero.
	 * If there is no such name series, the index will be ignored
	 * and the child will be added to the end of the list of 
	 * all children.
	 * 
	 * @param index index in which the new child is added
	 * @param name name of this node 
	 * @param node new child node
	 *
	public void addChild(int index, String name, Node node) {
		Node c = this.getChildNode(name, index);
		if (c != null) {
			addChildBefore(c, name, node);
		} else
			addChild(name, node);
	}
	

	/**
	 * Adds a new child to this node. The child will be put
	 * before <code>indexNode</code>.
	 * 
	 * @param indexNode the child node before which the new node will be added
	 * @param name name of the new child node
	 * @param node new child node
	 * @throws IllegalArgumentException
	 *
	public void addChildBefore(Node indexNode, String name, Node node) 
			throws IllegalArgumentException {
		int i = findNode(indexNode);
		if (i > -1) 
			addChildAtAbsoluteIndex(i, name, node);
		else
			throw new IllegalArgumentException("Child node not found.");
	}


	/**
	 * Returns the i'th child node with the given name.
	 * 
	 * @param name name of the node
	 * @param index index of the node among all the nodes that share
	 * the same name
	 * @return one single node; can be <code>null</code>
	 *
	public Node getChildNode(String name, int index) {
		Node result = null;
		int i = 0; // keeps track of the index of the 
		            // node among all the nodes that share
		            // the same name 
		
		for (NameNodeTuple t: children) 
			if (t.name.equals(name)) {
				if (i == index) { 
					result = t.node;
					break;
				} else 
					i++;
			}
		return result;
	}

	/**
	 * Adds a new child at the absolute index in the list 
	 * of all children. 
	 * This method should be used only if the exact position of the
	 * child in the children list matters. 
	 * 
	 * @param index the absolute index in the list
	 * @param name name of the new child node
	 * @param node new child node
	 * 
	 * @see #addChild(int, String, Node)
	 *
	protected void addChildAtAbsoluteIndex(int index, String name, Node node) {
		children.add(index, new NameNodeTuple(name, node));
		node.parent = this;
	}
	


 */
