/*	
 * JasmineUtil.java 	$Revision: 9 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.jasmine.plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.plugins.collection.AbstractListElement;
import org.coreasm.engine.plugins.collection.AbstractMapElement;
import org.coreasm.engine.plugins.collection.AbstractSetElement;
import org.coreasm.engine.plugins.list.ListElement;
import org.coreasm.engine.plugins.map.MapElement;
import org.coreasm.engine.plugins.number.NumberElement;
import org.coreasm.engine.plugins.set.SetElement;
import org.coreasm.engine.plugins.string.StringElement;

/** 
 * Some utility functions of JASMine.
 *   
 * @author Roozbeh Farahbod
 * @version $Revision: 9 $, Last modified: $Date: 2009-01-28 10:03:22 +0100 (Mi, 28 Jan 2009) $
 */
public class JasmineUtil {

	/**
	 * Returns the Java class with the given name. If the given control API is
	 * not null and it has its own class loader, this method uses the API's class
	 * loader. Otherwise, the default loader is used.
	 * 
	 * @param x name of the class
	 * @param capi an instance of {@link ControlAPI}; can be <code>null</code>.
	 * @return the corresponding class
	 * 
	 * @throws ClassNotFoundException if a class with the given name cannot be loaded.
	 * 
	 * @see Class#forName(String)
	 * @see Class#forName(String, boolean, ClassLoader)
	 */
	public static Class<? extends Object> getJavaClass(String x, ClassLoader loader) throws ClassNotFoundException {
		if (loader != null)
			return Class.forName(x, true, loader);
		else
			return Class.forName(x);
	}

	/**
	 * Returns <code>true</code> if a class with the given name is available and can be loaded.
	 * 
	 * @see #getJavaClass(String, ControlAPI)
	 */
	public static boolean isJavaClassName(String x, ClassLoader loader) {
		try {
			getJavaClass(x, loader);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Converts the given Java object into a CoreASM element according
	 * to the following rules: 
	 * <ul>
	 * <li>{@link Boolean} is converted to {@link BooleanElement}.</li>
	 * <li>{@link Number} is converted to {@link NumberElement}.</li>
	 * <li>{@link String} is converted to {@link StringElement}.</li>
	 * <li>{@link Set} is converted to {@link AbstractSetElement}, and all its members are also recursively converted.</li>
	 * <li>{@link List} is converted to {@link AbstractListElement}, and all its members are also recursively converted.</li>
	 * <li>{@link Map} is converted to {@link AbstractMapElement}, and all its key-value pairs are also recursively converted.</li>
	 * <li>All other objects are wrapped in a {@link JObjectElement}. 
	 * </ul>
	 * 
	 * @param obj a Java object
	 * @return CoreASM counterpart of <code>obj</code>.
	 */
	public static Element toCoreASM(Object obj) {
		if (obj == null)
			return Element.UNDEF;
		else
		if (obj instanceof Boolean)
			return BooleanElement.valueOf((Boolean)obj);
		else
		if (obj instanceof Number) 
			return NumberElement.getInstance(((Number)obj).doubleValue());
		else
		if (obj instanceof String)
			return new StringElement((String)obj);
		else
		if (obj instanceof Set) {
			Set<Element> tempSet = new HashSet<Element>();
			for (Object sm: (Set)obj)
				tempSet.add(toCoreASM(sm));
			return new SetElement(tempSet);
		}
		else
		if (obj instanceof List) {
			List<Element> tempList = new ArrayList<Element>();
			for (Object lm: (List)obj)
				tempList.add(toCoreASM(lm));
			return new ListElement(tempList);
		}
		else
		if (obj instanceof Map) {
			Map<Element, Element> tempMap = new HashMap<Element, Element>();
			for (Object pair: ((Map)obj).entrySet()) {
				// TODO this needs to be tested
				Entry pairEntry = (Entry)pair;
				tempMap.put(toCoreASM(pairEntry.getKey()), toCoreASM(pairEntry.getValue()));
			}
			return new MapElement(tempMap);
		} 
		else 
			return new JObjectElement(obj);
	}

	/**
	 * Converts the given element into a Java object. If the given
	 * element is an instance of {@link JObjectElement}, its inner
	 * object is returned. Otherwise, the element is converted according
	 * to the following rules: 
	 * <ul>
	 * <li>{@link BooleanElement} is converted to {@link Boolean}.</li>
	 * <li>{@link NumberElement} is converted to {@link Integer}, {@link Long}, or {@link Double}.</li>
	 * <li>{@link StringElement} is converted to {@link String}.</li>
	 * <li>{@link AbstractSetElement} is converted to {@link Set}, and all its members are also recursively converted.</li>
	 * <li>{@link AbstractListElement} is converted to {@link List}, and all its members are also recursively converted.</li>
	 * <li>{@link AbstractMapElement} is converted to {@link Map}, and all its key-value pairs are also recursively converted.</li>
	 * </ul>
	 * 
	 * Otherwise, it returns the same element <i>e</i>.
	 * 
	 * @param e CoreASM element to be converted
	 * @return Java counterpart of e
	 */
	public static Object toJava(Element e) {
		if (e.equals(Element.UNDEF))
			return null;
		
		else
		if (e instanceof JObjectElement)
			return ((JObjectElement)e).object;
		
		else
		if (e instanceof BooleanElement)
			return ((BooleanElement)e).getValue();
		
		else
		if (e instanceof NumberElement) { 
			double d = ((NumberElement)e).getValue();
			Double D = d;
			if (d == Math.floor(d)) { 
				// if d is an integer
				/*
				if (d > Byte.MIN_VALUE && d < Byte.MAX_VALUE)
					return new Byte(D.byteValue());
				else
				if (d > Short.MIN_VALUE && d < Short.MAX_VALUE)
					return new Short(D.shortValue());
				else
				*/
				if (d > Integer.MIN_VALUE && d < Integer.MAX_VALUE)
					return new Integer(D.intValue());
				else
				if (d > Long.MIN_VALUE && d < Long.MAX_VALUE)
					return new Long(D.longValue());
				else
					return D;
			} else {
				// if d is not an integer
				// TODO Do we need to convert it to float? Or does it make things complicated?
				return D;
			}
		}
		
		else
		if (e instanceof StringElement)
			return ((StringElement)e).getValue();
		
		else
		if (e instanceof AbstractSetElement) {
			Set<Object> result = new HashSet<Object>();
			for (Element sm: ((AbstractSetElement)e).getSet())
				result.add(toJava(sm));
			return result;
		}
		
		else
		if (e instanceof AbstractListElement) {
			List<Object> result = new ArrayList<Object>();
			for (Element lm: ((AbstractListElement)e).getList())
				result.add(toJava(lm));
			return result;
		}
		else
		if (e instanceof AbstractMapElement) {
			Map<Object,Object> result = new HashMap<Object, Object>();
			for (Entry<Element,Element> mm: ((AbstractMapElement)e).getMap().entrySet())
				result.put(toJava(mm.getKey()), toJava(mm.getValue()));
			return result;
		}
		else
			return e;
	}
	
	/**
	 * If the given element is already an instnace of 
	 * {@link JObjectElement}, it returns <code>element</code>;
	 * otherwise, it converts the given element into a Java object 
	 * using {@link #toJava(Element)} and returns the 
	 * result wrapped in an instance of {@link JObjectElement}.
	 *  
	 * @param element CoreASM element
	 * @return JObject pointing to a Java version of the given element
	 */
	public static JObjectElement javaValue(Element element) {
		if (element instanceof JObjectElement)
			return (JObjectElement)element;
		else
			return new JObjectElement(toJava(element));
	}
	
	/**
	 * Converts the given Java object into a 
	 * CoreASM element using {@link #toCoreASM(Object)}.
	 * 
	 * @param obj
	 * @return
	 */
	public static Element asmValue(Object obj) {
		return toCoreASM(obj);
		// TODO this is inconsistent with jValue(Element)
	}
	
	/**
	 * Tries to cast the type of the given value to match
	 * the given type, for those cases that the castings  
	 * are not done automatically by JVM (e.g., numbers).
	 * 
	 * @param field the field to store the value in
	 * @param value the new value
	 */
	public static Object specialTypeCast(Class<? extends Object> clazz, Object value) {
		if (value instanceof Number) {
			Number n = (Number)value;
			if (clazz.equals(Byte.class) || clazz.equals(Byte.TYPE))
				value = n.byteValue();
			if (clazz.equals(Short.class) || clazz.equals(Short.TYPE))
				value = n.shortValue();
			if (clazz.equals(Integer.class) || clazz.equals(Integer.TYPE))
				value = n.intValue();
			if (clazz.equals(Long.class) || clazz.equals(Long.TYPE))
				value = n.longValue();
			if (clazz.equals(Float.class) || clazz.equals(Float.TYPE))
				value = n.floatValue();
			if (clazz.equals(Double.class) || clazz.equals(Double.TYPE))
				value = n.doubleValue();
		}
		return value;
	}

	/**
	 * Tries to cast the type of the given arguments to match
	 * the given types, for those cases that the castings  
	 * are not done automatically by JVM (e.g., numbers).
	 * 
	 * @param expectedClasses expected types
	 * @param arguments actual arguments
	 */
	public static Object[] adjustArgumentTypes(Class<? extends Object>[] expectedClasses, Object[] arguments) {
		for (int i=0; i < arguments.length; i++) {
			arguments[i] = specialTypeCast(expectedClasses[i], arguments[i]);
		}
		return arguments;
	}

	/** 
	 * Checks if the type of the given value is a subclass of 
	 * the given class. This method assumes inheritance relationship 
	 * between different classes of Numbers.
	 * 
	 * {@link Integer} < {@link Long} < {@link Float} < {@link Double}
	 * 
	 * @param superClass the required class
	 * @param subClass class of <code>value</code> (for performance issues)
	 * @param value the value that should match into the required class 
	 */
	public static boolean classMatches(Class<? extends Object> superClass, Class<? extends Object> subClass, Object value) {
		if (value == null)
			return true;	// null fits in any class
		
		assert subClass.equals(value.getClass());
		
		// if the value is Integer
		if (subClass.equals(Integer.class))
			if (superClass.equals(Integer.TYPE) || superClass.equals(Integer.class)
					|| superClass.equals(Long.TYPE) || superClass.equals(Long.class)
					|| superClass.equals(Float.TYPE) || superClass.equals(Float.class)
					|| superClass.equals(Double.TYPE) || superClass.equals(Double.class))
				return true;
		
		// if the value is Long
		if (subClass.equals(Long.class))
			if (superClass.equals(Long.TYPE) || superClass.equals(Long.class)
					|| superClass.equals(Float.TYPE) || superClass.equals(Float.class)
					|| superClass.equals(Double.TYPE) || superClass.equals(Double.class))
				return true;
		
		// if the value is Float
		if (subClass.equals(Float.class))
			if (superClass.equals(Float.TYPE) || superClass.equals(Float.class)
					|| superClass.equals(Double.TYPE) || superClass.equals(Double.class))
				return true;
		
		// if the value is Double
		if (subClass.equals(Double.class)) {
			if (superClass.equals(Double.TYPE) || superClass.equals(Double.class))
				return true;
			if (superClass.equals(Float.TYPE) || superClass.equals(Float.class)) {
				double d = ((Double)value).doubleValue();
				if (d < Float.MAX_VALUE && d > Float.MIN_VALUE)
					return true;
			} 
		}
		
		try {
			subClass.asSubclass(superClass);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks if the types of the given values are respectively subclasses of 
	 * the given classes. This method uses {@link #classMatches(Class, Class, Object)}.
	 * 
	 * @param superClasses the list of required classes
	 * @param subClasses classes of <code>values</code> (needed for performance)
	 * @param values the actual values that should match the required classes
	 */
	public static boolean classesMatch(Class<? extends Object>[] superClasses, Class<? extends Object>[] subClasses, Object[] values) {
		if (superClasses.length != subClasses.length)
			return false;

		for (int i=0; i < superClasses.length; i++) 
			if (!classMatches(superClasses[i], subClasses[i], values[i]))
				return false;

		return true;
	}
}
