package org.coreasm.eclipse.editors.warnings;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.jface.text.IDocument;

public class ReturnUndefWarning extends AbstractWarning {

	public ReturnUndefWarning(String ruleName, int returnPosition, Node node, ControlAPI capi, IDocument document) {
		super("The rule '" + ruleName + "' will always return undef!", "ReturnUndef " + returnPosition, calculatePosition(node, null, capi, document), ruleName.length());
	}
}
