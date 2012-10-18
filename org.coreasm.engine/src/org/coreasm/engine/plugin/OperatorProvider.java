/*	
 * OperatorProvider.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 Mashaal Memon
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugin;

import java.util.Collection;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.parser.OperatorRule;

/** 
 * CoreASM plug-ins can implement this interface to extend the language with new
 * operators or new behavior for already existing operators.
 *   
 *  @author  Mashaal Memon, Roozbeh Farahbod
 *  
 */
public interface OperatorProvider {
	
	/**
	 * Returns the operator rules, and thus operator syntax, provided by 
	 * this plugin as a list of operator rules.
	 *  
	 * @return a collection of operator rule information in <code>OperatorRule</code>s
	 */
	public abstract Collection<OperatorRule> getOperatorRules();
	
	/**
	 * Holds all behaviors of every operator provided by a plugin.
	 * Based on information stored in the node passed to the plugin here,
	 * the correct operator behavior is used to evaluate the node.
	 * - If there is a valid semantics for the operator and given operands, the
	 * resultant element should be returned.
	 * - If there is NO semantics for the operator and operands, a
	 * value of <code>null</code> should be returned.
	 * - If there is a problem with the operands provided, an 
	 * <code>InterpreterException</code> should be thrown.
	 * <p>
 	 * <b>NOTE:</b> Any implementation of this method must be thread-safe, since
 	 * it may be called simultaneously by more than one thread during the simulation.
 	 *  
 	 * @param interpreter the interpreter instance that calls this method
	 * @param opNode an AST <code>Node</code> for the given operator
	 * which should have a behavior provided by this plugin.
	 * 
	 * @return an <code>Element</code> which is the result of an 
	 * operator behavior evaluating a node.
	 * 
	 * @throws an <code>Interpreter</code> Exception if the result of
	 * interpreting this node is an error.
	 */
	public abstract Element interpretOperatorNode(Interpreter interpreter, ASTNode opNode) throws InterpreterException;

}
