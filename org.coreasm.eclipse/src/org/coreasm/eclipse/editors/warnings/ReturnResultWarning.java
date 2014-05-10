package org.coreasm.eclipse.editors.warnings;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.Node;

public class ReturnResultWarning extends AbstractWarning {

	public ReturnResultWarning(String ruleName, Node node, ASMDocument document) {
		super("The result of '" + ruleName + "' will always be undef!", "ReturnResult", document.getNodePosition(node.getFirstCSTNode().getNextCSTNode()), 2);
	}
}
