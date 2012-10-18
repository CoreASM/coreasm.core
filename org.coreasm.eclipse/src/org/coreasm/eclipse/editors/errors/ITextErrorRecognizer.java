package org.coreasm.eclipse.editors.errors;

/**
 * The ITextErrorRecognizer interface is a marker interface which marks instances
 * of the IErrorRecognizers interface as error recognizers which work directly on the
 * source of the CoreASM specification. This means that they can also be
 * run if there was a syntax error during parsing.
 * @author Markus Müller
 */
public interface ITextErrorRecognizer
extends IErrorRecognizer
{

}
