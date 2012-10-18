package org.coreasm.eclipse.editors.errors;

import java.util.List;

import org.coreasm.eclipse.editors.ASMDocument;

/**
 * An object implementing IErrorRecognizer is an object which checks an ASMDocument 
 * for a certain kind of errors. 
 */
public interface IErrorRecognizer
{
	/**
	 * Checks the given document for errors. The errors which have been found are
	 * returned as a list of AbstractError objects.
	 * 
	 * @param document the document which will be checked for errors
	 * @param errors a list which to which all found errors will be added
	 */
	public void checkForErrors(ASMDocument document, List<AbstractError> errors);

}
