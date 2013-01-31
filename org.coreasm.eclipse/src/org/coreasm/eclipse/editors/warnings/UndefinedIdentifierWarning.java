package org.coreasm.eclipse.editors.warnings;

public class UndefinedIdentifierWarning extends AbstractWarning {

	public UndefinedIdentifierWarning(String identifier, int position) {
		super("Undefined identifier encountered: " + identifier, position, identifier.length());
	}
}
