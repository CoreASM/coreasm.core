package org.coreasm.eclipse.debug.core.launching;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * Source lookup director for ASM source files
 * @author Michael Stegmaier
 *
 */
public class ASMSourceLookupDirector extends AbstractSourceLookupDirector {

	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] { new ASMSourceLookupParticipant() });
	}
}
