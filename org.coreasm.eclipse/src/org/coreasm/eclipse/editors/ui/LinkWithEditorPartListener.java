package org.coreasm.eclipse.editors.ui;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * 
 * This class is used to notify a view whenever a certain status 
 * change of an IEditorPart occurs.
 * 
 * @author marcel
 *
 */
public class LinkWithEditorPartListener implements IPartListener2 {
	  private final ILinkedWithASMEditorView view;

	  /**
	   * register the view to be notified if an IEditorPart changes its status
	   * @param view
	   */
	  public LinkWithEditorPartListener(ILinkedWithASMEditorView view) {
	    this.view = view;
	  }

	  public void partActivated(IWorkbenchPartReference ref) {
	    if (ref.getPart(true) instanceof IEditorPart) {
	      view.editorActivated(view.getViewSite().getPage().getActiveEditor());
	    }
	  }

	  public void partBroughtToTop(IWorkbenchPartReference ref) {
	    if (ref.getPart(true) instanceof IEditorPart) {
	      view.editorActivated(view.getViewSite().getPage().getActiveEditor());
	    }
	  }

	  public void partOpened(IWorkbenchPartReference ref) {
	    if (ref.getPart(true) instanceof IEditorPart) {
	      view.editorActivated(view.getViewSite().getPage().getActiveEditor());
	    }
	  }

	  public void partVisible(IWorkbenchPartReference ref) {
	    if (ref.getPart(true) instanceof IEditorPart) {
	      IEditorPart editor = view.getViewSite().getPage().getActiveEditor();
	      if(editor!=null) {
	        view.editorActivated(editor);
	      }
	    }
	  }

	  public void partClosed(IWorkbenchPartReference ref) {}
	  public void partDeactivated(IWorkbenchPartReference ref) {}
	  public void partHidden(IWorkbenchPartReference ref) {}
	  public void partInputChanged(IWorkbenchPartReference ref) {}
	}
