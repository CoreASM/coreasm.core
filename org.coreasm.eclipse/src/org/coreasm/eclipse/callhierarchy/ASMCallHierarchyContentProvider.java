package org.coreasm.eclipse.callhierarchy;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ASMCallHierarchyContentProvider implements ITreeContentProvider {
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (!(parentElement instanceof ASMCallHierarchyNode))
			return new Object[0];
		
		ASMCallHierarchyNode parentNode = (ASMCallHierarchyNode) parentElement;
		
		if (!parentNode.hasChildren())
			return new Object[0];
		
		return parentNode.getChildren().toArray();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(Object element) {
		if (!(element instanceof ASMCallHierarchyNode))
			return null;
		ASMCallHierarchyNode node = (ASMCallHierarchyNode)element;
		return node.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		if (!(element instanceof ASMCallHierarchyNode))
			return false;
		ASMCallHierarchyNode node = (ASMCallHierarchyNode)element;
		return node.hasChildren();
	}

}
