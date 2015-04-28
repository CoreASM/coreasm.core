package org.coreasm.eclipse.editors.errors;

import java.util.Map;


/**
 * This class models a UndefinedError. This is an error which has been thrown
 * by the parser for an unknown reason. This usually means that the parser 
 * is implemented incorrectly. It defines the following attributes:
 * <ul>
 * <li>Description: A description of the error, as deliverd by the parser.</li>
 * <li>Line & Column: The position of the syntax error, as returned by the parser.</li>
 * </ul>
 * @author Markus Müller
 */
public class UndefinedError 
extends AbstractError 
{
	public UndefinedError(String message, int line, int column)
	{
		super(ErrorType.UNDEFINED);
		set(AbstractError.DESCRIPTION, message);
		set(AbstractError.LINE, line);
		set(AbstractError.COLUMN, column);
	}
	
	protected UndefinedError(Map<String,String> attributes)
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
	
}
