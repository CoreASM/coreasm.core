package org.coreasm.eclipse.editors.errors;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.eclipse.editors.ASMEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Abstract class for modeling errors within CoreASM specifications. It stores
 * the attributes of an error as key/value pairs. The class also offers the
 * serialization of the attributes into a String, which is then used as an
 * attribute for an error marker. Afterwards the String can be deserialized
 * back into an instance of this class.
 * 
 * This class does not manage what attributes are available, this is the task
 * of its subclasses.
 * @author Markus Müller
 */
public abstract class AbstractError 
{
	// These two Strings are uses to separate keys and their values respectively
	// different key/value pairs within the serialization string. We use exotic
	// Unicode chars here, so there is no need for escaping.
	public static final String SEPERATOR_ATTR = new StringBuilder().append('\u25c9').toString();
	public static final String SEPERATOR_VAL = new StringBuilder().append('\u25b7').toString();
	
	public static enum ErrorType {
		UNDEFINED, SIMPLE, SYNTAX_ERROR, COREASM_ERROR 
	}
	
	// ATTRIBUTE NAMES:
	// general attributes:
	public static final String TYPE = "type";
	public static final String HEADER = "header";
	public static final String DESCRIPTION = "descr";
	public static final String POSITION = "pos";
	public static final String LINE = "line";
	public static final String COLUMN = "col";
	public static final String LENGTH = "len";
	// simple error attributes:
	public static final String CLASSNAME = "class";
	public static final String ERROR_ID = "errid";
	// syntax error attributes:
	public static final String ENCOUNTERED = "encountered";
	public static final String EXPECTED = "expected";
	
	private Map<String, String> attributes;
	private IDocument document;
	
	/**
	 * Generates a new instance of the given type with an empty attributes map.
	 */
	public AbstractError(ErrorType type)
	{
		attributes = new HashMap<String, String>();
		attributes.put(TYPE, type.name());
		document = null;
	}
	
	/**
	 * Generates a new instance of the given type and initializes the attributs
	 * map with the given map.
	 */
	protected AbstractError(Map<String,String> attributes)
	{
		this.attributes = new HashMap<String, String>(attributes);
	}
	
	/**
	 * Returns the type of this error.
	 */
	public ErrorType getErrorType()
	{
		String sType = attributes.get(TYPE);
		return ErrorType.valueOf(sType);
	}
	
	/**
	 * Returns the value of the attribute with the given key.
	 */
	public String get(String attribute)
	{
		return attributes.get(attribute);
	}
	
	/**
	 * Returns the value of the attribute with the given key as an integer.
	 * @param attribute
	 * @param def	The default value, which is used if the value cannot be converted into an integer.
	 * @return
	 */
	public int getInt(String attribute, int def)
	{
		String s = attributes.get(attribute);
		int i;
		try {
			i = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			i = def;
		}
		return i;
	}
	
	/**
	 * Returns the value of the POSITION attribute.
	 */
	public int getPosition()
	{
		if (attributes.containsKey(POSITION))
			return getInt(POSITION, 0);
		return 0;
		
	}
	
	/**
	 * Returns the value of the LENGTH attribute.
	 */
	public int getLength()
	{
		if (attributes.containsKey(LENGTH))
			return getInt(LENGTH, 0);
		return 0;
	}
	
	/**
	 * Returns a reference to the document this error is bound to.
	 */
	public IDocument getDocument()
	{
		return document;
	}
	
	public List<AbstractQuickFix> getQuickFixes() {
		return Collections.emptyList();
	}
	
	/**
	 * Sets an attribute.
	 * @param attribute	The key of the attribute
	 * @param value		The value the attribute is set to.
	 */
	public void set(String attribute, String value)
	{
		attributes.put(attribute, value);
	}
	
	/**
	 * Sets an attribute as integer.
	 * @param attribute	The key of the attribute
	 * @param value		The value, as an integer, the attribute is set to.
	 */
	public void set(String attribute, int value)
	{
		attributes.put(attribute, Integer.toString(value));
	}
	
	/**
	 * Binds the error to an document. Since we cannot use references as attributes
	 * for error markers the error object must be manually bound to its corresponding
	 * document after the deserialization.
	 * @param document
	 */
	public void setDocument(IDocument document)
	{
		this.document = document;
	}
	
	/**
	 * Encodes the attributes of this error object as a String.
	 */
	public String encode()
	{
		StringBuilder sbEncode = new StringBuilder();
		for (String attribute: attributes.keySet()) {
			String value = attributes.get(attribute);
			sbEncode.append(attribute)
				.append(SEPERATOR_VAL)
				.append(value)
				.append(SEPERATOR_ATTR);
		}
		if (sbEncode.length() > 0)
			sbEncode.deleteCharAt(sbEncode.length()-1);
		
		return sbEncode.toString(); 
	}
	
	public static AbstractError createFromMarker(IMarker marker) {
		try {
			AbstractError error = AbstractError.decode(marker.getAttribute("data", ""));
			IEditorPart editor = getEditor(marker);
			if (editor instanceof ASMEditor) {
				error.setDocument(((ASMEditor)editor).getDocumentProvider().getDocument(editor.getEditorInput()));
				return error;
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	private static IEditorPart getEditor(IMarker marker) {
		IResource resource = marker.getResource();
		if (resource instanceof IFile) {
			IEditorInput input = new FileEditorInput((IFile)resource);
			
			if (input != null) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				if (page != null)
					return page.findEditor(input);
			}
		}
		return null;
	}
	
	/**
	 * Decodes a String, which have been created by encode(), back into an
	 * instance of the correct subclass of AbstractError (subclass is chosen
	 * by the error type attribute).
	 * @throws Exception 
	 */
	private static AbstractError decode(String encoded) throws Exception
	{
		try {
			Map<String, String> attributes = new HashMap<String,String>();
			String[] pairs = encoded.split(SEPERATOR_ATTR);
			for (String pair: pairs) {
				String[] kv = pair.split(SEPERATOR_VAL);
					attributes.put(kv[0], kv[1]);
			}
			
			ErrorType type = ErrorType.valueOf(attributes.get(AbstractError.TYPE));
			AbstractError error;
			switch (type) {
			case SIMPLE:
				error = new SimpleError(attributes);
				break;
			case SYNTAX_ERROR:
				error = new SyntaxError(attributes);
				break;
			case UNDEFINED:
				error = new UndefinedError(attributes);
				break;
			default:
				error = null;
			}
			
			return error;
	
		}catch(Exception e) {
			//@warning there maybe unhandled error types which will cause an exception during decoding
			throw new Exception("Exception in AbstractError.decode("+encoded+") with message\n"+e.getMessage());
		}
	}
}
