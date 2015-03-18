/*
 * ControlAPI.java 	1.0 	$Revision: 243 $
 * 				updated 	25-Dec-2005		
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
 
package org.coreasm.engine;

import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterListener;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.scheduler.Scheduler;
import org.coreasm.engine.parser.Parser;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.ServiceRegistry;

import java.util.List;
import java.util.Set;

/**
 * Defines the interface of a CoreASM Engine to its both internal and external environments 
 * (e.g., internal engine components and external tools such as GUI and test tools).
 *  
 * @author Roozbeh Farahbod
 * 
 */
public interface ControlAPI extends CoreASMEngine, ServiceRegistry {
	
    /**
    * Adds the specified interpreter listener to receive interpreter events.
    * @param listener the interpreter listener
    */
   public void addInterpreterListener(InterpreterListener  listener);

   /**
    * Removes the specified interpreter listener so that it no longer receives interpreter events.
    * @param listener the interpreter listener
    */
   public void removeInterpreterListener(InterpreterListener listener);

   /**
    * Returns a list of all the registered interpreter listeners registered.
    * @return all registered <code>InterpreterListener</code>s.
    */
   public List<InterpreterListener> getInterpreterListeners();
   
	/**
	 * Returns the scheduler module of the engine.
	 */
	public Scheduler getScheduler();

	/**
	 * Returns the abstract storage module of the engine.
	 */
	public AbstractStorage getStorage();
	
	/**
	 * Returns the interpreter module of the engine. 
	 */
	public Interpreter getInterpreter();

	/**
	 * Returns the parser module of the engine.
	 */
	public Parser getParser();
	
	/**
	 * Returns a loaded plugin with the given name.
	 */
	public Plugin getPlugin(String name);
	
	/**
	 * Returns a set of all the loaded plugins.
	 */
	public Set<Plugin> getPlugins();

	/**
	 * Puts the engine in an error mode and sends out an error message.
	 */
	public void error(String msg);

	/**
	 * Puts the engine in an error mode,
	 * saves the throwable object and sends out the throwable's detailed message.
	 */
	public void error(Throwable e);

	/**
	 * Puts the engine in an error mode and sends out an error message.
	 * 
	 * @param msg error message
	 * @param errorNode the {@link ASTNode} on which the error occured
	 * @param interpreter the instance of the interpreter generating the error (can be <code>null</code>)
	 */
	public void error(String msg, Node errorNode, Interpreter interpreter);

	/**
	 * Puts the engine in an error mode,
	 * saves the Throwable object and sends out the throwable's detailed message.
	 * 
	 * @param e error
	 * @param errorNode the {@link ASTNode} on which the error occured
	 * @param interpreter the instance of the interpreter generating the error (can be <code>null</code>)
	 */
	public void error(Throwable e, Node errorNode, Interpreter interpreter);

	/**
	 * Puts the engine in an error mode,
	 *
	 * @param e the error instance
	 */
	public void error(CoreASMError e);

	/**
	 * Sends a warning message to the engine.
	 * 
	 * @param src the source component generating this warning
	 * @param msg the message
	 */
	public void warning(String src, String msg);
	
	/**
	 * Sends a warning message to the engine.
	 *
	 * @param src the source component generating this warning
	 * @param e the cause of this warning
	 */
	public void warning(String src, Throwable e);

	/**
	 * Sends a warning message to the engine.
	 *
	 * @param src the source component generating this warning
	 * @param msg error message
	 * @param node the {@link ASTNode} on which the warning occurred
	 * @param interpreter the instance of the interpreter generating the warning (can be <code>null</code>)
	 */
	public void warning(String src, String msg, Node node, Interpreter interpreter);

	/**
	 * Sends a warning message to the engine.
	 *
	 * @param src the source component generating this warning
	 * @param e the cause of this warning
	 * @param node the {@link ASTNode} on which the warning occurred
	 * @param interpreter the instance of the interpreter generating the warning (can be <code>null</code>)
	 */
	public void warning(String src, Throwable e, Node node, Interpreter interpreter);

	/**
	 * Sends a warning message to the engine.
	 *
	 * @param w the warning instance
	 */
	public void warning(CoreASMWarning w);


	/**
	 * Return <code>true</code> if the engine is in error mode
	 * or an error is occurred and it is going to be in error mode.
	 * 
	 */
	public boolean hasErrorOccurred();
}

