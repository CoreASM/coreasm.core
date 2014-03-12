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
	
	private static final String CONF_COREASM_ENGINE_ROOT_FOLDER = "org.coreasm.engine.rootFolder";

	private final static Logger logger = LoggerFactory.getLogger(Tools.class);

	private static String eol = null;
	
	public static final int DEFAULT_STRING_LENGTH_LIMIT =   40;

	public static final String COREASM_ENGINE_LIB_PATH = "coreasm.engine.lib.path";

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
	
	/**
	 * @return a time stamp of the format "yy.MM.dd - HH:mm" for the given date.
	 * 
	 * @param date the base date
	 */
	public static String getTimeStamp(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yy.MM.dd - HH:mm");
		return formatter.format(date);
	}

	/**
	 * @return a time stamp for the current time and date.
	 * 
	 * @see #getTimeStamp(Date)
	 */
	public static String getTimeStamp() {
		return getTimeStamp(new Date());
	}

	/**
	 * Adds a sequence of the given character to the beginning of the given
	 * string until it reaches the given length.
	 * 
	 * @param src
	 *            source string
	 * @param filler
	 *            filler character
	 * @param fixedLen
	 *            desired length of the resulting string
	 * @return the extended string
	 */
	public static String extendStr(String src, char filler, int fixedLen) {
		String result = src;
		while (result.length() < fixedLen) {
			result = filler + result;
		}
		return result;
	}

	/**
	 * Given a base directory and a path to a file, it concatenates the two
	 * parts and takes care of missing file separators.
	 * 
	 * @param baseDir
	 *            base directory
	 * @param fileName
	 *            file name
	 * @return the absolute path to the file
	 */
	public static String concatFileName(String baseDir, String fileName) {
		// cleanup
		baseDir = baseDir.trim();
		fileName = fileName.trim();

		if (baseDir.lastIndexOf(File.separator) != baseDir.length() - 1) {
			baseDir = baseDir + File.separator;
		}

		String concat = baseDir + fileName;

		return concat;
	}

	/**
	 * Given a base directory and a path to a file, it creates a full path to
	 * the file. If the base directory is not absolute, it adds the application
	 * root directory It also takes care of missing file separators.
	 * 
	 * @param baseDir
	 *            base directory
	 * @param fileName
	 *            file name
	 * @param rootFolder
	 *            root folder of the application
	 * @return the absolute path to the file
	 */
	public static String toFullPath(String baseDir, String fileName, String rootFolder) {
		String result = concatFileName(baseDir, fileName);

		File file = new File(result);
		if (!file.isAbsolute()) {
			result = concatFileName(rootFolder, result);
		}

		return result;
	}

	/**
	 * @return true if the given filename has is an absolute path.
	 * 
	 * @param fileName a file name
	 */
	public static boolean isAbsolutePath(String fileName) {
		final File file = new File(fileName);
		return file.isAbsolute();
	}

	/**
	 * @return <code>true</code> if the given file exists.
	 * 
	 * @param fileName
	 *            a file name
	 */
	public static boolean fileExists(String fileName) {
		final File file = new File(fileName);
		return file.exists();
	}

	/**
	 * Looks for the given file and returns an input stream view of the file if
	 * it is found. If the file name is not an absolute path, it looks for the
	 * file in the following order:
	 * <ol>
	 * <li>the container directory,</li>
	 * <li>current folder,</li>
	 * <li>the container directory in classpath,</li>
	 * <li>and finally the classpath.</li>
	 * </ol>
	 * where classpath is determined by the given class loader.
	 * 
	 * @param classLoader the class loader
	 * @param rootDir the application root directory 
	 * @param container the container folder where the file is expected to be
	 * @param fileName the name of the file
	 * 
	 * @return the input stream
	 * 
	 * @throws FileNotFoundException if the file is not found
	 */
	public static InputStream findConfigFileAsInputStream(
			ClassLoader classLoader, String rootDir, String container, String fileName)
			throws FileNotFoundException {

		String pathToFile = fileName;

		// 0. Is the file name an absolute path?
		if (isAbsolutePath(pathToFile)) {
			return new FileInputStream(pathToFile);
		}

		// 1. Try the container directory, if it exists
		pathToFile = concatFileName(rootDir,
				((container == null || container.length() == 0) ? fileName
						: (Tools.concatFileName(container, fileName))));
		if (Tools.fileExists(pathToFile)) {
			return new FileInputStream(pathToFile);
		}

		// 2. Try the root directory
		pathToFile = Tools.concatFileName(rootDir, fileName);
		if (Tools.fileExists(pathToFile)) {
			return new FileInputStream(pathToFile);
		}

		// 3.1 Convert all File.separators (like '\') to slashes. Otherwise the
		// resource may not be found in the classpath.
		fileName = fileName.replace(File.separatorChar, '/');

		// 3.2 Try the container directory in the classpath
		pathToFile = (container == null || container.length() == 0) ? fileName : (container + "/" + fileName);

		InputStream inStream = classLoader.getResourceAsStream(pathToFile);
		if (inStream == null) {
			if (container != null && container.length() > 0) {

				// 4. Try the root directory in the classpath
				pathToFile = fileName;
				inStream = classLoader.getResourceAsStream(pathToFile);
			}
		}

		if (inStream != null) {
			return inStream;
		} else {
			throw new FileNotFoundException();
		}
	}

}

