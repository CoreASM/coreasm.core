package org.coreasm.eclipse.editors.warnings;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.Node;

public class NoReturnWarning extends AbstractWarning {

	public NoReturnWarning(String ruleName, Node node, ASMDocument document) {
		super("The rule '" + ruleName + "' does not return anything!", "NoReturn", document.getNodePosition(node), ruleName.length());
	}
}
