package org.coreasm.eclipse.editors.errors;

import java.util.Map;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.warnings.CoreASMEclipseWarning;
import org.coreasm.engine.CoreASMError;

/**
 * This class represents errors from CoreASM.
 * @author Michael Stegmaier
 *
 */
public class CoreASMEclipseError extends AbstractError {
	
	public CoreASMEclipseError(CoreASMError error, ASMDocument document) {
		super(ErrorType.COREASM_ERROR);
		set(AbstractError.DESCRIPTION, "CoreASM Error: " + error.showError(null, null));
		set(AbstractError.POSITION, document.getNodePosition(error.node, error.pos));
		set(AbstractError.LENGTH, CoreASMEclipseWarning.calculateLength(error.node));
	}
	
	protected CoreASMEclipseError(Map<String, String> attributes)
	{
		super(attributes);
	}
}
