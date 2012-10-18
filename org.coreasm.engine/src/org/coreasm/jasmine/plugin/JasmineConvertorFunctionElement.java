/*	
 * JasmineConvertorFunctionElement.java  	$Revision: 9 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.jasmine.plugin;

import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionElement;

/** 
 * This function element provides type conversion between CoreASM and Java.
 *   
 * @author Roozbeh Farahbod
 * @version $Revision: 9 $, Last modified: $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $
 */
public class JasmineConvertorFunctionElement extends FunctionElement {

	public enum Type {toJava, fromJava};
	
	public static final String TO_JAVA_NAME = "toJava";
	public static final String FROM_JAVA_NAME = "fromJava";
	
	public final Type type;
	
	public JasmineConvertorFunctionElement(Type type) {
		super();
		this.type = type;
		this.setFClass(FunctionClass.fcDerived);
	}

	@Override
	public Element getValue(List<? extends Element> arg0) {
		Element result = Element.UNDEF;
		if (arg0.size() == 1) {
			Element e = arg0.get(0);
			if (type == Type.toJava) 
				result = JasmineUtil.javaValue(e);
			else
				if (e instanceof JObjectElement)
					result = JasmineUtil.asmValue(((JObjectElement)e).object);
				else
					result = Element.UNDEF;
		}
		return result;
	}

}
