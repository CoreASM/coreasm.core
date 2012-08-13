/*	
 * OperatorsContributor.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Mashaal Memon
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/** 
 *	This class represents a operator and its contributor(s) (if any). The object
 *  used to keep this information together ultimately for information/debugging purposes
 *  (i.e. knowing who contributed a particular operator to the grammar).
 *   
 *  @author  Mashaal Memon
 *  
 */
public class OperatorContributor {
	
	final String op;
	final String op2;
	private final HashSet<String> contributors = new HashSet<String>(); // a body may potentially have multiple contributors
	
	/**
	 * Create an object holding a an operator and its contributor.
	 * 
	 * @param op the first (group of) symbol(s) as a <code>String</code>.
	 * @param op2 the second (group of) symbol(s) as a <code>String</code>. A value of
	 * <code>null</code> should be passed in the case of no second (group of) symbols(s).
	 * @param contributor the <code>String</code> representing it's contributor .
	 */
	public OperatorContributor(String op, String op2, String contributor) {
		super();
		this.op = op;
		this.op2 = op2;
		addContributor(contributor);
	}
	
	/**
	 * Add the given operator contributor.
	 * 
	 * @param contributor the <code>String</code> representing a contributor
	 */
	public void addContributor(String contributor) {
		contributors.add(contributor);
	}
	
	/**
	 * Get first (group of) operator symbol(s).
	 *
	 * @return operator symbol(s) as a <code>String</code>
	 */
	public String getOp() {
		return op;
	}
	
	/**
	 * Get second (group of) operator symbol(s).
	 *
	 * @return operator symbol(s) as a <code>String</code>
	 */
	public String getOp2() {
		return op2;
	}
	
	/**
	 * Get contributers as a comma delimited string..
	 *
	 * @return contributors as a comma delimited string <code>String</code>
	 */
	public String getContributorsAsString() {
		String contributorsStr = "";
		
		Iterator<String> it = contributors.iterator();
		
		// put contributors into comma delimited string
		while(it.hasNext()) {
			if (contributorsStr.length() > 0) {
				contributorsStr += ", ";
			}
			
			contributorsStr += it.next();
		}
		
		return contributorsStr;
	}
	
	/**
	 * Get contributers as a collection
	 *
	 * @return contributors as a <code>Collection<String></code>
	 */
	public Collection<String> getContributorsAsCollection() {
		return contributors;
	}
	
	/*------------------------------ STATIC PORTION */
	
	/**
	 * Get operator symbols as a string (which can be used as a key)
	 * 
	 * @param op the first (group of) symbol(s) as a <code>String</code>.
	 * @param op2 the second (group of) symbol(s) as a <code>String</code>. A value of
	 * <code>null</code> should be passed in the case of no second (group of) symbols(s).
	 * 
	 * @return a <code>String</code> representing operator symbols as a string.
	 */
	public static String getOpSymbolsAsString(String op, String op2)
	{
		// get op symbols to use as key; group 2 is only used if defined
		return op + ((op2 != null) ? OperatorRule.OPERATOR_DELIMITER+op2 : "");
	}
	
	
}
