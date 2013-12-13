package org.coreasm.eclipse.editors.outlining;

import org.coreasm.eclipse.editors.IconManager;
import org.coreasm.eclipse.editors.outlining.OutlineTreeNode.NodeType;
import org.coreasm.eclipse.editors.outlining.ParsedContentProvider.ListNames;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for ParsedOutlinePage
 */
public class ParsedLabelProvider
extends StyledCellLabelProvider
{
	private static Image imageCoreASM = createImage("/icons/icon16.gif");
	private static Image imageFolder = createImage("/icons/editor/folder.gif");
	private static Image imageUse = createImage("/icons/editor/package.gif");
	private static Image imageInit = createImage("/icons/editor/init.gif");
	private static Image imageRule = createImage("/icons/editor/rule.gif");
	private static Image imageSign = createImage("/icons/editor/sign.gif");
	private static Image imageInclude = createImage("/icons/editor/module.gif");
	private static Image imageOption = createImage("/icons/editor/option.gif");
	private static Image imageGroupUse = createImage("/icons/editor/packagefolder.gif");
	private static Image imageError = createImage("/icons/editor/error.gif");
	private static Image imageWarning = createImage("/icons/editor/warning.gif");
	
	private static Image createImage(String filename)
	{
		return IconManager.getIcon(filename);
	}
	
	@Override
	public void update(ViewerCell cell)
	{
		Object element = cell.getElement();
		StyledString text = new StyledString();
		
		cell.setImage(getImage(element));
		text.append(getText(element));
		String suffix = getSuffix(element);
		if (suffix != null)
			text.append(" : " + suffix, StyledString.DECORATIONS_STYLER);
		
		cell.setText(text.toString());
		cell.setStyleRanges(text.getStyleRanges());
		
		super.update(cell);
	}
	
	/**
	 * Returns the icon which is used for the given node, depending on the type
	 * of the node.
	 * @param element The node, must be an instance of OutlineTreeNode
	 */
	private Image getImage(Object element)
	{
		if ( ! (element instanceof OutlineTreeNode) )
			return null;
		
		OutlineTreeNode node = (OutlineTreeNode) element;
		
		if (node.type == NodeType.ROOT_NODE) return imageCoreASM;
		if (node.type == NodeType.USE_NODE) return imageUse;
		if (node.type == NodeType.INIT_NODE) return imageInit;
		if (node.type == NodeType.RULE_NODE) return imageRule;
		if (node.type == NodeType.SIGNATURE_NODE) return imageSign;
		if (node.type == NodeType.OPTION_NODE) return imageOption;
		if (node.type == NodeType.INCLUDE_NODE) return imageInclude;

		if (node.type == NodeType.GROUP_NODE) {
			if (node.tag == ListNames.USE_NODES)
				return imageGroupUse;
			else
				return imageFolder;
		}
		
		if (node.type == NodeType.UNAVAILABLE_NODE) return imageError;
		if (node.type == NodeType.OUTDATED_NODE) return imageWarning;
		
		
		
		return null;		
	}

	/**
	 * Returns the text which is used as label for the given node.
	 * @param element The node, must be an instance of OutlineTreeNode
	 */
	private String getText(Object element)
	{
		if ( ! (element instanceof OutlineTreeNode) )
			return element.toString();
		
		OutlineTreeNode node = (OutlineTreeNode) element;
		return node.description;
	}
	
	/**
	 * Returns the suffix text which is used as label for the given node.
	 * The suffix is shown after the label with another color.
	 * @param element The node, must be an instance of OutlineTreeNode
	 */
	private String getSuffix(Object element)
	{
		if ( ! (element instanceof OutlineTreeNode) )
			return null;
		
		OutlineTreeNode node = (OutlineTreeNode) element;
		return node.suffix;
	}
	
	
}
