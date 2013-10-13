package org.coreasm.eclipse.editors.warnings;

import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.jface.text.IDocument;

/**
 * This warning indicates an undefined identifier.
 * @author Michael Stegmaier
 *
 */
public class UndefinedIdentifierWarning extends AbstractWarning {

	public UndefinedIdentifierWarning(String identifier, List<ASTNode> arguments, Node node, ControlAPI capi, IDocument document) {
		super("Undefined identifier encountered: " + identifier, "UndefinedIdentifier " + identifier + " " + arguments.size(), calculatePosition(node, null, capi, document), identifier.length());
	}
}
