package org.coreasm.eclipse.editors.warnings;

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
		super("CoreASM Warning: " + warning.showWarning(null, null), "CoreASMWarning", ((ASMDocument)document).getCharPosition(warning.getPos(), warning.getSpec()), calculateLength(warning.node));
	}

	public static int calculateLength(Node problemNode) {
		if (problemNode != null) {
			Node node = problemNode;
			while (node.getFirstCSTNode() != null) {
				node = node.getFirstCSTNode();
				while (node.getNextCSTNode() != null)
					node = node.getNextCSTNode();
			}
			int len = 0;
			if (node.getToken() != null)
				len = node.getToken().length();
			return node.getScannerInfo().charPosition + len - problemNode.getScannerInfo().charPosition;
		}
		return 0;
	}
}
