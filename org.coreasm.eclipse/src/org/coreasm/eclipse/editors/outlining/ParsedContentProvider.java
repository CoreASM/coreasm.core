package org.coreasm.eclipse.editors.outlining;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IDocumentProvider;

import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.ASMParser;
import org.coreasm.eclipse.editors.AstTools;
import org.coreasm.eclipse.editors.FileManager;
import org.coreasm.eclipse.editors.outlining.OutlineTreeNode.OutdatedTreeNode;
import org.coreasm.eclipse.editors.outlining.OutlineTreeNode.UnavailableTreeNode;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.modularity.IncludeNode;

/**
 * Content provider for ParsedOutlinePage
 * @author Markus Müller, Tobias Seyfang
 */
public class ParsedContentProvider implements ITreeContentProvider 
{
	static enum DisplayModeStructure {
		FLAT,			// All nodes should be added as children of the "CoreASM id" rootnode, without groups.
		STRUCTURED		// All nodes of a certain type should be structured in their own group.
	}
	
	static enum DisplayModeOrder {
		UNSORTED,		// The nodes within a group should be in the same order as in the specification. 
		ALPHABETICAL	// The nodes within a group should be sorted alphabetically.
	}
	
	private final static String AST_POSITIONS = "__ast_position";
	private IDocumentProvider documentProvider;
	private IPositionUpdater positionUpdater = new DefaultPositionUpdater(AST_POSITIONS);
	private ASMEditor editor;
	private ASMParser parser;
	private DisplayModeStructure outlineStructure;	// how should the tree be structured?
	private DisplayModeOrder outlineOrder;			// how should the tree be sorted?
	private ParsedOutlinePage outlinePage;
	private ArrayList<Object> lastRootList;			// last root list is displayed when parsing fails
	private ArrayList<Object> externRootList;		// root list which contains root nodes from plugins
	
	
	public ParsedContentProvider(IDocumentProvider documentProvider,
			ASMEditor editor) 
	{
		super();
		this.documentProvider = documentProvider;
		this.editor = editor;
		this.parser = editor.getParser();
		this.outlineStructure = DisplayModeStructure.STRUCTURED;
		this.outlineOrder = DisplayModeOrder.UNSORTED;
		externRootList = new ArrayList<Object>();
		lastRootList = null;
	}
	
	/**
	 * @param rootNode	Root node from an extern source (plugin)
	 */
	public void addRootNode(RootOutlineTreeNode rootNode) {
		externRootList.add(rootNode);
	}
	
	/**
	 * @param rootNode	Root node from an extern source (plugin)
	 */
	public void removeRootNode(RootOutlineTreeNode rootNode) {
		externRootList.remove(rootNode);
	}
	
	void setOutlinePage(ParsedOutlinePage outlinePage)
	{
		this.outlinePage = outlinePage;
	}

	public void setDisplayMode(DisplayModeStructure structure, DisplayModeOrder order)
	{
		this.outlineStructure = structure;
		this.outlineOrder = order;
	}

	public void setDisplayMode(DisplayModeStructure structure)
	{
		this.outlineStructure = structure;
	}

	public void setDisplayMode(DisplayModeOrder order)
	{
		this.outlineOrder = order;
	}

	@Override
	public void dispose()
	{

	}

	/**
	 * This method is called if the input of the ASMEditor this content provider
	 * belongs to has changed. If this happens, the ContentProvider must remove
	 * its PositionUpdater from the old input and add it to the new input. 
	 * The PositionUpdater watches the input of the ASMEditor for changes and
	 * keeps the positions the outline node are rerferring to up to date.
	 */
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
				} catch (BadPositionCategoryException e) {
					e.printStackTrace();  // TODO why does this exception occur?
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

	/**
	 * This method returns the root element of the outline tree. If there is 
	 * no tree to be displayed the DUMMY node is displayed.
	 * Otherwise an OutlineTreeNode of type ROOT is created as the root.
	 */
	@Override
	public Object[] getElements(Object inputElement)
	{
		// ensure the current page has a double click listener
		outlinePage.setupListener();
		
		ArrayList<Object> rootList = new ArrayList<Object>();
		
		Node n = parser.getRootNode();
		if (n == null && lastRootList == null) {
			// We have neither an actual nor an old syntax tree
			// -> display DUMMY node
			rootList.add(new UnavailableTreeNode());
			outlinePage.enableActions(false);		
		}
		else if (n == null && lastRootList != null) {
			// We have no actual but an old outline syntax tree
			// -> display the old tree
			rootList.add(new OutdatedTreeNode());
			rootList.addAll(lastRootList);
		}
		else {
			// We have an actual syntax tree
			// -> generate a new root node
			String description = n.getChildNodes().get(1).getToken();
			String suffix = null;
			if (((ASTNode)n).getGrammarRule().equals("CoreModule"))
				suffix = "CoreModule";
			RootOutlineTreeNode oRootNode = new RootOutlineTreeNode(n.toString(), description, suffix);
			
			// append root children to oRootNode (groupNodes and allNodes)
			for (Node childNode : n.getChildNodes()) {
				appendChild(childNode, oRootNode);
			}
			
			// add root and extern root to list
			rootList.add(oRootNode);
			rootList.addAll(externRootList);
			lastRootList = new ArrayList<Object>(rootList);
			
			outlinePage.enableActions(true);
		}
		
		return rootList.toArray();
	}

	/**
	 * @param childNode
	 * @param oRootNode		Root node which to 
	 * 
	 * Appends a child to oRootNode
	 * Extracts the childNode description depending on the type (e.g GRAMMAR_RULE)
	 */
	private void appendChild(Node childNode, RootOutlineTreeNode oRootNode) {
		if (childNode instanceof ASTNode) {
			ASTNode astNode = (ASTNode) childNode;
			
			if (astNode.getGrammarRule().equals("UseClauses")) {		
				String description = astNode.getChildNodes().get(1).getToken();
				oRootNode.addNode(new OutlineTreeNode.UseTreeNode(astNode.toString(), description));
			}
			
			else if (astNode.getGrammarRule().equals(AstTools.GRAMMAR_RULE)) {
				String description = astNode.getChildNodes().get(1).getChildNodes().get(0).getToken();
				oRootNode.addNode(new OutlineTreeNode.RuleTreeNode(astNode.toString(), description));
			}
			
			else if (astNode.getGrammarRule().equals(AstTools.GRAMMAR_INIT)) {
				String description = astNode.getChildNodes().get(1).getToken();
				oRootNode.addNode(new OutlineTreeNode.InitTreeNode(astNode.toString(), description));
			}
			
			else if (astNode.getGrammarRule().equals("Signature")) {
				String description = AstTools.findId(astNode);
				oRootNode.addNode(new OutlineTreeNode.SignatureTreeNode(astNode.toString(), description));
			}
			
			else if (astNode.getGrammarRule().equals("PropertyOption")) {	
				String suffix = AstTools.findId(astNode);
				String description = astNode.getChildNodes().get(1).getToken();
				oRootNode.addNode(new OutlineTreeNode.OptionTreeNode(astNode.toString(), description, suffix));
			}
		}
		
		// IncludeNode is not part of ASTNode
		if (childNode instanceof IncludeNode) {
			IncludeNode iNode = (IncludeNode) childNode;
			String filenameFromProj = "";
			try {
				//IFile file = FileManager.getFile(iNode.getFilename(), FileManager.getActiveProject());
				filenameFromProj = FileManager.getFilenameRelativeToProject(iNode.getFilename(), editor.getInputFile());
			} catch (Exception e) {
				e.printStackTrace();
			} 

			String description = iNode.getFilename();
			String suffix = filenameFromProj;
			
			oRootNode.addNode(
				new OutlineTreeNode.IncludeTreeNode(childNode.toString(), description, suffix));
		}
	}

	/**
	 * @param parentElement The parent for which the children are returned, must
	 * 						be an instance of class OutlineTreeNode.
	 * 
	 * Gets the child nodes for the given OutlineTreeNode.
	 * 
	 * Returns all nodes when FLAT is set. 
	 * Else return group nodes if the parentElement is a RootOutlineTreeNode or
	 * return children of a group node
	 */
	@Override
	public Object[] getChildren(Object parentElement)
	{
		// Wrong class
		if ( ! (parentElement instanceof OutlineTreeNode) )
			return null;

		OutlineTreeNode treeNode = (OutlineTreeNode) parentElement;
		
		if (outlineStructure == DisplayModeStructure.FLAT) {
			if (treeNode instanceof RootOutlineTreeNode) {
				RootOutlineTreeNode rootNode = (RootOutlineTreeNode) treeNode;
				ArrayList<OutlineTreeNode> allNodes = rootNode.getAllNodes(outlineOrder);
				return allNodes.toArray();
			}
		}
		else if (outlineStructure == DisplayModeStructure.STRUCTURED) {
			if (treeNode instanceof RootOutlineTreeNode) {
				RootOutlineTreeNode rootNode = (RootOutlineTreeNode) treeNode;
				Collection<GroupOutlineTreeNode> groupNodes = rootNode.getGroupNodes();
				
				return groupNodes.toArray();
			}
			else if (treeNode instanceof GroupOutlineTreeNode) {
				GroupOutlineTreeNode groupNode = (GroupOutlineTreeNode) treeNode;
				return groupNode.getChildren(outlineOrder).toArray();
			}
		}
			
		return null;
	}

	@Override
	public Object getParent(Object element) {
		// obviously the content provider works without that method
		// so we just return null
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof RootOutlineTreeNode) 
			return true;
		
		if (element instanceof GroupOutlineTreeNode) 
			return true;
		
		return false;
	}	
}
