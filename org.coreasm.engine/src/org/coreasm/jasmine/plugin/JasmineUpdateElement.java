/*	
 * JasmineUpdateElement.java  	$Revision: 130 $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 * The element that represents JASMine commands
 * sends through the 'jasmChannel'.
 *   
 * @author Roozbeh Farahbod
 * @version $Revision: 130 $, Last modified: $Date: 2010-03-31 01:27:47 +0200 (Mi, 31 Mrz 2010) $
 */
public class JasmineUpdateElement extends JasmineAbstractUpdateElement {

	// type of actions
	enum Type {Create, Store, Invoke};
	
	public final Type type;
	public final List<Object> arguments;
	public final Element agent;
	public final ScannerInfo sinfo;
	private final Set<Element> agents;
	private final Set<ScannerInfo> sinfos;
	
	/** 
	 * Creates a new update element with the given type
	 * and arguments.
	 * 
	 * @param agent the contributing agent
	 * @param type type of update
	 * @param sinfo the location of the node in the specification that produced the update
	 * @param args the arguments 
	 */
	public JasmineUpdateElement(Element agent, Type type, ScannerInfo sinfo, Object... args) {
		this.type = type;
		List<Object> arguments = new ArrayList<Object>();
		for (Object arg: args)
			arguments.add(arg);
		this.arguments = Collections.unmodifiableList(arguments);
		this.agent = agent;
		Set<Element> set = new HashSet<Element>();
		set.add(agent);
		this.agents = Collections.unmodifiableSet(set);
		this.sinfo = sinfo;
		Set<ScannerInfo> iset = new HashSet<ScannerInfo>();
		iset.add(sinfo);
		this.sinfos = Collections.unmodifiableSet(iset);
	}
	
	/**
	 * The hash code depends on type of the update, the agent producing the update,
	 * and the arguments of the individual update commands. 
	 * 
	 * The hash code does not depend on the location of the node producing the update ({@link #sinfo}).
	 * 
	 */
	public int hashCode() {
		int result = (type.hashCode() + agent.hashCode()) * 8;
		switch (this.type) {
		case Create:
			for (int i=0; i < 2; i++)
				result = result + (arguments.get(i)==null?0:arguments.get(i).hashCode());
			break;
		
		case Store:
			result = result + arguments.get(1).hashCode();
			break;
			
		case Invoke:
			final Object first = arguments.get(0);
			result = result + (first==null?0:first.hashCode()) + arguments.get(2).hashCode();
		}
		return result;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof JasmineUpdateElement) {
			JasmineUpdateElement other = (JasmineUpdateElement)obj;
			if (other.agent.equals(this.agent) && other.type == this.type ) {
				switch (this.type) {
				case Create: {
					boolean result = arguments.size() == other.arguments.size() 
									&& arguments.get(0).equals(other.arguments.get(0)) 
									&& arguments.get(1).equals(other.arguments.get(1));
					if (result) {
						Object[] argsArray = arguments.toArray();
						Object[] otherArgsArray = other.arguments.toArray();
						for (int i=2; i < arguments.size(); i++)
							if (argsArray[i] != otherArgsArray[i]) {
								result = false;
								break;
							}
					} 
					return result;
				}
					
				case Store: {
					Object[] argsArray = arguments.toArray();
					Object[] otherArgsArray = other.arguments.toArray();

					// do not check for the equality of the Java object
					// since its value may have changed, so just check
					// to see if it is the same object (item 0)
					return argsArray[0] == otherArgsArray[0]
					       && argsArray[1].equals(otherArgsArray[1]) 
					       && argsArray[2] == otherArgsArray[2];
				}
					
				case Invoke: {
					// see above, but here the Java object is the second item 
					// i.e., item 1
					boolean result = arguments.size() == other.arguments.size() 
									&& ((arguments.get(0)==null)?(other.arguments.get(0)==null):arguments.get(0).equals(other.arguments.get(0)))
									&& arguments.get(1) == other.arguments.get(1);
					if (result) {
						Object[] argsArray = arguments.toArray();
						Object[] otherArgsArray = other.arguments.toArray();
						for (int i=2; i < arguments.size(); i++)
							if (argsArray[i] != otherArgsArray[i]) {
								result = false;
								break;
							}
					} 
					return result;
				}
				
				default: return false;
				}
			} else
				return false;
		} else
			return false;
	}
	
	/**
	 * Returns the location argument of CREATE and INVOKE
	 * deferred updates. Returns <code>null</code> if the 
	 * update type is not CREATE or INVOKE.
	 */
	public Location getCoreASMLocation() {
		if ((type == Type.Create || type == Type.Invoke) && arguments.get(0) != null)
			return (Location)arguments.get(0);
		else
			return null;
	}
	
	/**
	 * Returns the JObjectElement that the store update
	 * is being applied to. This method returns <code>null</code>
	 * if update type is not Store.
	 */
	public JObjectElement getStoreObject() {
		if (type == Type.Store)
			return (JObjectElement)arguments.get(0);
		else
			return null;
	}

	/**
	 * Returns the field name of the store update.
	 * This method returns <code>null</code>
	 * if update type is not Store.
	 */
	public String getStoreField() {
		if (type == Type.Store)
			return (String)arguments.get(1);
		else
			return null;
	}
	
	/**
	 * Returns the to-be-assigned value of the store update.
	 * This method returns <code>null</code>
	 * if update type is not Store.
	 */
	public Object getStoreValue() {
		if (type == Type.Store)
			return arguments.get(2);
		else
			return null;
	}

	public String toString() {
		return "JASMineUpdate:(" + this.type + ", " + arguments + ")";
	}

	@Override
	public Set<Element> getAgents() {
		return this.agents;
	}

	@Override
	public Set<ScannerInfo> getScannerInfos() {
		return this.sinfos;
	}
}
