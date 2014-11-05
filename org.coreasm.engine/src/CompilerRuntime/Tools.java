/*	

 * Tools.java 	1.0 	$Revision: 243 $
 * The CoreASM Project
 *
 * Copyright (C) 2005-2009 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package CompilerRuntime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 *	Provides some general functionalities 
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class Tools {
	public static String convertToEscapeSqeuence(String string) {
		char[] chars = new char[string.length()];
		char[] result = new char[string.length()*2];
		string.getChars(0, string.length(), chars, 0);
		int srcIndex = 0;
		int resIndex = 0;
		while (srcIndex < chars.length) {
			switch (chars[srcIndex]) {
			case '\\':
				result[resIndex] = '\\';
				result[resIndex+1] = '\\';
				resIndex++;
				break;
			case '\r':
				result[resIndex] = '\\';
				result[resIndex+1] = 'r';
				resIndex++;
				break;
			case '\t':
				result[resIndex] = '\\';
				result[resIndex+1] = 't';
				resIndex++;
				break;
			case '\n':
				result[resIndex] = '\\';
				result[resIndex+1] = 'n';
				resIndex++;
				break;
			case '\"':
				result[resIndex] = '\\';
				result[resIndex+1] = '"';
				resIndex++;
				break;
			default:
				result[resIndex] = chars[srcIndex];
			}
			srcIndex++;
			resIndex++;
		}
		
		return new String(result, 0, resIndex);
	}

	/**
	 * Converts escape sequences to their corresponding characters. 
	 * This method replaces all escape sequences to their corresponding special
	 * characters.
	 * <p>
	 * The supported escape sequences are: <code>\\, \n, \t, \r, \"</code>
	 *
	 *  @return the same string with all the escape sequences converted to their 
	 *  		corresponding special characters
	 *   
	 * @throws IllegalArgumentException if there is an invalid escape character
	 */
	public static String convertFromEscapeSequence(String str) throws IllegalArgumentException {
		char[] chars = new char[str.length()];
		char[] result = new char[str.length()];
		str.getChars(0, str.length(), chars, 0);
		int srcIndex = 0;
		int resIndex = 0;
		while (srcIndex < chars.length - 1) {
			if (chars[srcIndex] == '\\') {
				switch (chars[srcIndex + 1]) {
				case '\\':
					result[resIndex] = '\\';
					break;
				case 'n':
					result[resIndex] = '\n';
					break;
				case 't':
					result[resIndex] = '\t';
					break;
				case 'r':
					result[resIndex] = '\r';
					break;
				case '"':
					result[resIndex] = '"';
					break;
				default:
					throw new IllegalArgumentException("Invalid escape sequence '\\" + chars[srcIndex+1] + "' in the string constant.");
				}
				srcIndex++;
			} else
				result[resIndex] = chars[srcIndex];
			srcIndex++;
			resIndex++;
		}
		
		if (chars.length > 0 && srcIndex < chars.length ) {
			if (chars[srcIndex] == '\\')
				throw new IllegalArgumentException("Invalid escape sequence '\\' in the string constant.");

			result[resIndex] = chars[srcIndex];
			srcIndex++;
			resIndex++;
		}
			
		return new String(result, 0, resIndex);
	}
}

