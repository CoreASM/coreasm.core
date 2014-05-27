package org.coreasm.eclipse.editors.outlining;

import java.net.URL;

/**
 * An OutlineTreeNode represents a node in the tree which is displayed in the
 * outline view for an ASMEditor. 
 * @author Markus Müller, Tobias Seyfang
 */
public class OutlineTreeNode implements Comparable<OutlineTreeNode>
{
	public final String description;// The description provided by the label provider
	public String suffix;			// The suffix which is shown after the description
	public int index;				// The offset within the CoreASM specification this node refers to
	public int length;				// The length of the piece of text this node refers to
	public String icon;				// The location of the icon
	public String group;			// The name of the group in which this node belongs to
	public URL iconURL;				// The location of the icon (for plugins)
	
	public OutlineTreeNode() {
		description = "";
		index = 0;
		length = 1;
	}
	
	public OutlineTreeNode(String description) {
		index = 0;
		length = 1;
		this.description = description;
	}
	
	public OutlineTreeNode(String node, String description) {
		index = getPositionFromSyntaxNode(node);
		length = 1;
		this.description = description;
	}
	
	public OutlineTreeNode(String node, String description, String suffix) {
		index = getPositionFromSyntaxNode(node);
		length = 1;
		this.description = description;
		this.suffix = suffix;
	}
	
	@Override
	public String toString()
	{
		return "[" + this.getClass().toString() + "]  " + description;
	}

	/**
	 * Compares this outline view to the given one by comparing their
	 * descriptions alphabetically (ignoring case)
	 */
	@Override
	public int compareTo(OutlineTreeNode node) {
		return this.description.compareToIgnoreCase(node.description);
	}
	
	/**
	 * Reads the position of a node from the syntax tree by parsing it out of
	 * the result of toString() of that node. The position is at the end of that
	 * String, preceded by "@"
	 */
	private int getPositionFromSyntaxNode(String strNode)
	{
		int position = 0;
		
		try {
			position = Integer.parseInt(
				strNode.substring(strNode.lastIndexOf('@')+1, strNode.length()-1));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return position;
	}

	/**
	 * Following classes are subclasses of OutlineTreeNode so its easier to
	 * use them and reduces copy code, especially in plugins
	 */
	public static class UseTreeNode extends OutlineTreeNode {
		public UseTreeNode(String node, String description) {
			super(node, description);
			group = "Used Plugins";
			icon = "/icons/editor/package.gif";
		}
	}
	
	public static class InitTreeNode extends OutlineTreeNode {
		public InitTreeNode(String node, String description) {
			super(node, description);
			group = "Initialization";
			icon = "/icons/editor/init.gif";
		}
	}
	
	public static class SignatureTreeNode extends OutlineTreeNode {
		public SignatureTreeNode(String node, String description) {
			super(node, description);
			group = "Signatures";
			icon = "/icons/editor/sign.gif";
		}
	}
	
	public static class OptionTreeNode extends OutlineTreeNode {
		public OptionTreeNode(String node, String description, String suffix) {
			super(node, description, suffix);
			group = "Options";
			icon = "/icons/editor/option.gif";
		}
	}
	
	public static class RuleTreeNode extends OutlineTreeNode {
		public RuleTreeNode(String node, String description) {
			super(node, description);
			group = "Rule Definitions";
			icon = "/icons/editor/rule.gif";
		}
	}
	
	public static class IncludeTreeNode extends OutlineTreeNode {
		public IncludeTreeNode(String node, String description, String suffix) {
			super(node, description, suffix);
			group = "Included Files";
			icon = "/icons/editor/module.gif";
		}
	}
	
	public static class UnavailableTreeNode extends OutlineTreeNode {
		public UnavailableTreeNode() {
			super("content outline currently not available");
			icon = "/icons/editor/error.gif";
		}
	}
	
	public static class OutdatedTreeNode extends OutlineTreeNode {
		public OutdatedTreeNode() {
			super("outline view is outdated because of errors");
			icon = "/icons/editor/warning.gif";
		}
	}
}
