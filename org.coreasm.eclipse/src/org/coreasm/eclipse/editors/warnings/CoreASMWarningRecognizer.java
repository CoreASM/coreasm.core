package org.coreasm.eclipse.editors.warnings;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.engine.CoreASMWarning;

/**
 * The <code>CoreASMWarningRecognizer</code> collects warnings from the CoreASM Engine
 * @author Michael Stegmaier
 *
 */
public class CoreASMWarningRecognizer implements IWarningRecognizer {
	private final ASMEditor parentEditor;
	
	public CoreASMWarningRecognizer(ASMEditor parentEditor) {
		this.parentEditor = parentEditor;
	}
	
	@Override
	public List<AbstractWarning> checkForWarnings(ASMDocument document) {
		List<AbstractWarning> warnings = new ArrayList<AbstractWarning>();
		for (CoreASMWarning warning : parentEditor.getParser().getSlimEngine().getWarnings())
			warnings.add(new CoreASMEclipseWarning(warning, document));
		return warnings;
	}

}
