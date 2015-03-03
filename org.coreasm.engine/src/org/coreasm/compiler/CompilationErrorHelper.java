package org.coreasm.compiler;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.CharacterPosition;

public class CompilationErrorHelper {
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
	
	public static CharacterPosition getNodePos(ASTNode node, CoreASMEngine engine){
		return ((Node) node).getCharPos(((ControlAPI) engine).getParser());
	}
	
	private static ASTNode getLast(ASTNode node){
		return node.getAbstractChildNodes().get(node.getAbstractChildNodes().size() - 1);
	}
	
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
