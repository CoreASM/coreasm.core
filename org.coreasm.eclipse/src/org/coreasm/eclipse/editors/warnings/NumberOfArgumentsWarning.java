package org.coreasm.eclipse.editors.warnings;

public class NumberOfArgumentsWarning extends AbstractWarning {

	public NumberOfArgumentsWarning(String functionName, int position) {
		super("The number of arguments passed to '" + functionName + "' does not match its signature.", "NumberOfAgruments", position, functionName.length());
	}
}
