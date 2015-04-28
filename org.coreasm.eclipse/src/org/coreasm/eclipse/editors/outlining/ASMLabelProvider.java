package org.coreasm.eclipse.editors.outlining;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

public class ASMLabelProvider extends StyledCellLabelProvider implements ILabelProvider {
	@Override
	public void update(ViewerCell cell)
	{
		Object element = cell.getElement();
		StyledString text = new StyledString();
		
		if (element instanceof ASMOutlineTreeNode) {
			ASMOutlineTreeNode node = (ASMOutlineTreeNode)element;
			cell.setImage(node.getImage());
			if (node.getDescription() == null) {
				Node n = node.getNode();
				if (n instanceof ASTNode)
					text.append("Unknown " + ((ASTNode) n).getGrammarClass());
				else
					text.append("Unknown Node");
			}
			else
				text.append(node.getDescription());
			String suffix = node.getSuffix();
			if (suffix != null)
				text.append(" : " + suffix, StyledString.DECORATIONS_STYLER);
			
			cell.setText(text.toString());
			cell.setStyleRanges(text.getStyleRanges());
		}
		else if (element != null)
			cell.setText(element.toString());
		
		super.update(cell);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ASMOutlineTreeNode) {
			ASMOutlineTreeNode node = (ASMOutlineTreeNode)element;
			return node.getImage();
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ASMOutlineTreeNode) {
			ASMOutlineTreeNode node = (ASMOutlineTreeNode)element;
			return node.getDescription();
		}
		return element.toString();
	}
}
