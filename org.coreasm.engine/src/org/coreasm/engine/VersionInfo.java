/*	
 * VersionInfo.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine;

import java.util.StringTokenizer;

/** 
 * Holds version information of a module.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class VersionInfo implements Comparable<VersionInfo> {
	
	private static final long MAX_VALUE = 9999;
	
	public final int major;
	public final int minor;
	public final int build;
	public final String postfix;
	
	public VersionInfo(int major, int minor, int build, String postfix) {
		if (major > MAX_VALUE || minor > MAX_VALUE || build > MAX_VALUE)
			throw new IllegalArgumentException("All version values must be below " + MAX_VALUE + ".");
		this.major = major;
		this.minor = minor;
		this.build = build;
		if (postfix == null)
			this.postfix = "";
		else
			this.postfix = postfix;
	}

	public String toString() {
		String str = major + "." + minor + "." + build;
		if (postfix != null && postfix.length() > 0)
			str = str + "-" + postfix;
		return str;
	}

	private long combinedValue() {
		return (MAX_VALUE+1) * (MAX_VALUE+1) * major +
			   (MAX_VALUE+1) * minor + build;
	}

	public int compareTo(VersionInfo o) {
		long dl = this.combinedValue();
		long dr = o.combinedValue();
		
		if (dl < dr) 
			return -1;
		else
			if (dl > dr)
				return +1;
			else
				return this.postfix.compareTo(o.postfix);
	}
	
	/**
	 * Parses the given string argument into a 
	 * VersionInfo object instance. The string should be 
	 * in the following format:
	 * <p>
	 * major.minor.build-postfix
	 * <p>
	 * The major field is the only mandatory field. The rest, 
	 * if provided, should appear in the given order; i.e., you cannot
	 * have a version information with only major and build.
	 * 
	 * @param str
	 */
	public static VersionInfo valueOf(String str) {
		String major = null;
		String minor = "0";
		String build = "0";
		String postfix = "";
		StringTokenizer tokenizer = new StringTokenizer(str, ".-");
		if (tokenizer.hasMoreTokens()) {
			major = tokenizer.nextToken();
			if (tokenizer.hasMoreTokens()) {
				minor = tokenizer.nextToken();
				if (tokenizer.hasMoreTokens()) {
					build = tokenizer.nextToken();
					if (tokenizer.hasMoreTokens())
						postfix = tokenizer.nextToken();
				}
			}
		}

		if (major == null)
			return null;
		else {
			try {
				int iMajor = Integer.parseInt(major);
				int iMinor = Integer.parseInt(minor);
				int iBuild = Integer.parseInt(build);
				return new VersionInfo(iMajor, iMinor, iBuild, postfix);
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}
}
