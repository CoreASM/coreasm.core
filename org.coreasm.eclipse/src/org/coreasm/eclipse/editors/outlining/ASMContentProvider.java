package org.coreasm.eclipse.editors.outlining;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.ASMParser;
import org.coreasm.eclipse.editors.outlining.ASMOutlineTreeNode.NodeType;
import org.coreasm.eclipse.util.Utilities;
import org.coreasm.engine.interpreter.ASTNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

public class ASMContentProvider implements ITreeContentProvider
{
	private final static String AST_POSITIONS = "__ast_position";
	
	private final IPositionUpdater positionUpdater = new DefaultPositionUpdater(AST_POSITIONS);
	private HashMap<ASMEditor, Observer> observers = new HashMap<ASMEditor, Observer>();
	private ASMOutlineTreeNode root;
	private ASMOutlineTreeNode ungroupedRoot;
	private ASMOutlineTreeNode groupedRoot;
	private boolean displayGroups = true;
	private boolean displaySorted = false;
	private StructuredViewer viewer;
	
	static {
		Utilities.addOutlineContentProvider(new StandardOutlineContentProvider());
	}
	
	public void setDisplaySorted(boolean displaySorted) {
		this.displaySorted = displaySorted;
	}
	
	public void setDisplayGroups(boolean displayGroups) {
		this.displayGroups = displayGroups;
	}
	
	public boolean isDisplaySorted() {
		return displaySorted;
	}
	
	public boolean isDisplayStructured() {
		return displayGroups;
	}
	
	@Override
	public void dispose() {
		for (Entry<ASMEditor, Observer> entry : observers.entrySet()) {
			ASMParser parser = entry.getKey().getParser();
			if (parser != null)
				parser.deleteObserver(entry.getValue());
		}
	}
	
	private ASMEditor getEditor(Object input) {
		ASMEditor editor = (ASMEditor)Utilities.getEditor(input);
		registerEditor(input);
		return editor;
	}
	
	private void registerEditor(final Object input) {
		IEditorPart editor = Utilities.getEditor(input);
		if (editor instanceof ASMEditor) {
			ASMEditor asmEditor = (ASMEditor)editor;
			Observer observer = observers.get(editor);
			if (observer == null) {
				observer = new Observer() {
					
					@Override
					public void update(Observable o, Object arg) {
						new UIJob("Updating Outline") {
							
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								if (viewer != null && !viewer.getControl().isDisposed())
									viewer.refresh(input);
								return Status.OK_STATUS;
							}
						}.schedule();
					}
				};
				asmEditor.getParser().addObserver(observer);
				observers.put(asmEditor, observer);
			}
		}
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer != this.viewer) {
			this.viewer = (StructuredViewer)viewer;
			this.viewer.addOpenListener(new IOpenListener() {
				
				@Override
				public void open(OpenEvent event) {
					ISelection selection = event.getSelection();
					IStructuredSelection sel = (IStructuredSelection) selection;
					registerEditor(sel.getFirstElement());
				}
			});
			this.viewer.addDoubleClickListener(new IDoubleClickListener() {
				
				@Override
				public void doubleClick(DoubleClickEvent event) {
					ISelection selection = event.getSelection();
					IStructuredSelection sel = (IStructuredSelection) selection;
					Object element = sel.getFirstElement();
					registerEditor(element);
					try {
						if (element instanceof ASMOutlineTreeNode) {
							ASMOutlineTreeNode node = (ASMOutlineTreeNode) element;
							if (node.getNode() == null)
								return;
							IFile parentFile = node.getParentFile();
							Utilities.openEditor(parentFile);
							ASMDocument document = (ASMDocument)getEditor(parentFile).getInputDocument();
							getEditor(parentFile).setHighlightRange(document.getUpdatedOffset(document.getNodePosition(node.getNode())), document.calculateLength(node.getNode()), true);
						}
					} catch (IllegalArgumentException exeption) {
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
		}
		if (oldInput != null) {
			ASMEditor editor = getEditor(oldInput);
			if (editor != null) {
				editor.getParser().deleteObserver(observers.get(editor));
				IDocument document = editor.getDocumentProvider().getDocument(oldInput);
				if (document != null) {
					try {
						document.removePositionCategory(AST_POSITIONS);
					} catch (BadPositionCategoryException e) {
						e.printStackTrace();
					}
					document.removePositionUpdater(positionUpdater);
				}
			}
		}
		
		if (newInput != null) {
			ASMEditor editor = getEditor(newInput);
			if (editor != null) {
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
		if (getEditor(inputElement) == null)
			return new Object[0];
		ASTNode node = getEditor(inputElement).getParser().getRootNode();
		if (node != null) {
			ungroupedRoot = new ASMOutlineTreeNode(node);
			groupedRoot = createStructuredTree(node);
			if (inputElement instanceof IFile) {
				IFile parent = (IFile)inputElement;
				ungroupedRoot.setParentFile(parent);
				groupedRoot.setParentFile(parent);
			}
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
			return new Object[0];
		
		ASMOutlineTreeNode parentNode = (ASMOutlineTreeNode) parentElement;
		
		if (!parentNode.hasChildren())
			return new Object[0];
		
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
