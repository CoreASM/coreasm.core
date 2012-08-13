/*	
 * InterpreterPlugin.java 	1.0 	$Revision: 243 $
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
 
package org.coreasm.engine.plugin;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;

/** 
 *	Interface for plugins that extend the Interpreter module.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public interface InterpreterPlugin {

	/**
 	 * This method is the interpreter rule of this plugin. 
 	 * This method gets the value of <i>pos</i> and returns 
 	 * a new value for <i>pos</i>. This is the implementation
 	 * of the <i>pluginRule</i> function.
 	 * <p>
 	 * This method should NOT return <code>null</code>. If this method
 	 * cannot interpret <code>pos</code>, it should return <code>pos</code>.
 	 * <p>
 	 * <b>NOTE:</b> Any implementation of this method must be thread-safe, since
 	 * it may be called simultaneously by more than one thread during the simulation. 
 	 *  
	 * @param interpreter the parent interpreter (most likely, component of the engine)
	 * @param pos the value of <i>pos</i>
	 *  
	 * @return new value of <i>pos</i>
	 */
	public abstract ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException;

}
