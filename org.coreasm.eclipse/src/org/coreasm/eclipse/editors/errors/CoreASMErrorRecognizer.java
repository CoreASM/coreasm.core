package org.coreasm.eclipse.editors.errors;

import java.util.List;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.SlimEngine;
import org.coreasm.engine.CoreASMError;

/**
 * The <code>CoreASMErrorRecognizer</code> collects errors from the CoreASM Engine
 * @author Michael Stegmaier
 *
 */
public class CoreASMErrorRecognizer implements ITextErrorRecognizer {
	private final ASMEditor parentEditor;
	
	public CoreASMErrorRecognizer(ASMEditor parentEditor) {
		this.parentEditor = parentEditor;
	}
	
	@Override
	public void checkForErrors(ASMDocument document, List<AbstractError> errors) {
		SlimEngine slimEngine = (SlimEngine)parentEditor.getParser().getSlimEngine();
		for (CoreASMError error : slimEngine.getErrors())
			errors.add(new CoreASMEclipseError(error, document));
	}
}
