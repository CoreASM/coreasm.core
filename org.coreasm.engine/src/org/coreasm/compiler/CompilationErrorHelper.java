package org.coreasm.compiler;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.CharacterPosition;

/**
 * Maps nodes to specification positions
 * @author Spellmaker
 *
 */
public class CompilationErrorHelper {
	/**
	 * Finds out how many characters a node spans
	 * @param node The node in the parse tree
	 * @param capi The control api used for the operation
	 * @return The length of the node as an int
	 */
	public static int getNodeLength(ASTNode node, ControlAPI capi){
		//find position of the right-most node in the subtree
		ASTNode lastChild = node;
		while(lastChild.getFirst() != null){
			lastChild = getLast(lastChild);
		}
		
		return lastChild.getScannerInfo().charPosition + 
				(lastChild.getToken() != null ? lastChild.getToken().length() : 0) - 
				node.getScannerInfo().charPosition;
	}
	
	/**
	 * Finds the character position of a node
	 * @param node A node in the parse tree
	 * @param engine The CoreASMEngine used for the operation
	 * @return The nodes character position
	 */
	public static CharacterPosition getNodePos(ASTNode node, CoreASMEngine engine){
		return ((Node) node).getCharPos(((ControlAPI) engine).getParser());
	}
	
	private static ASTNode getLast(ASTNode node){
		return node.getAbstractChildNodes().get(node.getAbstractChildNodes().size() - 1);
	}
	
	/**
	 * Creates an error message including the position of the error in the specification
	 * @param node The node causing the error
	 * @param capi The ControlAPI used for the operation
	 * @param message The base error message
	 * @param pluginName The name of the plugin which caused the error
	 * @return The error string, containing more information about the error position
	 */
	public static String makeErrorMessage(ASTNode node, ControlAPI capi, String message, String pluginName){
		StringBuilder error = new StringBuilder();
		CharacterPosition pos = getNodePos(node, capi);
		int len = getNodeLength(node, capi);
		error.append("compilation error at pos [").append(pos.getLineNumber()).
		append(":").append(pos.getColumnNumber()).append(",").append(len).append("], caused by plugin '").append(pluginName).
		append("': ").append(message);
		
		return error.toString();
	}
}
