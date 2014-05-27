package org.coreasm.eclipse.editors.outlining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.coreasm.eclipse.editors.outlining.ParsedContentProvider.DisplayModeOrder;

/**
 * @author Tobias
 *
 * Class which represents a root node in the outline
 */
public class RootOutlineTreeNode extends OutlineTreeNode {
	
	// group nodes
	private LinkedHashMap<String, GroupOutlineTreeNode> groupNodes = 
			new LinkedHashMap<String, GroupOutlineTreeNode>();
	
	// all nodes
	private ArrayList<OutlineTreeNode> allNodesUnsorted = new ArrayList<OutlineTreeNode>();
	private ArrayList<OutlineTreeNode> allNodesSorted = new ArrayList<OutlineTreeNode>();

	public RootOutlineTreeNode(String node, String description, String suffix) {
		super(node, description, suffix);
		icon = "/icons/icon16.gif";
	}

	public Collection<GroupOutlineTreeNode> getGroupNodes() {
		return groupNodes.values();
	}
	
	/**
	 * @param outlineOrder	The order of the outline
	 * @return				Nodes depening on order
	 */
	public ArrayList<OutlineTreeNode> getAllNodes(DisplayModeOrder outlineOrder) {
		if (outlineOrder == DisplayModeOrder.ALPHABETICAL) 
			return allNodesSorted;
		else
			return allNodesUnsorted;
	}

	/**
	 * @param oNode		Node which is added
	 * 
	 * Adds note to allNodes and GroupNodes
	 */
	public void addNode(OutlineTreeNode oNode) {
		// add to all nodes
		allNodesSorted.add(oNode);
		allNodesUnsorted.add(oNode);
		Collections.sort(allNodesSorted);
		
		// add to group nodes
		if (!groupNodes.containsKey(oNode.group) )
			addGroup(oNode, oNode.group);
		
		groupNodes.get(oNode.group).addChild(oNode);
	}
	
	/**
	 * @param oNode		Node which is added
	 * @param group		Name of the group the node should belong to
	 * 
	 * Adds a group to groupNodes, checks if oNode is a UseTreeNode 
	 * because its a special group
	 */
	private void addGroup(OutlineTreeNode oNode, String group) {
		GroupOutlineTreeNode groupNode = null;
		if (oNode instanceof UseTreeNode) {
			groupNode = new GroupOutlineTreeNode.UseGroupTreeNode(group);
		} else {
			groupNode = new GroupOutlineTreeNode(group);
		}
		
		groupNodes.put(group, groupNode);
	}
}
