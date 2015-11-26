package org.coreasm.eclipse.callhierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.coreasm.eclipse.editors.ASMDeclarationWatcher;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.Call;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.Declaration;
import org.coreasm.eclipse.util.IconManager;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
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
	
	public void clear() {
		children = null;
	}
	
	private static Image getImage(Node node) {
		if (node == null)
			return IconManager.getIcon(FileLocator.find(FrameworkUtil.getBundle(ASMCallHierarchyNode.class), new Path("/icons/editor/error.gif"), null));
		if (node instanceof ASTNode) {
			ASTNode astNode = (ASTNode)node;
			if (Kernel.GR_RULEDECLARATION.equals(astNode.getGrammarRule()))
				return IconManager.getIcon(FileLocator.find(FrameworkUtil.getBundle(ASMCallHierarchyNode.class), new Path("/icons/editor/rule.gif"), null));
		}
		return IconManager.getIcon(FileLocator.find(FrameworkUtil.getBundle(ASMCallHierarchyNode.class), new Path("/icons/editor/sign.gif"), null));
	}
	
	private List<ASMCallHierarchyNode> getChildren(Node node, IFile file) {
		if (node instanceof ASTNode)
			return getChildren((ASTNode)node, file);
		return new ArrayList<ASMCallHierarchyNode>();
	}
	
	private List<ASMCallHierarchyNode> getChildren(ASTNode node, IFile file) {
		ArrayList<ASMCallHierarchyNode> children = new ArrayList<ASMCallHierarchyNode>();
		for (Call caller : ASMDeclarationWatcher.getCallers(node, file)) {
			ASMCallHierarchyNode child = new ASMCallHierarchyNode(caller.getDeclarationNode(), caller.getCallerNode(), caller.getFile());
			child.parent = this;
			children.add(child);
		}
		return children;
	}
	
	@Override
	public String toString() {
		if (ruleNode == null)
			return "No AST available for " + file.getName();
		if (ruleNode instanceof ASTNode) {
			Declaration declaration = Declaration.from((ASTNode)ruleNode);
			if (declaration != null) {
				if (parent == null)
					return "Callers of " + declaration.getName();
			}
			ASTNode astNode = (ASTNode)ruleNode;
			while (astNode.getFirst() != null)
				astNode = astNode.getFirst();
			return astNode.getToken();
		}
		return "An error occurred";
	}
}
