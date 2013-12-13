package org.coreasm.eclipse.editors.ui;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.eclipse.editors.ASMParser;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class ASMASTContentProvider implements ITreeContentProvider 
{
	private IDocumentProvider documentProvider;
	private ASMParser parser;
	private String dummy = "outline currently not available";
	
	protected final static String AST_POSITIONS = "__ast_position";
	protected IPositionUpdater positionUpdater = new DefaultPositionUpdater(AST_POSITIONS); 
	
	public ASMASTContentProvider(IDocumentProvider provider, ASMParser parser)
	{
		if (provider != null && parser != null){
			this.documentProvider = provider;
			this.parser = parser;
		}else{
			try {
				throw new Exception("wrong initialization of SystaxOutlineContentProvider ");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void dispose() 
	{
		// TODO Auto-generated method stub

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
		if (parentElement instanceof Node) {
			Node node = (Node) parentElement;
			List<Node> allChildren = node.getChildNodes();
			List<Node> returnChildren = new ArrayList<Node>();
			for (Node n: allChildren)
				if ( ! n.getConcreteNodeType().equals("delimiter"))
					returnChildren.add(n);
			return returnChildren.toArray();
		}
		else return null;
	}

	@Override
	public Object getParent(Object element) 
	{
		if (element instanceof Node)
			return ((Node)element).getParent();
		else return null;
	}

	@Override
	public boolean hasChildren(Object element)
	{
		if (element instanceof Node)
			return ((Node)element).getChildNodes().size() > 0;
		else return false;
	}

	
}
