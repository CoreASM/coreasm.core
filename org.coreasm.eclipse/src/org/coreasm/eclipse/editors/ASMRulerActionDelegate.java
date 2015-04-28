package org.coreasm.eclipse.editors;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class ASMRulerActionDelegate extends AbstractRulerActionDelegate {

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo ruler) {
		return new ASMRulerAction(ResourceBundle.getBundle("org.coreasm.eclipse.editors.ASMEditorMessages"), "ASMRulerAction.", editor, ruler);
	}

}
