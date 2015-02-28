package org.coreasm.eclipse.editors.views;

import java.util.Observable;
import java.util.Observer;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.ui.ASMASTContentProvider;
import org.coreasm.eclipse.editors.ui.ILinkedWithASMEditorView;
import org.coreasm.eclipse.editors.ui.LinkWithEditorPartListener;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.part.ViewPart;

public class ASMAstTreeView extends ViewPart implements ILinkedWithASMEditorView, ISelectionChangedListener, Observer {
	public ASMAstTreeView() {
	}

	private IPartListener2 linkWithEditorPartListener  = new LinkWithEditorPartListener(this);

	private TreeViewer treeViewer=null;
	private Label notification=null;

	private ASMEditor asmEditor=null;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 *
	 * initialize tree view with elements
	 */
	public void createPartControl(Composite parent) {;
	parent.setLayout(new GridLayout(1,false));
	notification = new Label(parent, SWT.NONE);
	notification.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	treeViewer = new TreeViewer(parent);
	treeViewer.addSelectionChangedListener(this);	
	Tree tree = treeViewer.getTree();
	tree.setLayoutData(new GridData(GridData.FILL_BOTH));

	//register own view to selection provider of the site
	getSite().setSelectionProvider(treeViewer);

	//register view to part listener which will invoke the method editorActivated if a IEditorPart is selected
	getSite().getPage().addPartListener(linkWithEditorPartListener);

	}

	@Override
	public void setFocus() {

	}

	/*
	 * resets the ContentProvider => updates views content
	 */
	private void refresh() {
		//has to be synchronous to avoid null pointer exception caused be closing the editor
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if(asmEditor.getParser().getRootNode()==null)
				{
					notification.setText("Parse error in ASM editor "+asmEditor.getTitle());
					treeViewer.getTree().removeAll();
				}else
				{
					notification.setText("AST of editor "+asmEditor.getTitle());
					treeViewer.setContentProvider(new ASMASTContentProvider(asmEditor.getDocumentProvider(), asmEditor.getParser()));
					treeViewer.setInput(asmEditor.getParser().getRootNode());
				}
			}
		});	
	}
	
	/*
	 * This method is invoked if an EditorPart is selected.
	 * The (un)registers the view as observer to the editor's parser
	 * and causes a refresh of the treeViewer.
	 *
	 * @param IEditorPart activeEditor selected editor component
	 */
	@Override
	public void editorActivated(IEditorPart activeEditor) {

		/*
		 * if a different Editor has been selected, 
		 * the parser should not be observed any more.
		 * if the new editor is an ASMEditor its parser has to be observed instead.
		 */ 
		if (activeEditor instanceof ASMEditor){
			//remember the currently active ASMEditor and observe its ASMParser
			if (asmEditor != null)
				asmEditor.getParser().deleteObserver(this);
			asmEditor=(ASMEditor)activeEditor;
			asmEditor.getParser().addObserver(this);
			refresh();
		}	
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 *
	 *	if the parser has changed, this view should be updated
	 *  Parser invokes update and submits either true or false as argument 
	 *  representing the state of success for the last run of the parser.
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (asmEditor!=null)
			refresh();
	}

	/**
	 * if a tree node is selected, the corresponding line
	 * in of the the asmEditor will be highlighted.
	 * 
	 * (no multiselection)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {

		ISelection selection = event.getSelection();
		if (selection.isEmpty())
			asmEditor.resetHighlightRange();
		else
		{
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object element = sel.getFirstElement();

			try
			{
				setHighlighting(element);
			}
			catch (IllegalArgumentException exeption)
			{
				asmEditor.resetHighlightRange();
			}
		}
	}

	protected void setHighlighting(Object element) {
		if (element instanceof Node) {
			Node node = (Node) element;
			ASMDocument document = (ASMDocument) asmEditor.getInputDocument();
			asmEditor.setHighlightRange(document.getUpdatedOffset(document.getNodePosition(node)), document.calculateLength(node), true);
		} else
			asmEditor.resetHighlightRange();
	}
}