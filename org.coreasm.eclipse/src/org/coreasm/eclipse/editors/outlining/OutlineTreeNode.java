package org.coreasm.eclipse.editors.outlining;

/**
 * An OutlineTreeNode represents a node in the tree which is displayed in the
 * outline view for an ASMEditor. 
 * @author Markus Müller
 */
public class OutlineTreeNode
implements Comparable<OutlineTreeNode>
{
	public static enum NodeType {
		ROOT_NODE, 			// The root node, "CoreASM ID", or dummy node
		GROUP_NODE, 		// A group node for a certain type of nodes
		USE_NODE, 			// A node representing an use statement
		INIT_NODE, 			// A node representing an init statement
		RULE_NODE,			// A node representing rule declaration
		OPTION_NODE,		// A node representing an option statement
		SIGNATURE_NODE, 	// A node representing a statement of the Signature plugin
		INCLUDE_NODE,		// A node representing an include statement
		OUTDATED_NODE, 		// A node which shows that the outline view is outdated
		UNAVAILABLE_NODE	// A node which is shown if there is no tree to display
	}
	
	public final NodeType type;			// The type of the node
	public final String description;	// The description provided by the label provider
	public final Object tag;			// used by group nodes to store the NodeType of their children
	public final String suffix;			// The suffix which is shown after the description
	public final int index;				// The offset within the CoreASM specification this node refers to
	public final int length;			// The length of the piece of text this node refers to
	
	public OutlineTreeNode(NodeType type, String description, Object tag, String suffix, int index, int length)
	{
		super();
		this.type = type;
		this.description = description;
		this.tag = tag;
		this.suffix = suffix;
		this.index = index;
		this.length = length;
	}
	
	@Override
	public String toString()
	{
		return "[" + type.name() + "]  " + description;
	}

	/**
	 * Compares this outline view to the given one by comparing their
	 * descriptions alphabetically (ignoring case)
	 */
	@Override
	public int compareTo(OutlineTreeNode node) {
		return this.description.compareToIgnoreCase(node.description);
	}

}
