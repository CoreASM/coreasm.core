package org.coreasm.eclipse.editors.outlining;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.ASMParser;
import org.coreasm.eclipse.editors.AstTools;
import org.coreasm.eclipse.editors.FileManager;
import org.coreasm.eclipse.editors.outlining.OutlineTreeNode.NodeType;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.modularity.IncludeNode;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Content provider for ParsedOutlinePage
 * @author Markus M�ller
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
	
	/** This content provider manages a set of lists for nodes of different grammar types.
	 * These lists are identified by this enumeration. */
	static enum ListNames {
		ALL_NODES, USE_NODES, INIT_NODES, RULE_NODES, SIGNATURE_NODES, OPTION_NODES, INCLUDE_NODES
	}
	
	private final static String AST_POSITIONS = "__ast_position";
	private final static String DUMMY_STRING = "content outline currently not available";
	private final static String OUTDATED_STRING = "outline view is outdated because of errors";
	
	// This node is displayed if there is no syntax tree
	private final static OutlineTreeNode DUMMY = 
			new OutlineTreeNode(NodeType.UNAVAILABLE_NODE, DUMMY_STRING, null, null, 0, 1);
	
	// This node is shown on top of an old syntax tree if the tree is outdated
	// because of syntax errors.
	private final static OutlineTreeNode OUTDATED =
			new OutlineTreeNode(NodeType.OUTDATED_NODE, OUTDATED_STRING, null, null, 0, 1);

	private IDocumentProvider documentProvider;
	private IPositionUpdater positionUpdater = new DefaultPositionUpdater(AST_POSITIONS);
	private ASMEditor editor;
	private ASMParser parser;
	private DisplayModeStructure outlineStructure;	// how should the tree be structured?
	private DisplayModeOrder outlineOrder;			// how should the tree be sorted?
	private ParsedOutlinePage outlinePage;
	private ParsingResult result;			// the result from the last run of parseSyntaxTree()
	private OutlineTreeNode lastRoot;		// the root node of the currently displayed tree.
	private boolean isOutdated;				// true if the last parsing caused an syntax error
	

	public ParsedContentProvider(IDocumentProvider documentProvider,
			ASMEditor editor) 
	{
		super();
		this.documentProvider = documentProvider;
		this.editor = editor;
		this.parser = editor.getParser();
		this.outlineStructure = DisplayModeStructure.STRUCTURED;
		this.outlineOrder = DisplayModeOrder.UNSORTED;
		this.result = null;
		this.lastRoot = null;
		this.isOutdated = true;
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
		
		Object[] root = new Object[1];	// the array to be returned
		
		Node n = parser.getRootNode();
		if (n == null && lastRoot == null) {
			// We have neither an actual nor an old syntax tree
			// -> display DUMMY node
			root[0] = DUMMY;	
			outlinePage.enableActions(false);
		}
		else if (n == null && lastRoot != null) {
			// We have no actual but an old outline syntax tree
			// -> display the old tree
			root[0] = lastRoot;
			isOutdated = true;
		}
		else {
			// We have an actual syntax tree
			// -> generate a new root node
			String specId = n.getChildNodes().get(1).getToken();
			int position = getPositionFromSyntaxNode(n);
			String suffix = null;
			if (((ASTNode)n).getGrammarRule().equals("CoreModule"))
				suffix = "CoreModule";
			OutlineTreeNode oNode = new OutlineTreeNode(NodeType.ROOT_NODE, specId, null, suffix, position, 1);
			root[0]  = oNode;
			lastRoot = oNode;
			isOutdated = false;
			
			// We need to scan the new syntax tree to create a new ParsingResult object
			result = new ParsingResult();
			parseSyntaxTree(n, result);
			outlinePage.enableActions(true);
		}
		
		return root;
	}

	/**
	 * Gets the child nodes for the given OutlineTreeNode
	 * @param parentElement The parent for which the children are returned, must
	 * 						be an instance of class OutlineTreeNode.
	 */
	@Override
	public Object[] getChildren(Object parentElement)
	{
		// Wrong class
		if ( ! (parentElement instanceof OutlineTreeNode) )
			return null;
		
		// The parseSyntaxTree() method had to be called previously
		if ( result == null )
			return null;

		OutlineTreeNode parentNode = (OutlineTreeNode) parentElement;
		
		// Choose map with the correct order
		Map<ListNames, List<OutlineTreeNode>> listMap;
		listMap = result.lists.get(outlineOrder);
		
		// FLAT VIEW: if the parent node is the root, add all nodes as children of root
		// otherwise return no children
		if (outlineStructure == DisplayModeStructure.FLAT) {
			if (parentNode.type == NodeType.ROOT_NODE) {
				List<OutlineTreeNode> listAllNodes;
				listAllNodes = listMap.get(ListNames.ALL_NODES);
				if ( !isOutdated )
					return listAllNodes.toArray();
				else {
					List<OutlineTreeNode> l = new LinkedList<OutlineTreeNode>(listAllNodes);
					l.add(0, OUTDATED);
					return l.toArray();
				}
			}
			else
				return null;
		}
				
		// STRUCTURED VIEW: if the parent node is the root, generate a group node
		// for each node type and return them as children of root.
		// if the parent node is one of these group node return all nodes from the
		// list with the correct type.
		// Otherwise, return no children
		if (outlineStructure == DisplayModeStructure.STRUCTURED) {
			
			if (parentNode.type == NodeType.ROOT_NODE) {
				// Create group nodes
				OutlineTreeNode useGroupNode = new OutlineTreeNode(NodeType.GROUP_NODE, "Used Plugins", ListNames.USE_NODES, null, 0, 1);
				OutlineTreeNode ruleGroupNode = new OutlineTreeNode(NodeType.GROUP_NODE, "Rule Definitions", ListNames.RULE_NODES, null, 0, 1);
				OutlineTreeNode signGroupNode = new OutlineTreeNode(NodeType.GROUP_NODE, "Signatures", ListNames.SIGNATURE_NODES, null, 0, 1);
				OutlineTreeNode optionGroupNode = new OutlineTreeNode(NodeType.GROUP_NODE, "Options", ListNames.OPTION_NODES, null, 0, 1);
				OutlineTreeNode includeGroupNode = new OutlineTreeNode(NodeType.GROUP_NODE, "Included Files", ListNames.INCLUDE_NODES, null, 0, 1);

				// create group node for init nodes only if there is more than one init
				// otherwise show the init node as a direct child of the root, if there is one.
				OutlineTreeNode initNode = null;
				List<OutlineTreeNode> listInit = listMap.get(ListNames.INIT_NODES);
				if (listInit.size() > 1)
					initNode = new OutlineTreeNode(NodeType.GROUP_NODE, "Initialization", ListNames.INIT_NODES, null, 0, 1);
				else if (listInit.size() == 1)
					initNode = listInit.get(0);
				
				// add outdated node if necessary
				OutlineTreeNode outdatedNode = null;
				if (isOutdated) outdatedNode = OUTDATED;

				// Create a list with all group nodes, then remove all groups which are null or empty
				OutlineTreeNode[] _children = new OutlineTreeNode[] {
					outdatedNode, useGroupNode, signGroupNode, optionGroupNode, includeGroupNode, initNode, ruleGroupNode
				};

				List<OutlineTreeNode> children = new LinkedList<OutlineTreeNode>();
				for (OutlineTreeNode child: _children) {
					// don't add null references:
					if (child == null)
						continue;
					// add all nodes which are no group nodes:
					if (child.type != NodeType.GROUP_NODE) {
						children.add(child);
						continue;
					}
					// only add group nodes with at least one element:
					// (get the number of elements from the list for the NodeType of the group. 
					if (listMap.get(child.tag).size() > 0)
						children.add(child);
				}
				
				return children.toArray();
			}
			else if (parentNode.type == NodeType.GROUP_NODE) {
				// if the method is called for a group node, add all nodes
				// from the list for the NodeType of the group.
				List<OutlineTreeNode> listNodes = null;
				if (parentNode.tag == ListNames.USE_NODES)
					listNodes = listMap.get(ListNames.USE_NODES);
				if (parentNode.tag == ListNames.INIT_NODES)
					listNodes = listMap.get(ListNames.INIT_NODES);
				if (parentNode.tag == ListNames.RULE_NODES)
					listNodes = listMap.get(ListNames.RULE_NODES);
				if (parentNode.tag == ListNames.SIGNATURE_NODES)
					listNodes = listMap.get(ListNames.SIGNATURE_NODES);
				if (parentNode.tag == ListNames.OPTION_NODES)
					listNodes = listMap.get(ListNames.OPTION_NODES);
				if (parentNode.tag == ListNames.INCLUDE_NODES)
					listNodes = listMap.get(ListNames.INCLUDE_NODES);
				if (listNodes != null)
					return listNodes.toArray();
				else
					return null;
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
		// method is currently always returning true,
		// the result of getChildren() will decide if
		// the specified element has children.
		return true;
	}
	
	/**
	 * Reads the position of a node from the syntax tree by parsing it out of
	 * the result of toString() of that node. The position is at the end of that
	 * String, preceded by "@"
	 */
	private int getPositionFromSyntaxNode(Node node)
	{
		String strNode = node.toString();
		int position = 0;
		try {
			position = Integer.parseInt(
					strNode.substring(strNode.lastIndexOf('@')+1, strNode.length()-1));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return position;
	}
	
	/**
	 * This method parses a syntax tree specified by the given root node.
	 * It collects all nodes which are displayed in the outline view and
	 * stores them in different lists. The method walks through the syntax tree
	 * recursivly (pre-order). However, because we currently only display nodes
	 * for header elements we only need one recursion level.
	 * @param node
	 * @param result
	 */
	private void parseSyntaxTree(Node node, ParsingResult result)
	{
		Map<ListNames, List<OutlineTreeNode>> listsUnsorted =
				result.lists.get(DisplayModeOrder.UNSORTED);
			
		Map<ListNames, List<OutlineTreeNode>> listsAlpha =
				result.lists.get(DisplayModeOrder.ALPHABETICAL);
		
		// check if current syntax node should be added to the outline view
		// if yes, create an outline node for it and add the outline node
		// to the unsorted list for all nodes and the unsorted list for the
		// correct NodeType.
		if (node instanceof ASTNode) {
			ASTNode astNode = (ASTNode) node;
			
			if (astNode.getGrammarRule().equals("UseClauses")) {
				OutlineTreeNode oNode = new OutlineTreeNode(
					NodeType.USE_NODE,
					astNode.getChildNodes().get(1).getToken(),
					null, null,
					getPositionFromSyntaxNode(astNode),
					1
				);
				listsUnsorted.get(ListNames.ALL_NODES).add(oNode);
				listsUnsorted.get(ListNames.USE_NODES).add(oNode);
			}
			
			if (astNode.getGrammarRule().equals(AstTools.GRAMMAR_RULE)) {
				OutlineTreeNode oNode = new OutlineTreeNode(
					NodeType.RULE_NODE,
					node.getChildNodes().get(1).getChildNodes().get(0).getToken(),
					null, null,
					getPositionFromSyntaxNode(astNode),
					1
				);
				listsUnsorted.get(ListNames.ALL_NODES).add(oNode);
				listsUnsorted.get(ListNames.RULE_NODES).add(oNode);
			}
			
			if (astNode.getGrammarRule().equals(AstTools.GRAMMAR_INIT)) {
				OutlineTreeNode oNode = new OutlineTreeNode(
						NodeType.INIT_NODE,
						node.getChildNodes().get(1).getToken(),
						null, null,
						getPositionFromSyntaxNode(astNode),
						1
				);
				listsUnsorted.get(ListNames.ALL_NODES).add(oNode);
				listsUnsorted.get(ListNames.INIT_NODES).add(oNode);
			}
			
			if (astNode.getGrammarRule().equals("Signature")) {
				ASTNode child = astNode.getFirstASTNode();
				String description = AstTools.findId(child);
				/*if (child.getGrammarRule().equals("DerivedFunctionDeclaration"))
					description = child.getChildNodes().get(1).getChildNodes().get(0).getToken();
				else
					description = child.getChildNodes().get(1).getToken();*/
				
				OutlineTreeNode oNode = new OutlineTreeNode(
						NodeType.SIGNATURE_NODE,
						description,
						null, null,
						getPositionFromSyntaxNode(astNode),
						1
				);
				listsUnsorted.get(ListNames.ALL_NODES).add(oNode);
				listsUnsorted.get(ListNames.SIGNATURE_NODES).add(oNode);
			}
			
			if (astNode.getGrammarRule().equals("PropertyOption")) {
				String key = astNode.getChildNodes().get(1).getToken();
				String value = AstTools.findId(astNode);
				OutlineTreeNode oNode = new OutlineTreeNode(
					NodeType.OPTION_NODE,
					key,
					null,
					value,
					getPositionFromSyntaxNode(astNode),
					1
				);
				listsUnsorted.get(ListNames.ALL_NODES).add(oNode);
				listsUnsorted.get(ListNames.OPTION_NODES).add(oNode);
			}
			
		}
		
		// include nodes are not instances of ASTNode:
		if (node instanceof IncludeNode) {
			IncludeNode iNode = (IncludeNode) node;
			String filenameFromProj = null;
			try {
				//IFile file = FileManager.getFile(iNode.getFilename(), FileManager.getActiveProject());
				filenameFromProj = FileManager.getFilenameRelativeToProject(iNode.getFilename(), editor.getInputFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			OutlineTreeNode oNode = new OutlineTreeNode(
				NodeType.INCLUDE_NODE,
				iNode.getFilename(),
				null,
				filenameFromProj,
				getPositionFromSyntaxNode(iNode),
				1
			);
			listsUnsorted.get(ListNames.ALL_NODES).add(oNode);
			listsUnsorted.get(ListNames.INCLUDE_NODES).add(oNode);
		}
		
		// call function recursivly for all childs
		// currently only necessary for root node, because all 
		// relevant nodes are direct children of the root node.
		if (node == parser.getRootNode()) {
			for (Node child: node.getChildNodes())
				parseSyntaxTree(child, result);
		}
		
		// If we reach this point within the method call for the root node,
		// the recursion is complete.
		// At this point we have to generate the sorted variants of all lists.
		if (node == parser.getRootNode()) 
			for (ListNames listname: ListNames.values()) {
				List<OutlineTreeNode> unsortedList = listsUnsorted.get(listname);
				List<OutlineTreeNode> sortedList = listsAlpha.get(listname);
				createSortedList(unsortedList, sortedList);
			}

	}
	
	/**
	 * Helper method to sort the elements of a given list into another list.
	 * @param unsortedList	the input list
	 * @param sortedList	the output list, containing all elements of the input
	 * 						list, sorted by their natural order. The list is cleared
	 * 						before adding the elements of the unsorted list.
	 */
	private void createSortedList(List<OutlineTreeNode> unsortedList, List<OutlineTreeNode> sortedList)
	{
		sortedList.clear();
		sortedList.addAll(unsortedList);
		Collections.sort(sortedList);
	}
	
	/**
	 * Helper class which stores the lists with the outline nodes
	 * @author Markus M�ller
	 */
	private class ParsingResult
	{
		// This nested map is basically a map which maps two keys to a list
		// of outline nodes. The first key is the sorting order (DisplayModeOrder),
		// the second key is the type of the nodes the list contains.
		// So there are two lists for each node type: a sorted one and an unsorted one.
		Map<DisplayModeOrder, 
			Map<ListNames,
				List<OutlineTreeNode>>> lists;
		
		/**
		 * The constructor generates a new list for each combination of DisplayModeOrder
		 * and NodyType, ignoring those NodeTypes for which no lists are needed.
		 * The lists are put into the nested map under the corresponding keys.
		 */
		ParsingResult() {
			lists = new HashMap<DisplayModeOrder, Map<ListNames, List<OutlineTreeNode>>>();
			for (DisplayModeOrder order: DisplayModeOrder.values()) {
				Map<ListNames, List<OutlineTreeNode>> map = new HashMap<ListNames, List<OutlineTreeNode>>();
				for (ListNames listname: ListNames.values()) {
					List<OutlineTreeNode> list = new LinkedList<OutlineTreeNode>();
					map.put(listname, list);
				}
				lists.put(order, map);
			}
		}
		
	}
	
	
}
