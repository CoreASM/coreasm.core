package org.coreasm.eclipse.editors.warnings;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.CoreASMWarning;
import org.eclipse.jface.text.IDocument;

/**
 * This class represents warnings from CoreASM.
 * @author Michael Stegmaier
 *
 */
public class CoreASMEclipseWarning extends AbstractWarning {

	public CoreASMEclipseWarning(CoreASMWarning warning, IDocument document) {
		super("CoreASM Warning: " + warning.showWarning(null, null), "CoreASMWarning", ((ASMDocument)document).getCharPosition(warning.getPos(), warning.getSpec()), ASMDocument.calculateLength(warning.node));
	}
}
