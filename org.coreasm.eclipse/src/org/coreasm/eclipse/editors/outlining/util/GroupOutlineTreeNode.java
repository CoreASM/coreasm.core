package org.coreasm.eclipse.editors.outlining.util;

import java.util.ArrayList;
import java.util.Collections;

import org.coreasm.eclipse.editors.outlining.ParsedContentProvider.DisplayModeOrder;

/**
 * @author Tobias Seyfang
 * 
 *         Class which represents a group node in the outline
 */
public class GroupOutlineTreeNode extends OutlineTreeNode {

	// children
	private ArrayList<OutlineTreeNode> childrenUnsorted = new ArrayList<OutlineTreeNode>();
	private ArrayList<OutlineTreeNode> childrenSorted = new ArrayList<OutlineTreeNode>();

	public GroupOutlineTreeNode(String description) {
		super(description);
		icon = "/icons/editor/folder.gif";
	}

	public ArrayList<OutlineTreeNode> getChildren(DisplayModeOrder outlineOrder) {
		if (outlineOrder == DisplayModeOrder.ALPHABETICAL)
			return childrenSorted;
		else
			return childrenUnsorted;
	}

	public void addChild(OutlineTreeNode oNode) {
		childrenUnsorted.add(oNode);
		childrenSorted.add(oNode);
		Collections.sort(childrenSorted);
	}

	/**
	 * Use group is a different case because of the special icon
	 */
	public static class UseGroupTreeNode extends GroupOutlineTreeNode {
		public UseGroupTreeNode(String description) {
			super(description);
			icon = "/icons/editor/packagefolder.gif";
		}
	}
}
