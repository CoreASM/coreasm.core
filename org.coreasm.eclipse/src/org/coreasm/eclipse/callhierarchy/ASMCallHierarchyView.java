package org.coreasm.eclipse.callhierarchy;

import org.coreasm.engine.interpreter.ASTNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ASMCallHierarchyView extends ViewPart {
	public static final String ID = "org.coreasm.eclipse.views.ASMCallHierarchy";
	private ASMCallHierarchyViewer treeViewer;

	@Override
	public void createPartControl(Composite parent) {
		treeViewer = new ASMCallHierarchyViewer(parent);
		treeViewer.setAutoExpandLevel(0);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	public void setInput(ASTNode node, IFile file) {
		treeViewer.setInputData(new ASMCallHierarchyNode(node, node, file));
	}
	
	public static ASMCallHierarchyView openView(ASTNode node, IFile file) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		ASMCallHierarchyView view = null;
		try {
			 view = (ASMCallHierarchyView)page.showView(ID);
			 view.setInput(node, file);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return view;
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
}
