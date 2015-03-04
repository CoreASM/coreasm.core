package org.coreasm.eclipse.editors.outlining;

import java.util.Observable;
import java.util.Observer;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.FileManager;
import org.coreasm.eclipse.util.IconManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class ASMOutlinePage extends ContentOutlinePage implements Observer {
	private ASMEditor editor;
	
	private ASMContentProvider contentProvider;
	private StyledCellLabelProvider labelProvider;

	public ASMOutlinePage(ASMEditor editor) {
		super();
		this.editor = editor;
		editor.getParser().addObserver(this);
		labelProvider = new ASMLabelProvider();
		contentProvider = new ASMContentProvider();
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		TreeViewer viewer = getTreeViewer();

		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		
		viewer.addSelectionChangedListener(this);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TreeSelection s = (TreeSelection) event.getSelection();
				TreePath[] paths = s.getPaths();
				for (TreePath p: paths) {
					Object last = p.getLastSegment();
					if (!(last instanceof ASMOutlineTreeNode))
						continue;
					ASMOutlineTreeNode node = (ASMOutlineTreeNode) last;
					if (!"Included Files".equals(node.getGroup()))
						continue;
					String filename = node.getSuffix();
					String filenameFromProj = null;
					try {
						filenameFromProj = FileManager.getFilenameRelativeToProject(filename, editor.getInputFile());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					FileManager.openEditor(filenameFromProj, editor.getInputFile().getProject());
					break;
				}
			}
		});
		update();
	}
	
	protected void update() {
		TreeViewer viewer = getTreeViewer();
		if (viewer != null && !viewer.getControl().isDisposed()) {
			Control control = viewer.getControl();
			control.setRedraw(false);
			viewer.setInput(editor.getEditorInput());
			control.setRedraw(true);
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				update();
			}
		});
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		
		ISelection selection = event.getSelection();
		if (selection.isEmpty())
			editor.resetHighlightRange();
		else {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object element = sel.getFirstElement();
			
			try {
				if (element instanceof ASMOutlineTreeNode) {
					ASMOutlineTreeNode node = (ASMOutlineTreeNode) element;
					if (node.getNode() == null)
						return;

					ASMDocument document = (ASMDocument)editor.getInputDocument();
					editor.setHighlightRange(document.getUpdatedOffset(document.getNodePosition(node.getNode())), document.calculateLength(node.getNode()), true);
				}
				else
					editor.resetHighlightRange();
			}
			catch (IllegalArgumentException exeption) {
				editor.resetHighlightRange();
			}
		}
	}
	
	@Override
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);

		IToolBarManager manager = actionBars.getToolBarManager();
		Action action = new Action("Group up contents", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				contentProvider.setDisplayGroups(this.isChecked());
				update();
			}
		};
		action.setImageDescriptor(IconManager.getDescriptor("/icons/editor/folders.gif"));
		action.setChecked(contentProvider.isDisplayStructured());
		manager.add(action);
		action = new Action("Sort contents", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				contentProvider.setDisplaySorted(this.isChecked());
				update();
			}
		};
		action.setImageDescriptor(IconManager.getDescriptor("/icons/editor/sort.png"));
		action.setChecked(contentProvider.isDisplaySorted());
		manager.add(action);
	}
}
