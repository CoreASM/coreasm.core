package org.coreasm.eclipse.editors.warnings;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.Node;

public class ReturnUndefWarning extends AbstractWarning {

	public ReturnUndefWarning(String ruleName, int returnPosition, Node node, ASMDocument document) {
		super("The rule '" + ruleName + "' will always return undef!", "ReturnUndef " + returnPosition, document.getNodePosition(node), ruleName.length());
	}
}
