package org.coreasm.eclipse.editors.warnings;

public class ReturnUndefWarning extends AbstractWarning {

	public ReturnUndefWarning(String ruleName, int rulePosition, int returnPosition) {
		super("The rule '" + ruleName + "' will always return undef!", "ReturnUndef " + returnPosition, rulePosition, ruleName.length());
	}
}
