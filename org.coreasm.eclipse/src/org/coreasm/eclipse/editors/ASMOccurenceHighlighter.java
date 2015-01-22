package org.coreasm.eclipse.editors;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.kernel.UpdateRuleNode;
import org.coreasm.engine.plugins.turboasm.ReturnResultNode;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class ASMOccurenceHighlighter implements IASMSelectionListener {
	private static final String ANNOTATION_OCCURRENCE = "org.coreasm.eclipse.ui.occurrence";
	private static final String ANNOTATION_OCCURRENCE_WRITE = "org.coreasm.eclipse.ui.occurrence.write";
	
	private Annotation[] annotations;
	
	public ASMOccurenceHighlighter(ASMEditor editor) {
		editor.addPostSelectionListener(this);
	}

	@Override
	public void selectionChanged(ASMEditor editor, ITextSelection selection, ASTNode root) {
		removeAnnotations(editor);
		if (root == null)
			return;
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		ASMDocument document = (ASMDocument)documentProvider.getDocument(editor.getEditorInput());
		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());
		ASTNode node = document.getIDnodeAt(selection.getOffset());
		Map<Annotation, Position> annotationMap = new HashMap<Annotation, Position>();
		if (node != null) {
			Stack<ASTNode> fringe = new Stack<ASTNode>();

			fringe.add(root);
			while (!fringe.isEmpty()) {
				ASTNode n = fringe.pop();
				if (ASTNode.ID_CLASS.equals(n.getGrammarClass()) && node.getToken().equals(n.getToken())) {
					String type = ANNOTATION_OCCURRENCE;
					if (n.getParent() != null && n.getParent().getParent() != null) {
						ASTNode grandParent = n.getParent().getParent();
						if ((grandParent instanceof UpdateRuleNode || grandParent instanceof ReturnResultNode) && grandParent.getFirst() == n.getParent())
							type = ANNOTATION_OCCURRENCE_WRITE;
					}
					Annotation annotation = new Annotation(type, false, (ANNOTATION_OCCURRENCE_WRITE.equals(type) ? "Write Occurrence" : "Occurrence") + " of " + n.getToken());
					annotationMap.put(annotation, new Position(document.getNodePosition(n), n.getToken().length()));
				}
				for (ASTNode child : n.getAbstractChildNodes())
					fringe.add(fringe.size(), child);
			}
			if (annotationModel instanceof IAnnotationModelExtension)
				((IAnnotationModelExtension)annotationModel).replaceAnnotations(annotations, annotationMap);
			else {
				for (Entry<Annotation, Position> annotation : annotationMap.entrySet())
					annotationModel.addAnnotation(annotation.getKey(), annotation.getValue());
			}
			annotations = annotationMap.keySet().toArray(new Annotation[annotationMap.keySet().size()]);
		}
	}
	
	private void removeAnnotations(ASMEditor editor) {
		if (annotations == null)
			return;
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider == null)
			return;
		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());
		if (annotationModel == null)
			return;
		if (annotationModel instanceof IAnnotationModelExtension)
			((IAnnotationModelExtension)annotationModel).replaceAnnotations(annotations, null);
		else {
			for (Annotation annotation : annotations)
				annotationModel.removeAnnotation(annotation);
		}
		annotations = null;
	}
}
