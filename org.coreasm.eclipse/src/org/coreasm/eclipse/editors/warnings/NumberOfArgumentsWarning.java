package org.coreasm.eclipse.editors.warnings;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.Node;

public class NumberOfArgumentsWarning extends AbstractWarning {

	public NumberOfArgumentsWarning(String functionName, Node node, ASMDocument document) {
		super("The number of arguments passed to '" + functionName + "' does not match its signature.", "NumberOfAgruments", document.getNodePosition(node), functionName.length());
	}
}
