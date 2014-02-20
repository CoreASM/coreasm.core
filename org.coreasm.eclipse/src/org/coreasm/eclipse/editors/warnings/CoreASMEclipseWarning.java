package org.coreasm.eclipse.editors.warnings;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.CoreASMWarning;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.MacroCallRuleNode;
import org.eclipse.jface.text.IDocument;

/**
 * This class represents warnings from CoreASM.
 * @author Michael Stegmaier
 *
 */
public class CoreASMEclipseWarning extends AbstractWarning {

	public CoreASMEclipseWarning(CoreASMWarning warning, IDocument document) {
		super("CoreASM Warning: " + warning.showWarning(null, null), "CoreASMWarning", ((ASMDocument)document).getNodePosition(warning.node, warning.pos), calculateLength(warning.node));
	}

	private static int calculateLength(Node node) {
		if (node instanceof MacroCallRuleNode)
			node = ((MacroCallRuleNode)node).getFirst();
		if (node instanceof FunctionRuleTermNode) {
			FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
			if (frNode.hasName())
				return frNode.getName().length();
		}
		if (node != null) {
			if (node.getToken() != null)
				return node.getToken().length();
			Node lastChild = node.getFirstCSTNode();
			for (Node child = lastChild; child != null; child = child.getNextCSTNode()) {
				if (child.getToken() != null)
					lastChild = child;
			}
			if (lastChild != null && lastChild.getToken() != null)
				return lastChild.getScannerInfo().charPosition - node.getScannerInfo().charPosition + lastChild.getToken().length();
		}
		return 0;
	}
}
