package org.coreasm.eclipse.editors;

import java.util.LinkedList;
import java.util.List;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;

/**
 * This class is a helper class providing static methods and constants for
 * navigating through the syntax tree.
 * @author Markus Müller
 */
public class AstTools
{
	// constants for grammar rules
	public static final String GRAMMAR_ID = "ID";
	public static final String GRAMMAR_INIT = "Initialization";
	public static final String GRAMMAR_RULE = "RuleDeclaration";
	
	// constants for parser names
	public static final String PARSER_USE = "UseClause";
	public static final String PARSER_HEADER = "Header";
	public static final String PARSER_RULE = GRAMMAR_RULE;
	
	// constants for plugin names
	public static final String PLUGIN_KERNEL = "Kernel";

	/**
	 * Search recursively through the syntax tree until an ID node is found (pre-order)
	 * @param node	The node where the search is started (the root of the recursion)
	 * @return	The ID of the first ID node being found.
	 */
	public static String findId(Node node) {
		Node idNode = findIdNode(node);
		if ( idNode != null )
			return idNode.getToken();
		else return null;
	}
	
	/**
	 * Search recursively through the syntax tree until an ID node is found (pre-order)
	 * @param node	The node where the search is started (the root of the recursion)
	 * @return	The first ID node being found.
	 */
	public static Node findIdNode(Node node) {
		if (node instanceof ASTNode) {
			ASTNode astNode = (ASTNode) node;
			if (astNode.getGrammarRule().equals(GRAMMAR_ID))
				return astNode;
		}
		
		List<Node> children = node.getChildNodes();
		for (Node child: children) {
			Node idNode = findIdNode(child);
			if (idNode != null)
				return idNode;
		}
		
		return null;
	}

	/**
	 * Returns a list with all direct child nodes of a certain node which are
	 * set to a specific grammar rule.
	 */
	public static List<ASTNode> findChildNodes(ASTNode root, String grammarRule)
	{
		List<ASTNode> nodeList = new LinkedList<ASTNode>();
		
		List<ASTNode> children = root.getAbstractChildNodes();
		for (ASTNode child: children)
			if (child.getGrammarRule().equals(grammarRule))
				nodeList.add(child);
		
		return nodeList;
	}
	
	/**
	 * Returns the first direct child node of a certain node which is set to
	 * a specific grammar rule, or null if there is no such node.
	 */
	public static ASTNode findFirstChildNode(ASTNode root, String grammarRule) 
	{
		List<ASTNode> children = root.getAbstractChildNodes();
		for (ASTNode child: children)
			if (child.getGrammarRule().equals(grammarRule))
				return child;
		
		return null;
	}
	
	/**
	 * Returns a list with all direct child nodes of a certain node which
	 * contain a specific grammar rule.
	 */
	public static List<Node> findChildrenWithToken(ASTNode root, String token)
	{
		List<Node> returnlist = new LinkedList<Node>();
		List<Node> children = root.getChildNodes();
		for (Node child: children) {
			String childtoken = child.getToken();
			if (childtoken != null && childtoken.equals(token))
				returnlist.add(child);
		}
		return returnlist;
	}
}
