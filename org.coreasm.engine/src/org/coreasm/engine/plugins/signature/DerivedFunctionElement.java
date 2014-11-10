/*	
 * DerivedFunctionElement.java  	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.signature;

import java.util.Collections;
import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;

/** 
 * Derived functions keep a copy of the expression tree and call the 
 * interpreter to evaluate the subtree everytime they are asked for
 * their value.
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class DerivedFunctionElement extends FunctionElement {

	protected final ControlAPI capi;
	protected final List<String> params;
	protected final ASTNode expr;
	
	/**
	 * Creates a new derived function with the given list 
	 * of parameters.
	 */
	public DerivedFunctionElement(ControlAPI capi, List<String> params, ASTNode expr) {
		this.capi = capi;
		this.params = Collections.unmodifiableList(params);
		this.expr = expr;
		setFClass(FunctionClass.fcDerived);
	}
	
	/*
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (args.size() == params.size()) {
			Interpreter interpreter = capi.getInterpreter().getInterpreterInstance();
			bindArguments(interpreter, args);
			
			synchronized(this) {
				ASTNode exprCopy = (ASTNode)interpreter.copyTree(expr);
				try {
					interpreter.interpret(exprCopy, interpreter.getSelf());
					if (exprCopy.getValue() != null)
						result = exprCopy.getValue();
				} catch (InterpreterException e) {
					capi.error(e, expr, interpreter);
				} finally {
					unbindArguments(interpreter);
				}
			}
		}
		
		return result;
	}

	protected void bindArguments(Interpreter interpreter, List<? extends Element> values) {
		interpreter.hideEnvVars();
		for (int i=0; i < params.size(); i++)
			interpreter.addEnv(params.get(i), values.get(i));
	}
	
	protected void unbindArguments(Interpreter interpreter) {
		for (int i=0; i < params.size(); i++)
			interpreter.removeEnv(params.get(i));
		interpreter.unhideEnvVars();
	}
}
