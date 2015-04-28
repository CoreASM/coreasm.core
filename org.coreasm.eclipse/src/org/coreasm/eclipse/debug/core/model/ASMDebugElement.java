package org.coreasm.eclipse.debug.core.model;

import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Implementation of common function for ASM debug elements
 * @author Michael Stegmaier
 *
 */
public class ASMDebugElement extends DebugElement {
	
	public ASMDebugElement(IDebugTarget debugTarget) {
		super(debugTarget);
	}

	@Override
	public String getModelIdentifier() {
		return "org.coreasm.eclipse.debug";
	}
}
