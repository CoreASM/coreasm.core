package org.coreasm.eclipse.editors.errors;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.jparsec.error.ParseErrorDetails;
import org.codehaus.jparsec.error.ParserException;
import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.ASMParser.ParsingResult;
import org.coreasm.eclipse.editors.warnings.AbstractWarning;
import org.coreasm.eclipse.editors.warnings.CoreASMWarningRecognizer;
import org.coreasm.eclipse.editors.warnings.IWarningRecognizer;
import org.coreasm.eclipse.editors.warnings.NumberOfArgumentsWarningRecognizer;
import org.coreasm.eclipse.editors.warnings.UndefinedIdentifierWarningRecognizer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;

/**
 * An ErrorManager checks a CoreASM specification for errors beyond
 * syntax errors. Each instance of ASMEditor creates an ErrorManager
 * and binds it to its ASMParser as an Observer. So the ErrorManager
 * gets notified each time the parser was run.
 * 
 * An ErrorManager manages a list of objects implementing the IErrorRecognizer
 * interface. These objects are doing the actual error checking, each error
 * recognizer searches for a certain kind of errors. The ErrorManager executes
 * these ErrorRegognizers after each run of the parser.
 * 
 * @author Markus Müller
 */
public class ErrorManager implements Observer
{
	private ASMEditor asmEditor;	// the editor this instance belongs to
	private List<ITextErrorRecognizer> listTextParsers;
	private List<ITreeErrorRecognizer> listTreeParsers;
	private List<IWarningRecognizer> warningRecognizers;
	
	/**
	 * Generates a new ErrorManager and adds all available ErrorRecognizers to itself.
	 * @param asmEditor	The ASMEditor instance the generated ErrorManager belongs to.
	 */
	public ErrorManager(ASMEditor asmEditor)
	{
		this.asmEditor = asmEditor;
		this.listTextParsers = new LinkedList<ITextErrorRecognizer>();
		this.listTreeParsers = new LinkedList<ITreeErrorRecognizer>();
		this.warningRecognizers = new LinkedList<IWarningRecognizer>();

		// Creating and adding all available ErrorRecognizers.
		addErrorRecognizer(new InitErrorRecognizer());
		addErrorRecognizer(new RuleErrorRecognizer(asmEditor));
		addErrorRecognizer(new PluginErrorRecognizer(asmEditor.getParser()));
		addErrorRecognizer(new ModularityErrorRecognizer(asmEditor));
		addErrorRecognizer(new CoreASMErrorRecognizer(asmEditor));
		addWarningRecognizer(new UndefinedIdentifierWarningRecognizer(asmEditor));
		addWarningRecognizer(new NumberOfArgumentsWarningRecognizer(asmEditor));
		addWarningRecognizer(new CoreASMWarningRecognizer(asmEditor));
	}
	
	/**
	 * Adds an ErrorRegognizer to this ErrorManager, so the ErrorRecognizer will
	 * be run after each run of the parser.
	 */
	public void addErrorRecognizer(IErrorRecognizer errorRecognizer)
	{
		if (errorRecognizer instanceof ITextErrorRecognizer)
			listTextParsers.add((ITextErrorRecognizer) errorRecognizer);
		
		if (errorRecognizer instanceof ITreeErrorRecognizer)
			listTreeParsers.add((ITreeErrorRecognizer) errorRecognizer);
	}
	
	/**
	 * Adds a WarningRecognizer to this ErrorManager.
	 * @param warningRecognizer
	 */
	public void addWarningRecognizer(IWarningRecognizer warningRecognizer) {
		warningRecognizers.add(warningRecognizer);
	}
	
	/**
	 * Executes all ErrorRecognizers implementing the ITextErrorRecognizer interface and
	 * collects the errors which were found in a list.
	 * @param document	The document which is to be checked.
	 * @return			A list with all errors which have been found.
	 * @see				org.coreasm.eclipse.editors.errors.ITextErrorRecognizer
	 */
	public List<AbstractError> checkAllTextErrorRecognizers(ASMDocument document)
	{
		List<AbstractError> errors = new LinkedList<AbstractError>();
		for (ITextErrorRecognizer errorParser: listTextParsers)
			errorParser.checkForErrors(document, errors);
		return errors;
	}
	
	/**
	 * Executes all ErrorRecognizers implementing the ITreeErrorRecognizer interface and
	 * collects the errors which were found in a list.
	 * @param document	The document which is to be checked.
	 * @return			A list with all errors which have been found.
	 * @see				org.coreasm.eclipse.editors.errors.ITreeErrorRecognizer
	 */
	public List<AbstractError> checkAllTreeErrorRecognizers(ASMDocument document)
	{
		List<AbstractError> errors = new LinkedList<AbstractError>();
		for (ITreeErrorRecognizer errorParser: listTreeParsers)
			errorParser.checkForErrors(document, errors);
		return errors;
	}
	
	/**
	 * Executes all ErrorRecognizers (both TextErrorRecognizers and TreeErrorRecognizers)
	 * interface and collects the errors which were found in a list.
	 * @param document	The document which is to be checked.
	 * @return			A list with all errors which have been found.
	 * @see				org.coreasm.eclipse.editors.errors.ITextErrorRecognizer
	 * @see				org.coreasm.eclipse.editors.errors.ITreeErrorRecognizer
	 */	
	public List<AbstractError> checkAllErrorRecognizer(ASMDocument document) 
	{
		List<AbstractError> errors = new LinkedList<AbstractError>();
		errors.addAll(checkAllTextErrorRecognizers(document));
		errors.addAll(checkAllTreeErrorRecognizers(document));
		return errors;
	}
	
	/**
	 * Execute all WarningRecognizers.
	 * @param document document to be checked
	 * @return list of warnings that have been found
	 */
	public List<AbstractWarning> checkAllWarnings(ASMDocument document) {
		List<AbstractWarning> warnings = new LinkedList<AbstractWarning>();
		for (IWarningRecognizer warningRecognizer : warningRecognizers)
			warnings.addAll(warningRecognizer.checkForWarnings(document));
		return warnings;
	}

	/**
	 * This is the method of the Observer interface which is called after each
	 * run of the parser. It executes the ErrorRecognizers depending if the parsing
	 * was successful and creates an error marker for each error. It also creates
	 * a marker if the parser delivered a syntax error or an unknown error.
	 * @param o		The observable which has called this method. This must be
	 * 				the parser instance which is bound to the same instance of 
	 * 				ASMEditor than this ErrorManager.
	 * @param arg	The data the Observable delivered. This must be an instance
	 * 				of ParsingResult.
	 */
	@Override
	public void update(Observable o, Object arg)
	{
		// check if correctly called by the right parser
		if (o != asmEditor.getParser() ||
				! (arg instanceof ParsingResult))
			return;
		ParsingResult result = (ParsingResult) arg;
		List<AbstractError> errors = new LinkedList<AbstractError>();
		
		// clear old markers
		asmEditor.removeMarkers(IMarker.PROBLEM);
		
		// always run TextErrorRecognizers
		errors.addAll(checkAllTextErrorRecognizers(result.document));
		
		// run TreeErrorRecognizers only if there was no syntax error
		if (result.wasSuccessful == true)
			errors.addAll(checkAllTreeErrorRecognizers(result.document));
		
		// create markers for all errors
		for (AbstractError error: errors) {
			if (error instanceof SimpleError)
				asmEditor.createSimpleMark((SimpleError) error, IMarker.SEVERITY_ERROR);
			else
				asmEditor.createErrorMark(error);
		}
		
		if (result.wasSuccessful) {
			for (AbstractWarning warning : checkAllWarnings(result.document))
				asmEditor.createWarningMark(warning);
		}

		// if there was a syntax error: create error object
		if (result.exception != null) {
			ParserException pe = result.exception;
			ParseErrorDetails perr = pe.getErrorDetails();
			
			if (perr != null) {
				// SYNTAX ERROR
				int line = asmEditor.getSpec().getLine(pe.getLocation().line).line;
				int col = pe.getLocation().column;
				int index = 0;
				try {
					if (line > 0)
						index = result.document.getLineOffset(line-1) + col-1;
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				String message = pe.getMessage();
				int beginIndex = message.indexOf(':');
				if (beginIndex > 0)
					message = "Syntax Error: " + message.substring(beginIndex + 1).trim();
				String encountered = perr.getEncountered().trim();
				int length = getErrorLength(encountered, result.document.get(), index);
				// build expected string
				List<String> lstExpected = perr.getExpected();
				deleteDuplicatesAndSortList(lstExpected);
				// create error object
				SyntaxError serror = new SyntaxError(message, line, col, index, length, lstExpected, encountered);
				asmEditor.createSyntaxMark(serror, IMarker.SEVERITY_ERROR);
			}
			else
			{	// OTHER ERROR
				String message = pe.getMessage();
				int line = pe.getLocation().line;
				int col = pe.getLocation().column;
				// create error object
				UndefinedError error = new UndefinedError(message, line, col);
				asmEditor.createUndefinedMark(error, IMarker.SEVERITY_ERROR);
			}				
			
		}
		
	}
	
	
	
	// ==============================
	// Helper methods for update(...)
	// ==============================
	
	private int getErrorLength(String token, String strDoc, int index)
	{
		if (token.equals("EOF"))
			return 0;
		if (strDoc.charAt(index)=='"' && strDoc.startsWith(token, index+1))
			return token.length()+2;
		return token.length();
	}
	
	private void deleteDuplicatesAndSortList(List<String> list)
	{
		SortedSet<String> setEntries = new TreeSet<String>();
		for (String entry: list)
			if ( ! setEntries.contains(entry) )
				setEntries.add(entry);
		list.clear();
		list.addAll(setEntries);
	}
	
}
