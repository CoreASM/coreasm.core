package org.coreasm.eclipse.editors.ui;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;

/**
 * @author marcel
 *
 */
public interface ILinkedWithASMEditorView {
 /**
   * Called when an editor is activated
   * e.g. by a click from the user.
   * @param The activated editor part.
   */
  void editorActivated(IEditorPart activeEditor);

  /**
   * @return The site for this view.
   */
  IViewSite getViewSite();
}