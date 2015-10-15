package org.coreasm.eclipse.callhierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.coreasm.eclipse.editors.ASMDeclarationWatcher;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.RuleCall;
import org.coreasm.eclipse.util.IconManager;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.FrameworkUtil;

public class ASMCallHierarchyNode {
	private final Node ruleNode;
	private final Node node;
	private final Image image;
	private final IFile file;
	
	private ASMCallHierarchyNode parent;
	private List<ASMCallHierarchyNode> children;
	
	public ASMCallHierarchyNode(Node ruleNode, Node node, IFile file) {
		this.ruleNode = ruleNode;
		this.node = node;
		this.image = getImage(ruleNode);
		this.file = file;
	}
	
	public ASMCallHierarchyNode(ASMCallHierarchyNode node) {
		this(null, null, null);
		children = new ArrayList<ASMCallHierarchyNode>();
		children.add(node);
	}
	
	public ASMCallHierarchyNode getParent() {
		return parent;
	}
	
	public List<ASMCallHierarchyNode> getChildren() {
		if (children == null)
			children = getChildren(ruleNode, file);
		return Collections.unmodifiableList(children);
	}
	
	public Node getNode() {
		return node;
	}
	
	public Node getRuleNode() {
		return ruleNode;
	}
	
	public IFile getFile() {
		return file;
	}
	
	public Image getImage() {
		return image;
	}

	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}
	
	private static Image getImage(Node node) {
		return IconManager.getIcon(FileLocator.find(FrameworkUtil.getBundle(ASMCallHierarchyNode.class), new Path("/icons/editor/rule.gif"), null));
	}
	
	private List<ASMCallHierarchyNode> getChildren(Node node, IFile file) {
		if (node instanceof ASTNode)
			return getChildren((ASTNode)node, file);
		return new ArrayList<ASMCallHierarchyNode>();
	}
	
	private List<ASMCallHierarchyNode> getChildren(ASTNode node, IFile file) {
		ArrayList<ASMCallHierarchyNode> children = new ArrayList<ASMCallHierarchyNode>();
		for (RuleCall caller : ASMDeclarationWatcher.getRuleCallers(node, file)) {
			ASMCallHierarchyNode child = new ASMCallHierarchyNode(caller.getRuleNode(), caller.getCallerNode(), caller.getFile());
			child.parent = this;
			children.add(child);
		}
		return children;
	}
	
	@Override
	public String toString() {
		if (ruleNode == null)
			return "No AST available for " + file.getName();
		if (parent == null)
			return "Callers of " + ruleNode.getToken();
		if (ruleNode instanceof ASTNode) {
			ASTNode astNode = (ASTNode)ruleNode;
			while (astNode.getFirst() != null)
				astNode = astNode.getFirst();
			return astNode.getToken();
		}
		return ruleNode.getToken();
	}
}
