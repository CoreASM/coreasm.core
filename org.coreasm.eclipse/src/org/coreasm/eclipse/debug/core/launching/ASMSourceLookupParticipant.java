package org.coreasm.eclipse.debug.core.launching;

import org.coreasm.eclipse.debug.core.model.ASMStackFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;

/**
 * Source lookup participant for ASM source files
 * @author Michael Stegmaier
 *
 */
public class ASMSourceLookupParticipant extends AbstractSourceLookupParticipant {

	@Override
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof ASMStackFrame)
			return ((ASMStackFrame)object).getSourceName();
		
		return null;
	}
}
