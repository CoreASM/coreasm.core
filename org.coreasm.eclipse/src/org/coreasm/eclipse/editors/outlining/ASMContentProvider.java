package org.coreasm.eclipse.editors.outlining;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.outlining.ASMOutlineTreeNode.NodeType;
import org.coreasm.eclipse.util.Utilities;
import org.coreasm.engine.interpreter.ASTNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;

public class ASMContentProvider implements ITreeContentProvider, Observer 
{
	private final static String AST_POSITIONS = "__ast_position";
	
	private final IPositionUpdater positionUpdater = new DefaultPositionUpdater(AST_POSITIONS);
	private ASMEditor editor;
	private ASMOutlineTreeNode root;
	private ASMOutlineTreeNode ungroupedRoot;
	private ASMOutlineTreeNode groupedRoot;
	private boolean displayGroups = true;
	private boolean displaySorted = false;
	private IFile parentFile;
	private StructuredViewer viewer;
	
	public ASMContentProvider() {
	}
	
	public ASMContentProvider(ASMEditor editor) {
		this.editor = editor;
		editor.getParser().addObserver(this);
	}
	
	public void setDisplaySorted(boolean displaySorted) {
		this.displaySorted = displaySorted;
		update();
	}
	
	public void setDisplayGroups(boolean displayGroups) {
		this.displayGroups = displayGroups;
		update();
	}
	
	public boolean isDisplaySorted() {
		return displaySorted;
	}
	
	public boolean isDisplayStructured() {
		return displayGroups;
	}
	
	public boolean isContentAvailable() {
		return root != null;
	}

	@Override
	public void dispose() {

	}
	
	private ASMEditor getEditor(Object input) {
		if (!(input instanceof IFile))
			return editor;
		if (editor != null)
			editor.getParser().deleteObserver(this);
		parentFile = (IFile)input;
		editor = (ASMEditor)Utilities.getEditor(parentFile);
		if (editor != null)
			editor.getParser().addObserver(this);
		return editor;
	}
	
	protected void update() {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			if (parentFile != null)
				viewer.refresh(parentFile);
			else {
				viewer.refresh();
				if (viewer instanceof TreeViewer) {
					TreeViewer treeViewer = (TreeViewer)viewer;
					Control control = treeViewer.getControl();
					control.setRedraw(false);
					treeViewer.expandAll();
					control.setRedraw(true);
				}
			}
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				update();
			}
		});
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer != this.viewer) {
			this.viewer = (StructuredViewer)viewer;
			this.viewer.addOpenListener(new IOpenListener() {
				
				@Override
				public void open(OpenEvent event) {
					getEditor(parentFile);
				}
			});
			this.viewer.addDoubleClickListener(new IDoubleClickListener() {
				
				@Override
				public void doubleClick(DoubleClickEvent event) {
					ISelection selection = event.getSelection();
					IStructuredSelection sel = (IStructuredSelection) selection;
					Object element = sel.getFirstElement();
					try {
						ASMEditor editor = getEditor(parentFile);
						if (editor == null)
							Utilities.openEditor(parentFile);
					
						if (element instanceof ASMOutlineTreeNode) {
							ASMOutlineTreeNode node = (ASMOutlineTreeNode) element;
							if (node.getType() == NodeType.GROUP_NODE)
								return;

							ASMDocument document = (ASMDocument)getEditor(parentFile).getInputDocument();
							getEditor(parentFile).setHighlightRange(document.getUpdatedOffset(document.getNodePosition(node.getNode())), ASMDocument.calculateLength(node.getNode()), true);
						}
					}
					catch (IllegalArgumentException exeption) {
						getEditor(parentFile).resetHighlightRange();
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
		}
		if (getEditor(newInput) != null) {
			if (oldInput != null) {
				IDocument document = getEditor(oldInput).getDocumentProvider().getDocument(oldInput);
				if (document != null) {
					try {
						document.removePositionCategory(AST_POSITIONS);
					} catch (BadPositionCategoryException e) {
						e.printStackTrace();
					}
					document.removePositionUpdater(positionUpdater);
				}
			}
			
			if (newInput != null) {
				IDocument document = getEditor(newInput).getDocumentProvider().getDocument(newInput);
				if (document != null) {
					document.addPositionCategory(AST_POSITIONS);
					document.addPositionUpdater(positionUpdater);
				}
			}
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		ASTNode node = null;
		if (getEditor(inputElement) != null)
			node = getEditor(inputElement).getParser().getRootNode();
		else {
			groupedRoot = null;
			ungroupedRoot = null;
		}
		if (node != null) {
			ungroupedRoot = new ASMOutlineTreeNode(node);
			groupedRoot = createStructuredTree(node);
		}
		
		root = (displayGroups ? groupedRoot : ungroupedRoot);
		
		if (root == null)
			return new Object[] { ASMOutlineTreeNode.UNAVAILABLE_NODE };
		
		if (node == null)
			return new Object[] { ASMOutlineTreeNode.OUTDATED_NODE, root };
		
		return new Object[] { root };
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IResource)
			return getElements(parentElement);
		if (!(parentElement instanceof ASMOutlineTreeNode))
			return null;
		
		ASMOutlineTreeNode parentNode = (ASMOutlineTreeNode) parentElement;
		
		if (!parentNode.hasChildren())
			return null;
		
		List<ASMOutlineTreeNode> list = parentNode.getChildren();
		
		if (displaySorted)
			Collections.sort(list);

		return list.toArray();
	}

	@Override
	public Object getParent(Object element) {
		if (!(element instanceof ASMOutlineTreeNode))
			return null;
		ASMOutlineTreeNode node = (ASMOutlineTreeNode)element;
		return node.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IFile) {
			IFile file = (IFile)element;
			return "casm".equals(file.getFileExtension()) || "coreasm".equals(file.getFileExtension());
		}
		if (!(element instanceof ASMOutlineTreeNode))
			return false;
		ASMOutlineTreeNode node = (ASMOutlineTreeNode)element;
		return node.hasChildren();
	}
	
	private ASMOutlineTreeNode createStructuredTree(ASTNode node) {
		return createStructuredTree(new ASMOutlineTreeNode(node));
	}
	
	private ASMOutlineTreeNode createStructuredTree(ASMOutlineTreeNode node) {
		if (!node.hasChildren())
			return node;
		HashMap<String, ASMOutlineTreeNode> groups = new HashMap<String, ASMOutlineTreeNode>();
		for (ASMOutlineTreeNode child : node.getChildren()) {
			if (child.getGroup() != null) {
				node.removeChild(child);
				ASMOutlineTreeNode group = groups.get(child.getGroup());
				if (group == null) {
					group = new ASMOutlineTreeNode(NodeType.GROUP_NODE, child.getGroup());
					groups.put(child.getGroup(), group);
					node.addChild(group);
				}
				group.addChild(createStructuredTree(child));
			}
		}
		return node;
	}
}
