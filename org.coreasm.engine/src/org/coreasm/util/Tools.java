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
 
package org.coreasm.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import org.coreasm.engine.Specification;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 *	Provides some general functionalities 
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class Tools {
	
	private static final String CONF_COREASM_ENGINE_ROOT_FOLDER = "org.coreasm.engine.rootFolder";

	private final static Logger logger = LoggerFactory.getLogger(Tools.class);

	private static String eol = null;
	
	public static final int DEFAULT_STRING_LENGTH_LIMIT =   40;
	private static Random random = new Random();
	
	/**
	 * Formats a <code>double</code> value into a <code>String</code>
	 * with <i>d</i> digits after decimal point.
	 */
	public static String dFormat(double v, int d) {
		double p = Math.pow(10, d);
		return String.valueOf( Math.round(v * p) / p );
	}

	/**
	 * Formats an integer (<code>long</code>) value into a <code>String</code> 
	 * with possibly leading zeros so that the result has at least <i>d</i> digits.
	 */
	public static String lFormat(long v, int d) {
		int initL;
		
		if (v == 0)
			initL = 1;
		else
			initL = 1 + (int)Math.floor(Math.log(v) / Math.log(10));
		if (initL >= d) 
			return String.valueOf(v);
		char[] zeros = new char[d - initL];
		for (int i=0; i < zeros.length; i++)
			zeros[i] = '0';
		String s = new String(zeros) + String.valueOf(v);
		return s;
	}
	
	/**
	 * @param str A string
	 * @return returns <code>true</code> if the string contains only alphabetic characters; <code>false</code> otherwise.
	 */
	public static boolean isAlphabets(String str) {
		char[] chars = new char[str.length()];
		String alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		str.toUpperCase().getChars(0, str.length(), chars, 0);
		for (int i=0; i < chars.length; i++) {
			if (alphabets.indexOf(chars[i]) < 0) 
				return false;
		}
		return true;
	}
	
	/**
	 * Returns a random integer number in [0 .. max).
	 */
	public static int randInt(int max) {
		return random.nextInt(max);
	}
	
	/**
	 * Returns a String version of the given integer
	 * in an ordered form; i.e., "1st", "32nd", "54th", etc.
	 * 
	 * @param i the order
	 * @return the string version of the order
	 */
	public static String getIth(int i) {
		String result = null;
		switch(i % 10) {
		case 1:
			result = "st";
			break;
		case 2:
			result = "nd";
			break;
		case 3:
			result = "rd";
			break;
		default:
			result = "th";
		}
		return String.valueOf(i) + result;
	}

	/**
	 * Converts all occurrences of special characters such as new-line and tab, 
	 * to their corresponding escape sequences.
	 * <p>
	 * The supported escape sequences are: <code>\\, \n, \t, \r, \"</code>
	 * 
	 *  @return the same string with all the special characters converted to escape sequences
	 */
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

	/**
	 * If length of <code>value</code> is longer than <code>limit</code>
	 * cuts a piece from the center of <code>value</code> and replaces it with 
	 * '<code>...</code>' so that it fits in the <code>limit</code>. Otherwise,
	 * returns <code>value</code>.
	 */
	public static String sizeLimit(String value, int limit) {
		if (value.length() <= limit) 
			return value;
		else {
			int d = limit - 3;
			String result = value.substring(0, d / 2) + "...";
			result += value.substring(value.length() - (d - (d / 2)));
			return result;
		}
	}

	/**
	 * If length of <code>value</code> is longer than {@link #DEFAULT_STRING_LENGTH_LIMIT} 
	 * cuts a piece from the center of <code>value</code> and replaces it with 
	 * '<code>...</code>' so that it fits in the limit. Otherwise,
	 * returns <code>value</code>.
	 */
	public static String sizeLimit(String value) {
		return sizeLimit(value, DEFAULT_STRING_LENGTH_LIMIT);
	}
	
	/**
	 * @return a system independent EOL string.
	 */
	public static String getEOL() {
		if (eol == null) {
			eol = System.getProperty("line.separator");
			if (eol == null)
				eol = "\n";
		}
		return eol;
	}
	
	/**
	 * If the given string is enclosed in double quotes, returns the portion of 
	 * the string which is enclosed in the double quotes.
	 */
	public static String trimDoubleQuotes(String str) {
		if (str.endsWith("\"") && str.startsWith("\"") && str.length() > 2)
			str = str.substring(1, str.length() - 1);
		return str;
	}
	
	/**
	 * If the given string is enclosed in double quotes, removes all such double quotes and 
	 * returns the portion of 
	 * the string which is enclosed in the double quotes.
	 */
	public static String trimAllDoubleQuotes(String str) {
		while (str.endsWith("\"") && str.startsWith("\"") && str.length() > 2)
			str = str.substring(1, str.length() - 1);
		return str;
	}


	/**
	 * Creates a context info for the given collection of updates, appending it to the given StringBuffer.
	 *  
	 * @param indent indentation
	 * @param updates the collection of updates
	 * @param parser a link to the parser 
	 * @param spec a link to the specification
	 */
	public static String getContextInfo(String indent, Collection<Update> updates, Parser parser, Specification spec) {
		StringBuffer result = new StringBuffer();
		if (updates != null && updates.size() > 0) {
			for (Update u: updates) {
				result.append(getContextInfo(result, indent, u, parser, spec));
			}
		}
		
		return result.toString();
	}

	/**
	 * Creates a context info for the given update, appending it to the given StringBuffer.
	 *  
	 * @param indent indentation
	 * @param update the update
	 * @param parser a link to the parser 
	 * @param spec a link to the specification
	 */
	public static String getContextInfo(String indent, Update update, Parser parser, Specification spec) {
		StringBuffer result = new StringBuffer();
		if (update != null) {
			getContextInfo(result, indent, update, parser, spec);
		}
		return result.toString();
	}
	
	/**
	 * A wrapper for the String tokenizer of the standard Java library.
	 * 
	 * @param input the string of values separated by the delimiter
	 * @param delim the delimiter
	 * @return an {@link ArrayList} of {@link String} values
	 */
	public static List<String> tokenize(String input, String delim) {
		List<String> result = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(input, delim);
		while (tokenizer.hasMoreTokens())
			result.add(tokenizer.nextToken(delim));
		return result;
	}

	/*
	 * Creates a context info for the given update, appending it to the given StringBuffer.
	 *  
	 * @param buffer StringBuffer instance
	 * @param indent indentation
	 * @param update the update
	 * @param parser a link to the parser 
	 * @param spec a link to the specification
	 */
	private static String getContextInfo(StringBuffer buffer, String indent, Update update, Parser parser, Specification spec) {
		buffer.append(indent + "  - " + update);
    	if (update.sources != null) {
    		buffer.append(" produced by the following " + ((update.sources.size()>1)?"lines":"line") + ":" + Tools.getEOL());
    		for (ScannerInfo info: update.sources) {
    			buffer.append(indent + "      - " + info.getContext(parser, spec));
    		}
    	}
    	buffer.append(Tools.getEOL());
		
		return buffer.toString();
	}

	/**
	 * Given a list of nodes, returns the list of values of those nodes.
	 * 
	 * @throws InterpreterException if a node in the list does not have a value
	 */
	public static ElementList getValueList(List<ASTNode> nodes) throws InterpreterException {
		if (nodes.size() == 0)
			return ElementList.NO_ARGUMENT;
		
		ArrayList<Element> vList = new ArrayList<Element>();
		Element value = null;
		for (ASTNode n: nodes) {
			value = n.getValue();
			if (value == null) 
				throw new InterpreterException("Expecting expression as argument.");
			vList.add(n.getValue());
		}
		// avoiding to have too many empty lists
		return ElementList.create(vList);
	}

	/**
	 * Searches for the first occurrence of <code>object</code> in the given 
	 * array and returns the index. If none is found or the list is empty, returns -1.
	 *  
	 * @param <T>
	 * @param object
	 * @param list
	 * @return the index of the found node
	 */
	public static <T> int find(T object, T[] list) {
		if (list.length < 1) 
			return -1;
		for (int i=0; i < list.length; i++) 
			if (object.equals(list[i]))
				return i;
		return -1;
	}

	// TODO later move all these to a configuration class
	
	/**
	 * Detects and returns the root folder of the running application.
	 */
	public static String getRootFolder() {
		String rootFolder = System.getProperty(CONF_COREASM_ENGINE_ROOT_FOLDER);
		if (rootFolder == null) {
			rootFolder = getRootFolder(null);
			System.setProperty(CONF_COREASM_ENGINE_ROOT_FOLDER, rootFolder);
		}
		return rootFolder;
	}

	/**
	 * Sets the root folder of the CoreASM engine
	 * 
	 * @param rootFolder the full path to the root folder
	 */
	public static void setRootFolder(String rootFolder) {
		System.setProperty(CONF_COREASM_ENGINE_ROOT_FOLDER, rootFolder);
	}

	/**
	 * Detects and returns the root folder of the running application.
	 */
	public static String getRootFolder(Class<?> mainClass) {
		if (mainClass == null)
			mainClass = Tools.class;
		
		final String baseErrorMsg = "Cannot locate root folder.";

		final String classFile = mainClass.getName().replaceAll("\\.", "/") + ".class";
		final URL classURL = ClassLoader.getSystemResource(classFile);

		String fullPath = "";
		String sampleClassFile = "/org/coreasm/util/Tools.class";
		if (classURL == null) {
			Tools tempObject = new Tools();
			fullPath = tempObject.getClass().getResource(sampleClassFile).toString();
			logger.warn("{} The application may be running in an OSGi container.", baseErrorMsg);
			File file = new File(".");
			logger.warn("Root folder is assumed to be {}.", file.getAbsolutePath());
			return ".";
		} else {
			fullPath = classURL.toString();
		}
		
		
		try {
			fullPath = URLDecoder.decode(fullPath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.warn("{} UTF-8 encoding is not supported.", baseErrorMsg);
			return ".";
		}
		
		if (fullPath.indexOf("file:") > -1) {
			fullPath = fullPath.replaceFirst("file:", "").replaceFirst(classFile, "");
			fullPath = fullPath.substring(0, fullPath.lastIndexOf('/'));
		} 
		if (fullPath.indexOf("jar:") > -1) {
			fullPath = fullPath.replaceFirst("jar:", "").replaceFirst("!" + classFile, "");
			fullPath = fullPath.substring(0, fullPath.lastIndexOf('/'));
		}
		if (fullPath.indexOf("bundleresource:") > -1) {
			fullPath = fullPath.substring(0, fullPath.indexOf(sampleClassFile));
		}
		
		// replace the java separator with the 
		fullPath = fullPath.replace('/', File.separatorChar);
		
		// remove leading backslash
		if (fullPath.startsWith("\\")){
			fullPath = fullPath.substring(1);
		}
		
		// remove the final 'bin'
		final int binIndex = fullPath.indexOf(File.separator + "bin");
		if (binIndex == fullPath.length() - 4)
			fullPath = fullPath.substring(0, binIndex);
		
		logger.debug("Root folder is detected at {}.", fullPath);
		
		return fullPath;
	}

}

