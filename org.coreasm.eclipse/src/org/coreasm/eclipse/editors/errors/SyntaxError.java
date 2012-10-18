package org.coreasm.eclipse.editors.errors;

import java.util.List;
import java.util.Map;

/**
 * This class models a SyntaxError. This is an error which has been thrown
 * by the parser after it encountered a syntax error.
 * It defines the following attributes:
 * <ul>
 * <li>Line & Column: The position of the syntax error, as returned by the parser.</li>
 * <li>Position & Length: The position of the syntax error, as the index of
 * the character where the error occurred. The length of a syntax error is always 1.</li> 
 * <li>Encountered: The token which was found, which is illegal at this position</li>
 * <li>Expected: A list of grammar elements which are allowed at this position</li>
 * </ul>
 * @author Markus Müller
 */
public class SyntaxError 
extends AbstractError 
{
	public SyntaxError(String descr, int line, int column, int position, int length,
			List<String> expected, String encountered) 
	{
		super(ErrorType.SYNTAX_ERROR);
		
		// check expected list & remove "CoreModule" if it's not the only list entry
		// if the list is empty afterwards, add one entry for "CoreASM" and one for "CoreModule"
		for (int i=expected.size()-1; i>=0; i--) {
			String e = expected.get(i);
			if (e.equals("CoreModule")) {
				expected.remove(i);
			}
		}
		if (expected.size() == 0) {
			expected.add("CoreASM");
			expected.add("CoreModule");
		}
		
		// build expected string:
		// the expected list is serialized as a String
		// we use the exotic unicode character 25c6 as the separator for list 
		// elements, so we don't have to escape anything.
		String strExpected = "";
		for (int i=0; i<expected.size(); i++) {
			strExpected += expected.get(i);
			if (i<expected.size()-1)
				strExpected += '\u25c6';
		}

		set(AbstractError.HEADER, "Syntax Error");
		set(AbstractError.DESCRIPTION, descr);
		set(AbstractError.LINE, line);
		set(AbstractError.COLUMN, column);
		set(AbstractError.POSITION, position);
		set(AbstractError.LENGTH, length);
		set(AbstractError.ENCOUNTERED, encountered);
		set(AbstractError.EXPECTED, strExpected);
	}
	
	protected SyntaxError(Map<String,String> attributes)
	{
		super(attributes);
	}
	
	public String getDescription()
	{
		return get(AbstractError.DESCRIPTION);
	}
	
	public int getLine()
	{
		return getInt(AbstractError.LINE, 0);
	}
	
	public int getColumn()
	{
		return getInt(AbstractError.COLUMN, 0);
	}
	
	public int getLength()
	{
		return getInt(AbstractError.LENGTH, 0);
	}
	
	public String getEncountered()
	{
		return get(AbstractError.ENCOUNTERED);
	}
	
	public String[] getExpected()
	{
		String strExpected = get(AbstractError.EXPECTED);
		String[] expected = strExpected.split(Character.toString('\u25c6'));
		return expected;
	}
	
}
