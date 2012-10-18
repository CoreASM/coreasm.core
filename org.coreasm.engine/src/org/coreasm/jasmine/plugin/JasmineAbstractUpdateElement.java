/*	
 * JasmineAbstractUpdateElement.java  	$Revision: 130 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-03-31 01:27:47 +0200 (Mi, 31 Mrz 2010) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.jasmine.plugin;

import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * Super class of JASMine update elements.
 *   
 * @author Roozbeh Farahbod
 * @version $Revision: 130 $, Last modified: $Date: 2010-03-31 01:27:47 +0200 (Mi, 31 Mrz 2010) $
 */
public abstract class JasmineAbstractUpdateElement extends Element {
	
	/**
	 * Returns the set of agents that contributed to this update.
	 */
	public abstract Set<Element> getAgents();
	
	/**
	 * Returns the set of locations in the specification that produced this update.
	 */
	public abstract Set<ScannerInfo> getScannerInfos();

}
