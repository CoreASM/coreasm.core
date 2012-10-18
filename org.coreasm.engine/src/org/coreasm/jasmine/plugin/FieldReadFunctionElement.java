/*
 * FieldReadFunctionElement.java 		$Revision: 9 $
 * 
 * Copyright (c) 2008 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.jasmine.plugin;

import java.lang.reflect.Field;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.string.StringElement;

/**
 * A monitored function to read field values of Java objects.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class FieldReadFunctionElement extends FunctionElement {

	private JasminePlugin jasmine;
	
	public static final String SUGGESTED_NAME = "javaFieldRead";
	
	public FieldReadFunctionElement(JasminePlugin jasmine) {
		this.setFClass(FunctionClass.fcMonitored);
		this.jasmine = jasmine;
	}

	/**
	 * If the size of <code>args</code> is 2 and the first argument is
	 * a {@link JObjectElement} and the second argument is a
	 * field name (as a {@link String}), this method returns 
	 * the value of the given field in the encapsulated object.
	 * 
	 *  Otherwise, it returns {@link Element#UNDEF}.
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (args.size() == 2) {
			String fieldName = ((StringElement)args.get(0)).getValue();
			JObjectElement jobj = (JObjectElement)args.get(1);
			Field field = null;
			
			// get the field
			try {
				field = jobj.jType().getField(fieldName);
			} catch (Exception e) {
				return result;
			}
			
			// get the value of the field
			Object fieldValue = null;
			try {
				fieldValue = field.get(jobj.object);
			} catch (Exception e) {
				return result;
			}
			
			if (jasmine.isImplicitConversionMode())
				result = JasmineUtil.asmValue(fieldValue);
			else
				result = new JObjectElement(fieldValue);
		}			

		return result;
	}

}
