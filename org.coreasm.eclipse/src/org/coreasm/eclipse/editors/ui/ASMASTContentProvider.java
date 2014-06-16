package org.coreasm.eclipse.editors.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IDocumentProvider;

import org.coreasm.eclipse.editors.ASMParser;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.Node.NameNodeTuple;

public class ASMASTContentProvider implements ITreeContentProvider 
{
	private IDocumentProvider documentProvider;
	private ASMParser parser;
	private String dummy = "outline currently not available";
	
	protected final static String AST_POSITIONS = "__ast_position";
	protected IPositionUpdater positionUpdater = new DefaultPositionUpdater(AST_POSITIONS); 
	
	private class ContentWrapper {
		private final NameNodeTuple content;
		
		private ContentWrapper(NameNodeTuple content) {
			this.content = content;
		}
		
		@Override
		public String toString() {
			if (!Node.DEFAULT_NAME.equals(content.name))
				return content.name + ": " + content.node;
			return content.node.toString();
		}
	}
	
	public ASMASTContentProvider(IDocumentProvider provider, ASMParser parser)
	{
		if (provider != null && parser != null){
			this.documentProvider = provider;
			this.parser = parser;
		}else{
			try {
				throw new Exception("wrong initialization of SystaxOutlineContentProvider ");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void dispose() 
	{

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
	{
		if (oldInput != null)
		{
			IDocument document = documentProvider.getDocument(oldInput);
			if (document != null)
			{
				try
				{
					document.removePositionCategory(AST_POSITIONS);
				}
				catch (BadPositionCategoryException x)
				{	
				}
				document.removePositionUpdater(positionUpdater);
			}
		}
		
		if (newInput != null)
		{
			IDocument document = documentProvider.getDocument(newInput);
			if (document != null) {
				document.addPositionCategory(AST_POSITIONS);
				document.addPositionUpdater(positionUpdater);
			}
		}
	}

	@Override
	public Object[] getElements(Object inputElement) 
	{
		Object o = parser.getRootNode();
		if (o == null)
			o = dummy;
		
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) 
	{
		if (parentElement instanceof ContentWrapper)
			parentElement = ((ContentWrapper) parentElement).content.node;
		if (parentElement instanceof Node) {
			Node node = (Node) parentElement;
			List<NameNodeTuple> allChildren = node.getChildNodesWithNames();
			List<ContentWrapper> returnChildren = new ArrayList<ContentWrapper>();
			for (NameNodeTuple n: allChildren)
				if ( ! n.node.getConcreteNodeType().equals("delimiter"))
					returnChildren.add(new ContentWrapper(n));
			return returnChildren.toArray();
		}
		else return null;
	}

	@Override
	public Object getParent(Object element) 
	{
		if (element instanceof ContentWrapper)
			element = ((ContentWrapper) element).content.node;
		if (element instanceof Node)
			return ((Node)element).getParent();
		else return null;
	}

	@Override
	public boolean hasChildren(Object element)
	{
		if (element instanceof ContentWrapper)
			element = ((ContentWrapper) element).content.node;
		if (element instanceof Node)
			return ((Node)element).getChildNodes().size() > 0;
		else return false;
	}

	
}
