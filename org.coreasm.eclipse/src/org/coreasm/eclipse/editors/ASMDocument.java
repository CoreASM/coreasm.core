package org.coreasm.eclipse.editors;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.Specification;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.CharacterPosition;
import org.coreasm.engine.parser.Parser;
import org.coreasm.util.Logger;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

/**
 * The ASMDocument class represents a CoreASM specification as a document of an
 * Eclipse CoreASM editor.
 * 
 * @author Markus Müller, Michael Stegmaier
 */
public class ASMDocument
extends Document
{
	/** References the rootnote of the syntax tree of the specifications, contains
	 * null if the specfication haven't been parsed before or if the last parsing
	 * caused an error. */
	private Node rootnode = null;
	
	/** This list contains all syntax elements of the uppermost level below the
	 * rootnode. The list is remembered if the last parsing caused an error. */
	private List<HeaderElement> headerElements; 
	
	/** This flag stores if a specification is a full specification (beginning
	 * with "CoreASM") or if it is a module (beginning with "CoreModule"). */
	private Boolean isIncluded = null;
	
	private ControlAPI capi;
	
	public ASMDocument()
	{
		super();
		headerElements = new LinkedList<HeaderElement>();
	}

	/**
	 * This method returns if a specification is a module or not.
	 * @return	true if it is a module, false otherwise.
	 */
	public boolean isIncludedSpecification()
	{
		if (isIncluded == null)
			checkIfSpecificationIsIncluded();
		return isIncluded;
	}
	
	/**
	 * This method checks if the CoreASM specifiaction which is represented by the document
	 * is a normal specification (starting with the keyword "CoreASM") or if it is intended
	 * to be included in other specifications (starting with the keyword "CoreModule").
	 * The result of the method is stored in the variable isIncluded.
	 */
	private void checkIfSpecificationIsIncluded()
	{
		String strDoc = get();
		
		// This variable stores if the document has been recognized as a
		// CoreModule specification or not. A Boolean object is used here
		// which is null unless the decision was made.
		Boolean bCoreModule = null;
		
		// Search the document for "CoreModule" words. ( \b: word boundary)
		Pattern pCoreModule = Pattern.compile("\\bCoreModule\\b");
		Matcher mCoreModule = pCoreModule.matcher(strDoc);

		while (mCoreModule.find()) {
			int offset = mCoreModule.start();
			String type = getContent(offset);
			
			// if this "CoreModule" is part of a comment 
			// > continue with next one:
			if ( !type.equals(ASMEditor.PARTITION_CODE) )
				continue;
			
			// look at each character before the "CoreModule":
			// it has to be a whitespace or a comment
			for (int i=0; i<offset; i++) {
				if ( !Character.isWhitespace(strDoc.charAt(i)) && !getContent(i).equals(ASMEditor.PARTITION_COMMENT)) {
					// if not: store result and break out of for loop
					bCoreModule = false;
					break;
				}
			}
			
			// If we reach this point and bCoreModule is still null, we have found a 
			// CoreModule keyword which is only preceded by whitespaces or comments 
			// (so we set bCoreModule to true to store this result).
			// If we reach this point and bCoreModule is false, we have found
			// a CoreModule keyword which is preceded by something else than
			// whitespaces or code.
			// In either case we have a result, and we can break out of the while loop.
			if (bCoreModule == null)
				bCoreModule = true;
			break;			
		}
		
		// If bCoreModule is still null here, there were no CoreModule keywords
		// or only commented ones, so we set bCoreModule to false.
		if (bCoreModule == null)
			bCoreModule = false;
		
		isIncluded = bCoreModule;
	}
	
	/**
	 * This method causes the document to "forget" if it is a "CoreASM" or a "CoreModule" specification,
	 * so this will be rechecked the next time the method isIncludedSpecification() is called.
	 */
	public void resetInclusionState()
	{
		isIncluded = null;
	}
	
	public Node getRootnode() 
	{
		return rootnode;
	}
	
	/**
	 * Returns the position of the specified Node or CharacterPosition in this document
	 * @param node Node to return position of
	 * @return
	 */
	public int getNodePosition(Node node) {
		return getNodePosition(node, null);
	}
	
	/**
	 * Returns the position of the specified Node or CharacterPosition in this document
	 * @param node Node to return position of
	 * @param charPos CharacterPosition to return position of
	 * @return
	 */
	public int getNodePosition(Node node, CharacterPosition charPos) {
		if (capi != null) {
			Parser parser = capi.getParser();
			if (charPos == null && node != null && node.getScannerInfo() != null)
				charPos = node.getScannerInfo().getPos(parser.getPositionMap());
			if (charPos != null) {
				Specification spec = capi.getSpec();
				try {
					int line = charPos.line;
					if (spec != null)
						line = spec.getLine(charPos.line).line;
					return getLineOffset(line - 1) + charPos.column - 1;
				} catch (BadLocationException e) {
				}
			}
		}
		if (node != null)
			return node.getScannerInfo().charPosition;
		return 0;
	}
	
	/**
	 * Returns a list of nodes in the specified line
	 * @param line Line to return nodes of
	 * @return List of nodes in the specified line
	 */
	public List<ASTNode> getASTNodesOnLine(int line) {
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		List<ASTNode> nodes = new LinkedList<ASTNode>();
		ASTNode rootNode = (ASTNode)getRootnode();
		
		if (rootNode != null)
			fringe.add(rootNode);
		while (!fringe.isEmpty()) {
			ASTNode node = fringe.pop();
			try {
				if (getLineOfOffset(getNodePosition(node)) == line)
					nodes.add(node);
			} catch (BadLocationException e) {
			}
			for (ASTNode child : node.getAbstractChildNodes())
				fringe.add(fringe.size(), child);
		}
		return nodes;
	}
	
	/**
	 * Returns a list of nodes in the line of the specified offset
	 * @param offset Offset in the line of which the nodes should be returned
	 * @return List of nodes in the line of the specified offset
	 */
	public List<ASTNode> getASTNodesOnLineOfOffset(int offset) throws BadLocationException {
		return getASTNodesOnLine(getLineOfOffset(offset));
	}
	
	/**
	 * Returns the line number of the specified node
	 * @param node Node to return line number of
	 * @return the line number of the specified node
	 * @throws BadLocationException
	 */
	public int getLineOfNode(ASTNode node) throws BadLocationException {
		return getLineOfOffset(getNodePosition(node));
	}
	
	public void setControlAPI(ControlAPI capi) {
		this.capi = capi;
	}

	/**
	 * Sets the rootnode of a new syntax tree for the document. If the rootnode
	 * is not null, it also rebuilds the header element list from the new syntax tree.
	 * This method is intended to be called by the parser after parsing.
	 * @param rootnode	The rootnode of the new syntax tree.
	 */
	void setRootnode(Node rootnode) 
	{
		this.rootnode = rootnode;
		if (rootnode != null) {
			// Rebuilding the list of header elements
			headerElements.clear();
			List<Node> childnodes = rootnode.getChildNodes();
			// skip first two childnodes -> 1.: keyword 'CoreASM' - 2.: specification ID 
			for (int i=2; i<childnodes.size(); i++) {
				Node child = childnodes.get(i);
				String token = null;
				try {
					token = child.getFirstCSTNode().getToken();
				} catch (NullPointerException e) {
					; // do nothing
				}
				if (token==null) continue;  // ignore this node if it has no token
				
				int offset = 0;
				try {
					offset = getNodePosition(child);
				} catch (NullPointerException e) {
					Logger.log(Logger.WARNING, Logger.ui, "syntax node without a ScannerInfo: " + child.toString());
				}
				headerElements.add(new HeaderElement(offset, token));
			}
		}
	}
	
	/**
	 * Updates the position of header elements after the document has been edited
	 * without an successful parsing, so the document knows the updated position
	 * of all header elements even without a new parsing.
	 * @param offset	Where did the edit occur?
	 * @param delta		How many characters have been inserted (>0) or deleted (<0)
	 */
	void updateHeaders(int offset, int delta)
	{
		for (HeaderElement el: headerElements) {
			if (el.offset >= offset) {
				el.offset += delta;
			}
		}
	}
	
	/**
	 * Returns the current offset of a specific header element, specified by its
	 * original offset at the last successful parsing.
	 * @param oldOffset	The original offset of a header element from the last 
	 * 					successful parsing.
	 * @return The current offset of this header element, or oldOffset if there
	 * 			was no header element with that offset.
	 */
	public int getUpdatedOffset(int oldOffset)
	{
		for (HeaderElement he: headerElements)
			if (he.oldOffset == oldOffset)
				return he.offset;
		return oldOffset;
	}
	
	/**
	 * Returns the content type of the partition at a certain offset
	 * @param offset	The character for which the content type shouls be returned
	 * @return	The tag of the content type, or "" if there was an exception.
	 */
	private String getContent(int offset)
	{
		String s = "";
		try {
			s = getContentType(offset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	
	/**
	 * This class represents an header element of the syntax tree. It tracks the
	 * changes of its offset which occur when the document gets edited without
	 * a successful reparsing.
	 * @author Markus
	 *
	 */
	private class HeaderElement
	{
		int offset;		// the current offset of the element.
		int oldOffset;	// the original offset of the element from the last parsing 
		//String token;	// the token of the element node.
		
		HeaderElement(int offset, String token) {
			super();
			this.offset = offset;
			this.oldOffset = offset;
			//this.token = token;
		}
	}
}
