package org.coreasm.eclipse.editors.errors;

/**
 * The ITreeErrorRecognizer interface is a marker interface which marks instances
 * of the IErrorRecognizer interface as error parsers which work on the syntax tree
 * of a parsed CoreASM specification. This means that they can only be run
 * if there was no syntax error during parsing.
 * @author Markus Müller
 */
public interface ITreeErrorRecognizer
extends IErrorRecognizer
{
	
}
