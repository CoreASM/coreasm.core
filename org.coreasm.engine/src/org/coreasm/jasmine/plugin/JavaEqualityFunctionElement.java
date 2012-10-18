/*
 * JavaEqualityFunctionElement.java 		$Revision: 9 $
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

import java.util.List;

import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;

/**
 * A monitored function to check the equality of two JObject elements
 * by calling the {@link Object#equals(Object)} method on their
 * encapsulated Java objects.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class JavaEqualityFunctionElement extends FunctionElement {

	public static final String SUGGESTED_NAME = "jEquals";
	
	public JavaEqualityFunctionElement() {
		this.setFClass(FunctionClass.fcMonitored);
	}

	/**
	 * If the size of <code>args</code> is 2 and both
	 * arguments are instances of {@link JObjectElement},
	 * this method returns the result of equality check 
	 * on the arguments by calling {@link Object#equals(Object)}
	 * on the reference object of the first argument.
	 * 
	 * Otherwise it returns {@link BooleanElement#FALSE}.
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = BooleanElement.FALSE;
		if (args.size() == 2) 
			if (args.get(0) instanceof JObjectElement && args.get(1) instanceof JObjectElement) {
				JObjectElement jobj1 = (JObjectElement)args.get(0);
				JObjectElement jobj2 = (JObjectElement)args.get(1);
				
				result = BooleanElement.valueOf(jobj1.object.equals(jobj2.object));
			}			

		return result;
	}

}
