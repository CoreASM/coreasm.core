package org.coreasm.eclipse.editors.warnings;

import java.util.List;

import org.coreasm.eclipse.editors.ASMDocument;

/**
 * This interface should be implemented by warning recognizers.
 * @author Michael Stegmaier
 *
 */
public interface IWarningRecognizer {
	/**
	 * Checks a document for warnings.
	 * @param document document to check for warnings
	 * @return list of warnings that have been found
	 */
	public List<AbstractWarning> checkForWarnings(ASMDocument document);
}
