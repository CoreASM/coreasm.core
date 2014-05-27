package org.coreasm.eclipse.editors.outlining;

import org.coreasm.eclipse.editors.IconManager;
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
		
		if (node.icon != null)
			return IconManager.getIcon(node.icon);
		
		if (node.iconURL != null)
			return IconManager.getIcon(node.iconURL);
		
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
