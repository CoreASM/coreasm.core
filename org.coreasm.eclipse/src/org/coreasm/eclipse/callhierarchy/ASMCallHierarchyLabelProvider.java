package org.coreasm.eclipse.callhierarchy;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class ASMCallHierarchyLabelProvider implements ILabelProvider {
	@Override
	public Image getImage(Object element) {
		if (!(element instanceof ASMCallHierarchyNode))
			return null;
		ASMCallHierarchyNode node = (ASMCallHierarchyNode)element;
		return node.getImage();
	}

	@Override
	public String getText(Object element) {
		if (!(element instanceof ASMCallHierarchyNode))
			return "Unknown: " + element.toString();
		return element.toString();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

}
