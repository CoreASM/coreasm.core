package org.coreasm.eclipse.editors.warnings;

import java.util.List;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.Node;

public class NumberOfArgumentsWarning extends AbstractWarning {

	public NumberOfArgumentsWarning(String functionName, int numberOfArguments, List<String> params, Node node, ASMDocument document) {
		super("The number of arguments passed to '" + functionName + "' does not match its signature.", "NumberOfAgruments " + functionName + " " + numberOfArguments + " " + listToString(params), document.getNodePosition(node), ASMDocument.calculateLength(node));
	}
	
	private static String listToString(List<String> list) {
		String result = "";
		for (String element : list) {
			if (!result.isEmpty())
				result += " ";
			result += element;
		}
		return result;
	}
}
