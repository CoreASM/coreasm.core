/*	
 * XMLSerializable.java 
 * 
 * Copyright (C) 2010 Roozbeh Farahbod
 *
 * Last modified by $Author$ on $Date$.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.absstorage;

/**
 * This interface is introduced to provide a unified way of serializing 
 * CoreASM elements to and from XML elements. 
 * 
 * @author Roozbeh Farahbod
 *
 */
public interface XMLSerializable {

	/** 
	 * Writes the CoreASM element to an XML element. 
	 */
	public org.w3c.dom.Element toXML();
	
	/** 
	 * Reads an element from the given XML element.
	 * 
	 * @param xmlElement an XML element representing an instance of this CoreASM element type
	 * @return the created CoreASM element
	 * @throws IllegalArgumentException
	 */
	public Element fromXML(org.w3c.dom.Element xmlElement) throws IllegalArgumentException;
}
