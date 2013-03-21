/*
 * StringMatchingFunction.java 	1.0
 * 
 *
 * Copyright (C) 2013 Marcel Dausend
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.string;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;

/** 
 *  Function returns the boolean result (or undef) as result of regular expression matching using java regular expressions
 *   
 *  @author  Marcel Dausend
 *  
 */
public class StringMatchingFunction extends FunctionElement {

	public static String STRING_MATCHES_FUNCTION_NAME = "matches";
	//needed for ErrorHandling
	private ControlAPI capi;

	
	/**
	 * Creates a new StringMatchingFunction 
	 * @param capi 
	 */
	public StringMatchingFunction(ControlAPI capi) {
		setFClass(FunctionClass.fcDerived);
		this.capi= capi; 
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element ret = Element.UNDEF;

		// if we have the correct number of arguments
		if (args.size() == 2) {
			// if we have the correct type of arguments
			if ((args.get(0) instanceof StringElement) &&
					(args.get(1) instanceof StringElement)) {

				String string = ((StringElement) args.get(0)).string;
				String regex = ((StringElement) args.get(1)).string;

				// check if the second argument is a correct regular expression
				try {
					if (Pattern.compile(regex) != null) 
						if ( string.matches(regex) ) ret = BooleanElement.TRUE;
						else ret = BooleanElement.FALSE;

				}catch (PatternSyntaxException e) {
					capi.error(new CoreASMError("regex used in expression - matches(\""+string+"\", \""+ regex+"\") - is not conform to the syntax of Java regular expressions!\nFor more help look here: http://www.regexplanet.com/advanced/java/",capi.getInterpreter().getInterpreterInstance().getPosition()));
				}
			}
		}
		return ret;
	}
}
