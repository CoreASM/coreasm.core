package org.coreasm.eclipse.editors.outlining;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.FileManager;
import org.coreasm.eclipse.editors.IconManager;
import org.coreasm.eclipse.editors.outlining.ParsedContentProvider.DisplayModeOrder;
import org.coreasm.eclipse.editors.outlining.ParsedContentProvider.DisplayModeStructure;
import org.coreasm.eclipse.editors.outlining.util.GroupOutlineTreeNode;
import org.coreasm.eclipse.editors.outlining.util.OutlineTreeNode;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;

/**
 * This content page shows an outline view for CoreASM specifications based on
 * the syntax tree which was created by the parser.
 * @author Markus Müller
 */
public class ParsedOutlinePage 
extends AbstractContentPage {

	ParsedContentProvider contentProvider;

	// The double click listener listens for double clicks onto outline nodes.
	IDoubleClickListener dcl = null;

	public ParsedOutlinePage(ASMEditor editor)
	{
		super(
				editor,
				new ParsedContentProvider(editor.getDocumentProvider(), editor),
				new ParsedLabelProvider()
				);
		this.contentProvider = (ParsedContentProvider) super.getContentProvider();
		this.contentProvider.setOutlinePage(this);
	}

	public ParsedContentProvider getContentProvider() {
		return contentProvider;
	}
	
	/**
	 * This method sets up a DoubleClickListener which reacts on double clicking
	 * a node for an include statement and tries to open a new editor for the
	 * included files.
	 */
	public void setupListener()
	{
		TreeViewer treeviewer = this.getTreeViewer();
		if (treeviewer == null)
			return;

		if (dcl == null) {
			dcl = new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					TreeSelection s = (TreeSelection) event.getSelection();
					TreePath[] paths = s.getPaths();
					for (TreePath p: paths) {
						Object last = p.getLastSegment();
						if ( !(last instanceof OutlineTreeNode) )
							continue;
						OutlineTreeNode node = (OutlineTreeNode) last;
						if (!(node instanceof OutlineTreeNode.IncludeTreeNode))
							continue;
						String filename = node.suffix;	// suffix contains the filename relative to the project
						FileManager.openEditor(filename, ((ASMEditor) parentEditor).getInputFile().getProject());	
					}
				}
			};
			treeviewer.addDoubleClickListener(dcl);
		}
	}

	/**
	 * This method is usually called by AbstractContentPage when the user clicked
	 * onto a node of the syntax tree. It gets the element which should be
	 * higlighted and which is an instance of class OutlineNode, 
	 * as provided by its ContentProvider.  
	 */
	@Override
	protected void setHighlighting(Object element) 
	{
		if (element instanceof OutlineTreeNode)
		{
			OutlineTreeNode node = (OutlineTreeNode) element;
			if (node instanceof GroupOutlineTreeNode)
				return;

			int index = node.index;
			int length = node.length;

			ASMEditor e = (ASMEditor) parentEditor;
			ASMDocument d = (ASMDocument) e.getInputDocument();
			index = d.getUpdatedOffset(index);

			parentEditor.setHighlightRange(index, length, true);
		}
		else
			parentEditor.resetHighlightRange();
	}


	/**
	 * Adds the actions for changing the sorting of the tree nodes to the
	 * toolbar of the content page.
	 */
	@Override
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);

		IToolBarManager tbm = actionBars.getToolBarManager();
		actionViewStructure.setChecked(true);
		actionViewOrder.setChecked(false);
		tbm.add(actionViewStructure);
		tbm.add(actionViewOrder);
		this.update();
	}

	/**
	 * Enables the actions for changing the sorting of the tree nodes. 
	 */
	void enableActions(boolean enabled)
	{
		actionViewOrder.setEnabled(enabled);
		actionViewStructure.setEnabled(enabled);
	}


	// ============================================
	// 	\/ actions & icons for outline toolbar \/
	// ============================================

	private ImageDescriptor iconFolders = IconManager.getDescriptor("/icons/editor/folders.gif");
	private ImageDescriptor iconSort = IconManager.getDescriptor("/icons/editor/sort.png");

	private class AbstractAction
	extends Action
	{
		AbstractAction(String text, int style, ImageDescriptor image)
		{
			super(text,style);
			setImageDescriptor(image);
		}
	}

	private IAction actionViewStructure = new AbstractAction("toggle flat or structured view", IAction.AS_CHECK_BOX, iconFolders) {
		@Override
		public void run() {
			DisplayModeStructure structure;
			if (this.isChecked())
				structure = DisplayModeStructure.STRUCTURED;
			else 
				structure = DisplayModeStructure.FLAT;
			contentProvider.setDisplayMode(structure);
			ParsedOutlinePage.this.update();
		}
	};

	private IAction actionViewOrder = new AbstractAction("toggle alphabetical sorting", IAction.AS_CHECK_BOX, iconSort) {
		@Override
		public void run() {
			DisplayModeOrder order;
			if (this.isChecked())
				order = DisplayModeOrder.ALPHABETICAL;
			else 
				order = DisplayModeOrder.UNSORTED;
			contentProvider.setDisplayMode(order);
			ParsedOutlinePage.this.update();
		}
	};


}
