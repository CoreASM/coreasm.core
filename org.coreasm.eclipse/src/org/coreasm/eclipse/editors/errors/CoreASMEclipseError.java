package org.coreasm.eclipse.editors.errors;

import java.util.Map;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.Specification;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.MacroCallRuleNode;
import org.coreasm.engine.parser.CharacterPosition;
import org.coreasm.engine.parser.Parser;
import org.eclipse.jface.text.BadLocationException;
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
		set(AbstractError.POSITION, calculatePosition(error, capi, document));
		set(AbstractError.LENGTH, calculateLength(error.node, document));
	}
	
	protected CoreASMEclipseError(Map<String, String> attributes)
	{
		super(attributes);
	}

	private static int calculatePosition(CoreASMError error, ControlAPI capi, IDocument document) {
		CharacterPosition charPos = error.pos;
		if (capi != null) {
			Parser parser = capi.getParser();
			Node node = error.node;
			if (charPos == null && node != null && node.getScannerInfo() != null)
				charPos = node.getScannerInfo().getPos(parser.getPositionMap());
			if (charPos != null) {
				Specification spec = capi.getSpec();
				try {
					int line = charPos.line;
					if (spec != null)
						line = spec.getLine(charPos.line).line;
					return document.getLineOffset(line - 1) + charPos.column - 1;
				} catch (BadLocationException e) {
				}
			}
		}
		Node node = error.node;
		if (node != null)
			return node.getScannerInfo().charPosition;
		return 0;
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
