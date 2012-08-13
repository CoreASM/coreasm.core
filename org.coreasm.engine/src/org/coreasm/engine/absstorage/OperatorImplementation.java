/*	
 * OperatorImplementation.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import java.util.List;

/** 
 *	Superclass of any operator implementation.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public abstract class OperatorImplementation {

	/**
	 * Returns the operator (token) that this
	 * object implements.
	 * 
	 * @return operator token
	 */
	public abstract String getOperator();

	/**
	 * Tells whether this implementation works over 
	 * the given arguments. 
	 * @param args operands
	 * @return <code>true</code> if it applies, <code>false</code>
	 * otherwise.
	 */
	public abstract boolean applies(List<? extends Element> args);

	/**
	 * Applies this operator implementation on 
	 * the given operands, and returns the result.
	 * 
	 * @param args operands
	 * @return result of applying the operator on the operands
	 */
	public abstract Element apply(List<? extends Element> args);
}
