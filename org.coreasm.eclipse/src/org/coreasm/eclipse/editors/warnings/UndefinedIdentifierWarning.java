package org.coreasm.eclipse.editors.warnings;

import java.util.List;

import org.coreasm.engine.interpreter.ASTNode;

/**
 * This warning indicates an undefined identifier.
 * @author Michael Stegmaier
 *
 */
public class UndefinedIdentifierWarning extends AbstractWarning {

	public UndefinedIdentifierWarning(String identifier, List<ASTNode> arguments, int position) {
		super("Undefined identifier encountered: " + identifier, "UndefinedIdentifier " + identifier + " " + arguments.size(), position, identifier.length());
	}
}
