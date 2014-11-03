package org.coreasm.eclipse.editors.outlining;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.eclipse.util.IconManager;
import org.coreasm.eclipse.util.OutlineContentProvider;
import org.coreasm.eclipse.util.Utilities;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * @author Michael Stegmaier
 *
 */
public class ASMOutlineTreeNode implements Comparable<ASMOutlineTreeNode> {
	public static enum NodeType {
		GROUP_NODE,
		REGULAR_NODE,
		OUTDATED_NODE,
		UNAVAILABLE_NODE
	}
	
	public final static ASMOutlineTreeNode UNAVAILABLE_NODE = new ASMOutlineTreeNode(NodeType.UNAVAILABLE_NODE, "Contents unavailable until parsing finishes");
	public final static ASMOutlineTreeNode OUTDATED_NODE = new ASMOutlineTreeNode(NodeType.OUTDATED_NODE, "Contents are outdated because of errors");
	
	private final Node node;
	private final NodeType type;
	private final String description;
	private final String group;
	private final Image image;
	private final String suffix;
	
	private Object parent;
	private final List<ASMOutlineTreeNode> children;
	
	public ASMOutlineTreeNode(Node node, NodeType type, String description, String group, Image image, String suffix) {
		this.node = node;
		this.type = type;
		this.description = description;
		this.group = group;
		this.image = image;
		this.suffix = suffix;
		this.children = getChildren(node);
	}

	public ASMOutlineTreeNode(Node node) {
		this(node, NodeType.REGULAR_NODE, getDescription(node), getGroup(node), getImage(node), getSuffix(node));
	}
	
	public ASMOutlineTreeNode(NodeType type, String description) {
		this(null, type, description, null, getImage(type, description), null);
	}
	
	public void addChild(ASMOutlineTreeNode child) {
		children.add(child);
		child.parent = this;
	}
	
	public void removeChild(ASMOutlineTreeNode child) {
		if (children.remove(child))
			child.parent = null;
	}
	
	public void setParentFile(IFile parent) {
		this.parent = parent;
	}
	
	public IFile getParentFile() {
		if (parent instanceof IFile)
			return (IFile)parent;
		if (parent instanceof ASMOutlineTreeNode)
			return ((ASMOutlineTreeNode)parent).getParentFile();
		return null;
	}
	
	public Object getParent() {
		return parent;
	}
	
	public List<ASMOutlineTreeNode> getChildren() {
		return new ArrayList<ASMOutlineTreeNode>(children);
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public Node getNode() {
		return node;
	}
	
	public NodeType getType() {
		return type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getGroup() {
		return group;
	}
	
	public Image getImage() {
		return image;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	private static String getDescription(Node node) {
		if (node instanceof ASTNode)
			return getDescription((ASTNode)node);
		return node.getToken();
	}
	
	private static String getDescription(ASTNode node) {
		if (node == null)
			return null;
		if (ASTNode.ID_CLASS.equals(node.getGrammarClass()) || ASTNode.EXPRESSION_CLASS.equals(node.getGrammarClass()))
			return node.getToken();
		ASTNode child = node.getFirst();
		String description = null;
		while (child != null && description == null) {
			description = getDescription(child);
			child = child.getNext();
		}
		return description;
	}
	
	private static String getGroup(Node node) {
		if (node instanceof ASTNode)
			return getGroup((ASTNode)node);
		return null;
	}
	
	private static String getGroup(ASTNode node) {
		for (OutlineContentProvider provider : Utilities.getOutlineContentProviders()) {
			String group = provider.getGroup(node.getGrammarRule());
			if (group != null)
				return group;
		}
		return null;
	}
	
	private static Image getImage(Node node) {
		if (node instanceof ASTNode)
			return getImage((ASTNode)node);
		return null;
	}
	
	private static Image getImage(ASTNode node) {
		for (OutlineContentProvider provider : Utilities.getOutlineContentProviders()) {
			URL image = provider.getImage(node.getGrammarRule());
			if (image != null)
				return IconManager.getIcon(image);
		}
		return null;
	}
	
	private static Image getImage(NodeType type, String description) {
		if (type == NodeType.OUTDATED_NODE)
			return IconManager.getIcon("/icons/editor/warning.gif");
		if (type == NodeType.UNAVAILABLE_NODE)
			return IconManager.getIcon("/icons/editor/error.gif");
		if (type == NodeType.GROUP_NODE) {
			for (OutlineContentProvider provider : Utilities.getOutlineContentProviders()) {
				URL image = provider.getGroupImage(description);
				if (image != null)
					return IconManager.getIcon(image);
			}
			return IconManager.getIcon("/icons/editor/folder.gif");
		}
		return null;
	}
	
	private static String getSuffix(Node node) {
		if (node instanceof ASTNode)
			return getSuffix((ASTNode)node);
		return null;
	}
	
	private static String getSuffix(ASTNode node) {
		for (OutlineContentProvider provider : Utilities.getOutlineContentProviders()) {
			String suffix = provider.getSuffix(node.getGrammarRule(), getDescription(node));
			if (suffix != null)
				return suffix;
		}
		return null;
	}
	
	private static List<ASMOutlineTreeNode> getChildren(Node node) {
		if (node instanceof ASTNode)
			return getChildren((ASTNode)node);
		return new ArrayList<ASMOutlineTreeNode>();
	}
	
	private static List<ASMOutlineTreeNode> getChildren(ASTNode node) {
		ArrayList<ASMOutlineTreeNode> children = new ArrayList<ASMOutlineTreeNode>();
		for (OutlineContentProvider provider : Utilities.getOutlineContentProviders()) {
			if (provider.hasDeclarations(node.getGrammarRule())) {
				for (ASTNode child : node.getAbstractChildNodes()) {
					if (!ASTNode.ID_CLASS.equals(child.getGrammarClass()))
						children.add(new ASMOutlineTreeNode(child));
				}
				break;
			}
		}
		return children;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASMOutlineTreeNode other = (ASMOutlineTreeNode) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (suffix == null) {
			if (other.suffix != null)
				return false;
		} else if (!suffix.equals(other.suffix))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public int compareTo(ASMOutlineTreeNode node) {
		return getDescription().compareToIgnoreCase(node.getDescription());
	}
}
