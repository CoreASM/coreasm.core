/*
 * PowerSetElement.java 		$Revision: 11 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-01-28 11:03:35 +0100 (Mi, 28 Jan 2009) $ by $Author: rfarahbod $
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.compiler.plugins.math.include;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.coreasm.engine.plugins.set.SetElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;

/**
 * Provide the powerset of an enumerable.
 *   
 * @author Roozbeh Farahbod
 * @version Last modified on $Date: 2009-01-28 11:03:35 +0100 (Mi, 28 Jan 2009) $
 *
 */

public class PowerSetElement extends Element implements Enumerable, Collection<Element> {

	/**
	 * The maximum size
	 */
	public static final int MAX_SIZE = 31;
	
	//protected static final Logger logger = LoggerFactory.getLogger(PowerSetElement.class);

	private final ArrayList<Element> elements;
	private Set<Element> elementsSet = null;
	private String denotationalValue = null;
	
	/**
	 * Constructs a new powerset
	 * @param baseSet The base set
	 */
	public PowerSetElement(Enumerable baseSet) {
		Collection<? extends Element> base = baseSet.enumerate();
		if (base.size() > MAX_SIZE)
			System.out.println(
					"MathPlugin: Powerset function over a collection of more than " + MAX_SIZE + " elements.");
		elements = new ArrayList<Element>(base);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.Enumerable#contains(org.coreasm.engine.absstorage.Element)
	 */
	public boolean contains(Element e) {
		// Make sure you change both contians methods
		return containsElement(e);
	}

	public boolean contains(Object o) {
		// Make sure you change both contians methods
		return containsElement(o);
	}

	/*
	 * Single implementation for both 
	 * contains(Element) and contains(Object)
	 */
	private boolean containsElement(Object o) {
		if (o instanceof Enumerable) {
			for (Element element: ((Enumerable)o).enumerate()) 
				if (!elements.contains(element))
					return false;
			return true;
		} else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.Enumerable#enumerate()
	 */
	public Collection<Element> enumerate() {
		return this;
	}

	/**
	 * @return the base set of this powerset element
	 */
	public Set<Element> getBaseSet() {
		if (elementsSet == null)
			elementsSet = new HashSet<Element>(elements);
		return elementsSet;
	}
	
	@Override
	public boolean equals(Object anElement) {
		if (anElement instanceof PowerSetElement) {
			return this.getBaseSet().equals(((PowerSetElement)anElement).getBaseSet());
		} else
			if (anElement instanceof Enumerable) {
				return this.enumerate().equals(((Enumerable)anElement).enumerate());
			} else
				return super.equals(anElement);
	}

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	public boolean add(Element o) {
		throw new UnsupportedOperationException("Cannot add an element to a powerset value.");
	}

	public boolean addAll(Collection<? extends Element> c) {
		throw new UnsupportedOperationException("Cannot add an element to a powerset value.");
	}

	public void clear() {
		throw new UnsupportedOperationException("Cannot clear a powerset.");
	}

	public boolean containsAll(Collection<?> c) {
		for (Object obj: c)
			if (!contains(obj))
				return false;
		return true;
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public Iterator<Element> iterator() {
		return new PowerSetIterator(elements);
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Cannot remove an element from powerset value.");
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Cannot remove an element from powerset value.");
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("PowerSet.retainAll(...) is not supported.");
	}

	public int size() {
		if (elements.size() > MAX_SIZE) {
			System.out.println(
					"MathPlugin: Cannot return the size of the Powerset function over a collection of more than " + MAX_SIZE + " elements.");
			return Integer.MAX_VALUE;
		} else 
			return (int)Math.pow(2, elements.size());
	}

	public Object[] toArray() {
		Object[] result = new Object[this.size()];
		int i = 0;
		for (Element e : this) {
			result[i] = e;
			i++;
		}
//		throw new UnsupportedOperationException("PowerSet.toArray(...) is not supported.");
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int size = this.size();
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		int i = 0;
		for (Element e : this) {
			a[i] = (T)e;
			i++;
		}
        if (a.length > size)
            a[size] = null;
        return a;
	}

	/**
	 * Expands the powerset to a string
	 * representation.
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		for (Element e: this)
			result.append(", " + e.toString());
		if (result.length() > 0)
			return "{" + result.toString().substring(2) + "}";
		else
			return "{}";
	}
	
	public String denotation() {
		if (denotationalValue == null) {
			denotationalValue = "P({";
			for (Element e: elements)
				denotationalValue = denotationalValue + e.denotation() + ", ";
			if (elements.size() > 0)
				denotationalValue = denotationalValue.substring(0, denotationalValue.length() -2);
			denotationalValue = denotationalValue + "})";
		}
		return denotationalValue;
	}

	/**
	 * The iterator for powerset elements. It 
	 * creates the next element in every call to the
	 * <code>next()</code> function.
	 *
	 */
	private static class PowerSetIterator implements Iterator<Element>  {

		private final ArrayList<Element> baseList;
		private final int baseSize;
	
		// having two indices for large base sets and small base sets
		// is just to increase the performance on small base sets
		private final BigInteger powersetSize;
		private final int smallPowersetSize; 
		private final boolean overSizeBaseSet;
		private BigInteger bigIndex;
		private long smallIndex;
		
		public PowerSetIterator(ArrayList<Element> baseList) {
			this.baseList = baseList;
			this.baseSize = baseList.size();
			overSizeBaseSet = (baseSize > 63);
			if (overSizeBaseSet) {
				powersetSize = (new BigInteger("2")).pow(baseSize);
				smallPowersetSize = Integer.MAX_VALUE;
			} else {
				smallPowersetSize = (int)Math.pow(2, baseSize);
				powersetSize = null;
			}
			bigIndex = BigInteger.ZERO;
			smallIndex = 0;
			
		}

		public boolean hasNext() {
			if (overSizeBaseSet)
				return bigIndex.compareTo(powersetSize) < 0;
			else
				return smallIndex < smallPowersetSize;  
		}

		public Element next() {
			if (hasNext()) {
				Collection<Element> resultSet = new ArrayList<Element>();

				if (overSizeBaseSet) {
					// if the base set is too large then 
					// we need to use BigIntegers to compute the next element of 
					// the powerset
					BigInteger indices = bigIndex;
					int ci = 0;
					while (indices.compareTo(BigInteger.ZERO) > 0) {
						if (indices.and(BigInteger.ONE).compareTo(BigInteger.ZERO) > 0)
							resultSet.add(baseList.get(ci));
						indices.shiftRight(1);
						ci++;
					}
					bigIndex.add(BigInteger.ONE);
				} else {
					// if the base set has less than or equal to 32 elements
					// then we can use basic integers to compute the next lement
					// of the powerset function
					long indices = smallIndex;
					int ci = 0;
					while (indices > 0) {
						if ((indices & 1) > 0) 
							resultSet.add(baseList.get(ci));
						indices = indices >> 1;
						ci++;
					}
					smallIndex++;
				}
				
				Set<Element> result = new HashSet<Element>();
				for (Element e: resultSet)
					result.add(e);
				return new SetElement(result);

			} else
				throw new NoSuchElementException("No more elements in the powerset.");
		}

		public void remove() {
			throw new UnsupportedOperationException("Cannot remove elements from powerset iterators.");
		}
		
	}
	
	@SuppressWarnings("unused")
	private static class PowerSetMember extends SetElement {

		private final Collection<Element> value;
		
		public PowerSetMember(Collection<Element> value) {
			this.value = value;
		}
		
		public boolean contains(Element e) {
			return value.contains(e);
		}

		public Collection<Element> enumerate() {
			return Collections.unmodifiableCollection(value);
		}
	
		public String toString() {
			StringBuffer result = new StringBuffer();
			for (Element e: value)
				result.append(", " + e.toString());
			if (result.length() > 0)
				return "{" + result.toString().substring(2) + "}";
			else
				return "{}";
		}
	}

	public List<Element> getIndexedView() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Powerset is not an indexed collection.");
	}

	public boolean supportsIndexedView() {
		return false;
	}
}
