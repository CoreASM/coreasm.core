package org.coreasm.eclipse.editors.warnings;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.Node;

public class DanglingElseWarning extends AbstractWarning {

	public DanglingElseWarning(Node node, ASMDocument document) {
		super("Dangling else", "DanglingElse " + document.getNodePosition(node.getParent()), node, document);
	}
}
