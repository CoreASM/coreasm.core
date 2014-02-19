package org.coreasm.eclipse.editors.warnings;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.Node;

public class NumberOfArgumentsWarning extends AbstractWarning {

	public NumberOfArgumentsWarning(String functionName, int numberOfArguments, Node node, ASMDocument document) {
		super("The number of arguments passed to '" + functionName + "' does not match its signature.", "NumberOfAgruments " + functionName + " " + numberOfArguments, document.getNodePosition(node), functionName.length());
	}
}
