/*	
 * FunctionElement.java 	1.1 	$Revision: 243 $
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

import java.util.Collections;
import java.util.List;
import java.util.Set;

/** 
 *	An abstract class that implements the elements 
 *  of the FUNCTION_ELEMENT universe.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public abstract class FunctionElement extends Element {

	/**
	 * Different classes of ASM functions.
	 */
	public static enum FunctionClass {
		fcMonitored, fcControlled, fcOut, fcStatic, fcDerived
	};
									  
	/**
	 * The default value of this function. 
	 * By default, this value is <code>Element.UNDEF</code>.
	 */
    protected final Element defaultValue;
	
    /**
     * Class of this function.
     * 
     * @refer ASM Book, Section 2.2.3
     */
    private FunctionClass fClass = FunctionClass.fcControlled;
    
	/**
	 * The signature of this function.
	 */
	private Signature signature;

	/**
	 * Creates a new function. 
	 * It's default value
	 * will be <code>Element.UNDEF</code> and signature 
	 * will be <code>null</code>.
	 * 
	 */
	public FunctionElement() {
		super();
		signature = null;
		defaultValue = Element.UNDEF;
	}

	/**
	 * Creates a new function with the default value.
	 * 
	 */
	public FunctionElement(Element defaultValue) {
		super();
		signature = null;
		this.defaultValue = defaultValue;
	}

	/**
	 * Returns the signature of this function.
	 * 
	 * @return the signature
	 */
	public Signature getSignature() {
		return signature;
	}
	
	/**
	 * Sets a new signature for this function.
	 * 
	 * @param sig the new signature
	 */
	public void setSignature(Signature sig) {
		this.signature = sig;
	}
	
	/**
	 * Returns the class of this function.
	 * 
	 * @return Returns the class of this function.
	 * @see FunctionClass
	 */
	public FunctionClass getFClass() {
		return fClass;
	}

	/**
	 * Sets the class of this function
	 * 
	 * @param fClass the new class to set.
	 * @see FunctionClass
	 */
	public void setFClass(FunctionClass fClass) {
		this.fClass = fClass;
	}

	public String getBackground() {
		return FunctionBackgroundElement.FUNCTION_BACKGROUND_NAME;
	}
	
	/**
	 * Returns the value of this function for the given
	 * list of arguments. 
	 * 
	 * @param args list of arguments
	 * @return the assigned value to the arguments, or 
	 * <code>defaultValue</code> if there is no value
	 * assigned to the given arugments.
	 * @see #defaultValue
	 */
	public abstract Element getValue(List<? extends Element> args);

	/**
	 * If supported, this method returns the current range of this function as a
	 * set of elements.
	 * 
	 * If not supported, this method should return 
	 * <code>Collections.emptySet()</code>.
	 */
	public Set<? extends Element> getRange() {
		return Collections.emptySet();
	}
	
	/**
	 * Sets the value of this function for the given 
	 * arguments only if this function is modifiable.
	 * 
	 * Implementations of this method should either check
	 * the function class (i.e., <code>this.getFClass()</code>) 
	 * or call <code>super.setValue(args, value)</code> before
	 * setting any value. 
	 * 
	 * @param args arguments (list of Elements)
	 * @param value the return value
	 */
	public void setValue(List<? extends Element> args, Element value) throws UnmodifiableFunctionException {
		if (!isModifiable()) 
			throw new UnmodifiableFunctionException("Cannot set the value of this function.");
	}
	
	/**
	 * If supported, returns the set of all the locations for 
	 * which this function has a value other than
	 * <code>undef</code>. As this function does not
	 * keep its name, a name should be passed to this method.
	 *  
	 * If not supported, this method should return 
	 * <code>Collections.emptySet()</code>.
	 * 
	 * @param name the function name that should be used in the location
	 * @return <code>Set&gt;Location&lt;</code>
	 * @see Location
	 */
	public Set<Location> getLocations(String name) {
		return Collections.emptySet();
	}
	
	/**
	 * Returns <code>true</code> if this function is either
	 * a <i>controlled</i> function or an <i>out</i> function.
	 * 
	 * @see FunctionElement#getFClass()
	 */
	public final boolean isModifiable() {
		return (fClass.equals(FunctionClass.fcControlled) 
				|| fClass.equals(FunctionClass.fcOut));
	}
	
	/**
	 * Returns <code>true</code> if the value of this function
	 * is readable; i.e., if this function is not
	 * an <i>out</i> function.
	 * 
	 * @see FunctionElement#getFClass()
	 */
	public final boolean isReadable() {
		return !fClass.equals(FunctionClass.fcOut);
	}
	
	@Override
	public String toString() {
		return "function-element";
	}

}

