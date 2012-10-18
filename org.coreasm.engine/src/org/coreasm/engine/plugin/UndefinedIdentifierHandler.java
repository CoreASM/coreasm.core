/*	
 * UndefinedIdentifierHandler.java 	$Revision: 243 $
 * 
 * Copyright (C) 2007 George Ma
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugin;

import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;

/** 
 * An interface for Interpreter Plugins that want to handle undefined identifiers
 * specially.
 *   
 * @author  George Ma
 * 
 */
public interface UndefinedIdentifierHandler {
    /**
     * Invoked by the interpreter when an undefined identifier is
     * encountered.
     * 
     * @param pos is the node representing the identifier
     * @param id is the identifier 
     * @param args is the ElementList following the identifier
     */
    public void handleUndefinedIndentifier(Interpreter interpreter, ASTNode pos, String id, List<? extends Element> args);
}
