package org.coreasm.eclipse.editors.errors;

import java.util.Map;

import org.coreasm.eclipse.editors.warnings.AbstractWarning;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.MacroCallRuleNode;
import org.eclipse.jface.text.IDocument;

/**
 * This class represents errors from CoreASM.
 * @author Michael Stegmaier
 *
 */
public class CoreASMEclipseError extends AbstractError {
	
	public CoreASMEclipseError(CoreASMError error, ControlAPI capi, IDocument document) {
		super(ErrorType.COREASM_ERROR);
		set(AbstractError.DESCRIPTION, "CoreASM Error: " + error.message);
		set(AbstractError.POSITION, AbstractWarning.calculatePosition(error.node, error.pos, capi, document));
		set(AbstractError.LENGTH, calculateLength(error.node, document));
	}
	
	protected CoreASMEclipseError(Map<String, String> attributes)
	{
		super(attributes);
	}

	private static int calculateLength(Node node, IDocument document) {
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
