package org.coreasm.eclipse.editors.warnings;

import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.CoreASMWarning;
import org.coreasm.engine.interpreter.Node;
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

	public static int calculateLength(Node problemNode) {
		if (problemNode != null) {
			int start = problemNode.getScannerInfo().charPosition;
			int end = start;
			
			Stack<Node> fringe = new Stack<Node>();
			fringe.add(problemNode);
			while (!fringe.isEmpty()) {
				Node node = fringe.pop();
				int len = 0;
				if (node.getToken() != null)
					len = node.getToken().length();
				if (node.getScannerInfo().charPosition + len > end)
					end = node.getScannerInfo().charPosition + len;
				for (Node child : node.getChildNodes())
					fringe.add(child);
			}
			return end - start;
		}
		return 0;
	}
}
