package org.coreasm.eclipse.editors.warnings;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.jface.text.IDocument;

public class NumberOfArgumentsWarning extends AbstractWarning {

	public NumberOfArgumentsWarning(String functionName, Node node, ControlAPI capi, IDocument document) {
		super("The number of arguments passed to '" + functionName + "' does not match its signature.", "NumberOfAgruments", calculatePosition(node, null, capi, document), functionName.length());
	}
}
