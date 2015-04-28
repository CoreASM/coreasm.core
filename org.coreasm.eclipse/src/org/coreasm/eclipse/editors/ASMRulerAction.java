package org.coreasm.eclipse.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.coreasm.eclipse.editors.quickfix.ASMQuickAssistProcessor;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.SelectMarkerRulerAction;

public class ASMRulerAction extends SelectMarkerRulerAction {
	private ITextEditor editor;
	private MarkerAnnotation annotation;
	private Position position;

	public ASMRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor, IVerticalRulerInfo ruler) {
		super(bundle, prefix, editor, ruler);
		this.editor = editor;
	}

	@Override
	public void run() {
		runWithEvent(null);
	}
	
	@Override
	public void runWithEvent(Event event) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		ASMQuickAssistProcessor.collectProposals(annotation, proposals);
		if (!proposals.isEmpty()) {
			ITextOperationTarget operation = (ITextOperationTarget)editor.getAdapter(ITextOperationTarget.class);
			if (operation != null && operation.canDoOperation(ISourceViewer.QUICK_ASSIST)) {
				editor.selectAndReveal(position.getOffset(), position.getLength());
				operation.doOperation(ISourceViewer.QUICK_ASSIST);
			}
		}
		super.run();
	}
	
	@Override
	public void update() {
		findMarkerAnnotation();
		setEnabled(true);
		super.update();
	}
	
	@SuppressWarnings("unchecked")
	private void findMarkerAnnotation() {
		annotation = null;
		
		AbstractMarkerAnnotationModel model = getAnnotationModel();
		if (model == null)
			return;
		
		IAnnotationAccessExtension annotationAccess = getAnnotationAccessExtension();
		
		Iterator<Annotation> iterator = model.getAnnotationIterator();
		int layer = -1;
		
		while (iterator.hasNext()) {
			Annotation a = iterator.next();
			if (!(a instanceof MarkerAnnotation) || a.isMarkedDeleted())
				continue;
			int l = annotationAccess.getLayer(a);
			Position p = model.getPosition(a);
			if (annotationAccess.getLayer(a) < layer || !includesRulerLine(p, getDocument()))
				continue;
			layer = l;
			position = p;
			annotation = (MarkerAnnotation)a;
		}
	}
}
