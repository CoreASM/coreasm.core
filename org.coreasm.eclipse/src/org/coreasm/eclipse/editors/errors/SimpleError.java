package org.coreasm.eclipse.editors.errors;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.Node;

/**
 * This class models a SimpleError. This is an error which has been found by
 * an IErrorParser. It defines the following attributes:
 * <ul>
 * <li>Header & Description: A short and a long description of the error</li>
 * <li>Position & Length: The position of the error and its legnth within the source</li>
 * <li>Classname: Because error markers can only contain strings and integers as
 * attributs, we cannot store a reference to the object which founs the error to
 * the marker. So we store the name of the class and use the Java Reflection API
 * to call the getQuixkFixes() method for the correct class.</li>
 * <li>Error_ID: One instance of an IErrorParser can create different kind of errors
 * which must be handled differently. So each error is identified by an Error_ID
 * tag which is used by the getQuickFixes() method to deliver the right set
 * of QuickFixes.</li>
 * </ul>
 * @author Markus Müller
 */
public class SimpleError
extends AbstractError
{
	public SimpleError(String title, String descr, int position,
			int length, String classname, String errorID) 
	{
		super(ErrorType.SIMPLE);
		set(AbstractError.HEADER, title);
		set(AbstractError.DESCRIPTION, descr);
		set(AbstractError.POSITION, position);
		set(AbstractError.LENGTH, length);
		set(AbstractError.CLASSNAME, classname);
		set(AbstractError.ERROR_ID, errorID);
	}
	
	public SimpleError(String title, String descr, Node node,
			ASMDocument document, int length, String classname, String errorID) {
		super(ErrorType.SIMPLE);
		set(AbstractError.HEADER, title);
		set(AbstractError.DESCRIPTION, descr);
		set(AbstractError.POSITION, document.getNodePosition(node));
		set(AbstractError.LENGTH, length);
		set(AbstractError.CLASSNAME, classname);
		set(AbstractError.ERROR_ID, errorID);
	}
	
	protected SimpleError(Map<String, String> attributes)
	{
		super(attributes);
	}
	
	public String getTitle()
	{
		return get(AbstractError.HEADER);
	}
	
	public String getDescription()
	{
		return get(AbstractError.DESCRIPTION);
	}
	
	public int getPosition()
	{
		return getInt(AbstractError.POSITION, 0);
	}
	
	public int getLength()
	{
		return getInt(AbstractError.LENGTH, 0);
	}
	
	public String getClassname()
	{
		return get(AbstractError.CLASSNAME);
	}
	
	public String getErrorID()
	{
		return get(AbstractError.ERROR_ID);
	}
	
	@Override
	public List<AbstractQuickFix> getQuickFixes() {
		List<AbstractQuickFix> list = new LinkedList<AbstractQuickFix>();

		try {
			Class<?> errorParser = Class.forName(getClassname());
			Method method = errorParser.getMethod("getQuickFixes", String.class);
			Object o = method.invoke(null, getErrorID());
			List<AbstractQuickFix> l = (List<AbstractQuickFix>) o;
			// Sort out fixes which are not applicable for the given error
			for (AbstractQuickFix fix: l)
				if (fix.checkValidility(this) == true)
					list.add(fix);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		for (AbstractQuickFix fix: list)
			fix.initChoices(this);

		return list;
	}
}
