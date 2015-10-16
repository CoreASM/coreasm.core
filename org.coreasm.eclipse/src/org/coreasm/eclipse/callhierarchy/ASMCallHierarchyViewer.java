package org.coreasm.eclipse.callhierarchy;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.util.Utilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PartInitException;

public class ASMCallHierarchyViewer extends TreeViewer {
	private ASMCallHierarchyNode inputData;

	public ASMCallHierarchyViewer(Composite parent) {
		super(new Tree(parent, SWT.MULTI));
		setContentProvider(new ASMCallHierarchyContentProvider());
		setLabelProvider(new ASMCallHierarchyLabelProvider());
		
		addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object element = sel.getFirstElement();
				try {
					if (element instanceof ASMCallHierarchyNode) {
						ASMCallHierarchyNode node = (ASMCallHierarchyNode) element;
						IFile file = node.getFile();
						Utilities.openEditor(file);
						
						if (node.getNode() != null) {
							ASMDocument document = (ASMDocument)getEditor(file).getInputDocument();
							getEditor(file).setHighlightRange(document.getUpdatedOffset(document.getNodePosition(node.getNode())), document.calculateLength(node.getNode()), true);
						}
						else
							setInputData(inputData);
					}
				} catch (IllegalArgumentException exeption) {
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void setInputData(ASMCallHierarchyNode node) {
		if (this.inputData != null)
			this.inputData.clear();
		inputData = node;
		setInput(new ASMCallHierarchyNode(node));
		getControl().setFocus();
	}
	
	private ASMEditor getEditor(IFile file) {
		return (ASMEditor)Utilities.getEditor(file);
	}
}
