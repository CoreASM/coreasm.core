package org.coreasm.eclipse.editors.errors;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.CharacterPosition;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * This class represents errors from CoreASM.
 * @author Michael Stegmaier
 *
 */
public class CoreASMEclipseError extends AbstractError {

	public CoreASMEclipseError(CoreASMError error, IDocument document) {
		super(ErrorType.COREASM_ERROR);
		set(AbstractError.DESCRIPTION, "CoreASM Error: " + error.message);
		set(AbstractError.POSITION, calculatePosition(error, document));
		set(AbstractError.LENGTH, calculateLength(error.node));
	}

	private static int calculatePosition(CoreASMError error, IDocument document) {
		CharacterPosition charPos = error.pos;
		if (charPos != null) {
			try {
				return document.getLineOffset(charPos.line - 1) + charPos.column - 1;
			} catch (BadLocationException e) {
			}
		}
		Node node = error.node;
		if (node != null)
			return node.getScannerInfo().charPosition;
		return 0;
	}
	
	private static int calculateLength(Node node) {
		if (node != null && node.getToken() != null)
			return node.getToken().length();
		return 0;
	}
}
