/*	
 * ASTNode.java 	1.0 	$Revision: 243 $
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
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UpdateMultiset;

/** 
 * Represents nodes of the abstract syntax tree.
 * TODO say that it will be exactly the tree that we had before
 * 
 * @author  Roozbeh Farahbod
 * 
 */
public class ASTNode extends Node implements Serializable {

	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	/** grammar classes */
	public static final String ID_CLASS = "Id";
	public static final String RULE_CLASS = "Rule";
	public static final String EXPRESSION_CLASS = "Expression";
	public static final String FUNCTION_RULE_CLASS = "FunctionRule";
	public static final String DECLARATION_CLASS = "Declaration";
	public static final String UNARY_OPERATOR_CLASS = "UnaryOperator";
	public static final String BINARY_OPERATOR_CLASS = "BinaryOperator";
    public static final String TERNARY_OPERATOR_CLASS = "TernaryOperator";
  public static final String PAREN_OPERATOR_CLASS = "ParenthesisOperator";
    public static final String INDEX_OPERATOR_CLASS = "IndexOperator";
    
	/** grammar class of this node */
	protected String grammarClass;
	
	/** name of the grammar rule associated with this node */
	protected String grammarRule;

	/** updates associated with this node */
	protected UpdateMultiset updates;
	
	/** a value associated with this node */
	protected Element value;
	
	/** a location associated with this node */
	protected Location location;

	/** 
	 * Creates a new abstract node.
     * 
     * @param pluginName name of the plugin creating this node
     * @param grammarClass grammar class (will NOT be <code>null</code>)
     * @param grammarRule grammar rule (will NOT be <code>null</code>)
	 * @param token token
     * @param scannerInfo information returned by the scanner
     * @param concreteType type of this node
     */
	public ASTNode(String pluginName, String grammarClass, 
			String grammarRule, String token,
			ScannerInfo scannerInfo, String concreteType) {
		super(pluginName, token, scannerInfo, concreteType);
        this.grammarClass = grammarClass;
        this.grammarRule = grammarRule;
		this.location = null;
		this.value = null;
		this.updates = null;
		if (this.grammarClass == null)
			this.grammarClass = "";
		if (this.grammarRule == null)
			this.grammarRule = "";
	}

	/** 
	 * Creates a new abstract node with a default concrete type.
     * 
     * @param pluginName name of the plugin creating this node
     * @param grammarClass grammar class (will NOT be <code>null</code>)
     * @param grammarRule grammar rule (will NOT be <code>null</code>)
	 * @param token token
     * @param scannerInfo information returned by the scanner
     */
	public ASTNode(String pluginName, String grammarClass, 
			String grammarRule, String token,
			ScannerInfo scannerInfo) {
		this(pluginName, grammarClass, grammarRule, token, scannerInfo, Node.DEFAULT_CONCRETE_TYPE);
	}

	/**
	 * Creates a new node as a duplicate of the given node. 
	 * <p>
	 * <b>Note:</b> The new has no parent or children. 
	 *  
	 * @param node an instance of {@link ASTNode}
	 */
	public ASTNode(ASTNode node) {
		this(node.pluginName, node.grammarClass, 
				node.grammarRule, 
				node.token, node.scannerInfo, node.concreteType);
	}
	
	/**
	 * Returns the syntactical class of this node.
	 */
	public String getGrammarClass() {
		return grammarClass;
	}

	/**
	 * Returns the name of the grammar rule that produced this node.
	 */
	public String getGrammarRule() {
		return grammarRule;
	}
	
	/**
	 * @param grammarClass The grammarClass to set.
	 */
	public void setGrammarClass(String grammarClass) {
		this.grammarClass = grammarClass;
	}

	/**
	 * @param grammarRule The grammarRule to set.
	 */
	public void setGrammarRule(String grammarRule) {
		this.grammarRule = grammarRule;
	}

	/**
	 * Returns the collection of update instructions (if any) generated 
	 * from evaluting this node. This is <i>updates(node)</i>.
	 * 
	 * @return <code>Collection</code> of <code>Update</code>
	 */
	public UpdateMultiset getUpdates(){
		return updates;
	}
	
	/**
	 * Returns the value associated with this node.
	 * 
	 */
	public Element getValue() {
		return value;
	}
	
	/**
	 * Returns the location associated with this node.
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * Returns <code>true</code> if this node is evaluated.
	 */
	public boolean isEvaluated() {
		return (updates != null || value != null || location != null);
	}
	
	/**
	 * Associates a location, a set of update instructions, and 
	 * a value to this node.
	 * 
	 * @param loc location
	 * @param updates collection of updates
	 * @param value value
	 */
	public void setNode(Location loc, UpdateMultiset updates, Element value) {
		this.location = loc;
		this.updates = updates;
		this.value = value;
	}

	/**
	 * Returns the next abstract node sibling of this node. If there
	 * is no such node, returns <code>null</code>. 
	 */
	public ASTNode getNextASTNode() {
		Node next = this.getNextCSTNode();
		// look for the next abstract node
		while (next != null && !(next instanceof ASTNode))
			next = next.getNextCSTNode();
		
		if (next == null)
			return null;
		else
			return (ASTNode)next;
	}

	/**
	 * Returns the next abstract node sibling of this node. If there
	 * is no such node, returns <code>null</code>.
	 * 
	 * @see #getNextASTNode()
	 */
	public ASTNode getNext() {
		return getNextASTNode();
	}
	
	/**
	 * Returns the first abstract child node of this node. If there
	 * is no such node, returns <code>null</code>. 
	 */
	public ASTNode getFirstASTNode() {
		Node first = this.getFirstCSTNode();
		// look for the first abstract node
		while (first != null && !(first instanceof ASTNode))
			first = first.getNextCSTNode();
		
		if (first == null)
			return null;
		else
			return (ASTNode)first;
	}

	/**
	 * Returns the first abstract child node of this node. If there
	 * is no such node, returns <code>null</code>.
	 * 
	 *  @see #getFirstASTNode()
	 */
	public ASTNode getFirst() {
		return getFirstASTNode();
	}
	/**
	 * Returns the parent node of this node in form 
	 * of an <code>ASTNode</code>. This should always be possible
	 * otherwise an error is thrown.
	 */
	@Override
	public ASTNode getParent() {
		return parent;
	}

	/**
	 * Returns a list of the abstract child nodes of this node.
	 * The order of the children is preserved.
	 * 
	 * @return list of nodes
	 */
	public List<ASTNode> getAbstractChildNodes() {
		if (children.isEmpty())
			return Collections.emptyList();
		else {
			List<ASTNode> result = new ArrayList<ASTNode>();
			
			for (Node node : getChildNodes())
				if (node instanceof ASTNode)
					result.add((ASTNode)node);
		
			return result;
		}
	}
	
	/**
	 * Returns a list of abstract child nodes of this node
	 * with their names.
	 * The order of the children is preserved.
	 * 
	 * @return list of (name, node) tuples.
	 * @see NameAbstractNodeTuple
	 */
	public List<NameAbstractNodeTuple> getAbstractChildNodesWithNames() {
		if (children.isEmpty())
			return Collections.emptyList();
		else {
			List<NameAbstractNodeTuple> result = new ArrayList<NameAbstractNodeTuple>();
			
			for (NameNodeTuple t : children)
				if (t.node instanceof ASTNode)
					result.add(new NameAbstractNodeTuple(t.name, (ASTNode)t.node));
			return result;
		}
	}
	
	/**
	 * Returns abstract child nodes of this node with the given name.
	 * The order is preserved but the indices are compressed.
	 * 
	 * @param name of nodes
	 * @return list of nodes
	 */
	public List<ASTNode> getAbstractChildNodes(String name) {
		if (children.isEmpty())
			return Collections.emptyList();
		else {
			List<ASTNode> result = new ArrayList<ASTNode>();
			
			for (Node node: this.getChildNodes(name))
				if (node instanceof ASTNode)
					result.add((ASTNode)node);
		
			return result;
		}
	}
	
	/**
	 * Returns a <code>String</code> representation of this node.
	 */
	@Override
	public String toString() {
		String str = "[";
		if (token != null)
			str = str + "'" + token + "':";
		if (grammarClass != null) 
			str = str + grammarClass + ":";
		if (grammarRule != null)
			str = str + grammarRule + " ";

        if (str.length() == 1)
			str = "[GenericNode";
		return str + (scannerInfo==null?"":scannerInfo) + "]";
	}
	
	/**
	 * @see Node#unparse()
	 */
	@Override
	public String unparse() {
		if (this.token != null && 
				!(this.getGrammarClass().equals(ASTNode.BINARY_OPERATOR_CLASS)
						|| this.getGrammarClass().equals(ASTNode.UNARY_OPERATOR_CLASS)))
			return this.token;
		else
			return "";
	}

	/**
	 * A class representing a tuple of the form <code>(name, abstract node)</code>.
	 * 
	 * @author Roozbeh Farahbod
	 */
	public final class NameAbstractNodeTuple implements Cloneable {
		public String name;
		public ASTNode node;
		
		/** 
		 * Create a name-node tuple with the given name and node.
		 */
		public NameAbstractNodeTuple(String name, ASTNode node) {
			this.name = name;
			this.node = node;
		}

		@Override
		protected Object clone() {
			return new NameAbstractNodeTuple(this.name, this.node);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NameAbstractNodeTuple) {
				NameAbstractNodeTuple t = (NameAbstractNodeTuple)obj;
				// going for reference equality of nodes
				if (t.name.equals(this.name) && t.node == this.node) 
					return true;
				else 
					return false;
			}
			return super.equals(obj);
		}
		
	}
	
	protected static final class VariableMap extends AbstractMap<String, ASTNode> {
		private final ASTNode node;
		
		public VariableMap(ASTNode node) {
			this.node = node;
		}

		@Override
		public Set<Entry<String, ASTNode>> entrySet() {
			return new AbstractSet<Entry<String,ASTNode>>() {
				private final int size;
				
				{
					int count = 0;
					Iterator<Entry<String, ASTNode>> it = iterator();
					while (it.hasNext()) {
						count++;
						it.next();
					}
					size = count;
				}
				
				@Override
				public Iterator<Entry<String, ASTNode>> iterator() {
					return new VariableMapIterator(node);
				}

				@Override
				public int size() {
					return size;
				}
			};
		}
		
		private final class VariableMapIterator implements Iterator<Entry<String, ASTNode>> {
			private ASTNode current;

			VariableMapIterator(ASTNode node) {
				current = node.getFirst();
			}
			
			@Override
			public boolean hasNext() {
				return current != null && ASTNode.ID_CLASS.equals(current.getGrammarClass());
			}

			@Override
			public Entry<String, ASTNode> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				ASTNodeEntry entry = new ASTNodeEntry(current) {
					@Override
					public ASTNode getValue() {
						return super.getValue().getNext();
					}
				};
				current = (current.getNext() != null ? current.getNext().getNext() : null);
				return entry;
			}
		}
	}
	
	protected static class ASTNodeEntry implements Entry<String, ASTNode> {
		private final ASTNode astNode;
		
		ASTNodeEntry(ASTNode astNode) {
			if (astNode == null)
				throw new IllegalArgumentException("astNode of ASTNodeEntry must not be null.");
			this.astNode = astNode;
		}
		
		@Override
		public String getKey() {
			return astNode.getToken();
		}

		@Override
		public ASTNode getValue() {
			return astNode;
		}

		@Override
		public ASTNode setValue(ASTNode value) {
			throw new UnsupportedOperationException();
		}
	}
}
