package org.coreasm.eclipse.editors.outlining;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * This class is an abstract base class for ContentPages for the Outlining for an
 * ASMEditor.
 * @author Markus Müller
 */
public abstract class AbstractContentPage
extends ContentOutlinePage
implements Observer
{
	protected ITextEditor parentEditor;	// the editor instance this page belongs to
	private IEditorInput input;			// the input of the editor
	
	// content and label providers:
	private ITreeContentProvider contentProvider;
	private IBaseLabelProvider labelProvider;
	
	/**
	 * Genearates a new AbstractContentPage. If the subclass doesn't provide a
	 * content provider or a label provider, a default implementation is created.
	 */
	public AbstractContentPage(ITextEditor parentEditor,
			ITreeContentProvider contentProvider,
			IBaseLabelProvider labelProvider)
	{
		super();
		this.parentEditor = parentEditor;
		
		if (contentProvider != null)
			this.contentProvider = contentProvider;
		else
			this.contentProvider = new TreeNodeContentProvider();
		
		if (labelProvider != null)
			this.labelProvider = labelProvider;
		else
			this.labelProvider = new LabelProvider();
	}
	
	
	
	public ITreeContentProvider getContentProvider()
	{
		return contentProvider;
	}

	public IBaseLabelProvider getLabelProvider()
	{
		return labelProvider;
	}

	/**
	 * Creates a TreeViewer for visualizing this page and sets the content
	 * and label providers for the TreeViewer.
	 */
	@Override
	public void createControl(Composite parent)
	{
		super.createControl(parent);
		
		TreeViewer viewer = getTreeViewer();

		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		
		viewer.addSelectionChangedListener(this);
		
		if (input != null)
			viewer.setInput(input);
	}
	
	/**
	 * Sets a new input for the ContentPage and updates the TreeViewer
	 * @param input 	The new input for the view.
	 */
	public void setInput(Object input)
	{
		this.input = (IEditorInput) input;
		update();
	}
	
	/**
	 * Updates and redraws the tree viewer 
	 */
	protected void update()
	{
		TreeViewer viewer = getTreeViewer();
		
		if (viewer != null)
		{
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed())
			{
				control.setRedraw(false);
				viewer.setInput(input);
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
	
	/**
	 * This method is called after each run of the parser, as defined by the
	 * Observer interface. It calls the update() method, so the outline view
	 * gets updated after each run of the parser. 
	 */
	@Override
	public void update(Observable o, Object arg)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				update();
			}
		});
	}



	/**
	 * This method is called if the user clicks onto a node of the outline tree.
	 * In this case, the editor should highlight the region which the clicked
	 * node refers to. This method retrieves the selected element from the tree
	 * and delegates it to the abstract method setHiglighting.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) 
	{
		super.selectionChanged(event);
		
		ISelection selection = event.getSelection();
		if (selection.isEmpty())
			parentEditor.resetHighlightRange();
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
				parentEditor.resetHighlightRange();
			}
		}
	}
	
	/**
	 * This method must be implemented by subclasses to retrieve the information
	 * out of the given element. This element is typically one of the object which
	 * were provided by the content provider of the subclass.
	 */
	protected abstract void setHighlighting(Object element);
	
}
