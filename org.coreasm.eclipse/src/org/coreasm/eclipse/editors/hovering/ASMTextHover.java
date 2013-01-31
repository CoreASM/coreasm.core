package org.coreasm.eclipse.editors.hovering;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * This class manages the retrieval of information from a marker which has
 * been hovered by the mouse cursor.
 * @author Markus Müller
 */
public class ASMTextHover
implements ITextHover, ITextHoverExtension, ITextHoverExtension2
{

	IInformationControlCreator icc = null;
	
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}

	/**
	 * This method retrieves the marker which has been hovered and reads the
	 * error object from it. The object which is returned by this method will be
	 * delivered to the ASMInformationControl. The ASMInformationControl expects
	 * this object to be a Map<String,Object> with two entries:
	 * <ul>
	 * <li>"document": a reference to the document the hover belongs to</li>
	 * <li>"marker": a reference to the marker which has been hovered</li>
	 * </ul>
	 */
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
	{
		Map<String, Object> hoverInfoMap = new HashMap<String,Object>();
		hoverInfoMap.put("document", textViewer.getDocument());
		
		IAnnotationModel model = null;
		if (textViewer instanceof ISourceViewer)
			model= ((ISourceViewer)textViewer).getAnnotationModel();
		if (model != null) {
			Iterator it;
			if (model instanceof IAnnotationModelExtension2)
				it = ((IAnnotationModelExtension2)model).getAnnotationIterator(hoverRegion.getOffset(), hoverRegion.getLength(), true, true);
			else
				it = model.getAnnotationIterator();
	
			while (it.hasNext()) {
				Annotation a = (Annotation) it.next();
				if (a instanceof MarkerAnnotation) {
					MarkerAnnotation ma = (MarkerAnnotation) a;
					IMarker m = ma.getMarker();
					hoverInfoMap.put("marker", m);
					return hoverInfoMap;
				}
			}
		}
		try {
			Node node = getNodeForRegion(textViewer.getDocument(), hoverRegion.getOffset(), hoverRegion.getLength());
			if (node instanceof ASTNode) {
				return node.toString();
			}
			return null;
		} catch (BadLocationException e) {
		}
		return null;
	}
	
	private Node getNodeForRegion(IDocument doc, int offset, int length) throws BadLocationException {
		ASMDocument asmDoc = (ASMDocument)doc;
		Stack<Node> queue = new Stack<Node>();
		Node node = null;
		Node rootNode = asmDoc.getRootnode();
		
		if (rootNode != null)
			queue.add(rootNode);
		int minDist = -1;
		while (!queue.isEmpty()) {
			Node n = queue.pop();
			int off = n.getScannerInfo().charPosition;
			if (doc.getLineOfOffset(off) == doc.getLineOfOffset(offset))
			{
				int dist = offset - off;
				
				if (minDist < 0 || dist < minDist) {
					node = n;
					minDist = dist;
				}
			}
			for (Node child : n.getChildNodes())
				queue.add(queue.size(), child);
		}
		return node;
	}
	
	@Override
	public IInformationControlCreator getHoverControlCreator() 
	{
		if (icc == null)
			icc = new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new ASMInformationControl(parent);
				}
			};

			return icc;
	}

	
	
	/**
	 * This is the old getHoverInfo method from ITextHover, which only supports
	 * returning Strings. Because we use getHoverInfo2 from ITextHoverExtension2
	 * this method just returns null. 
	 */
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
	{
		return null;
	}
	
}