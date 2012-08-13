/*	
 * AbstractMultiset.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Abstract implementation of <code>Multiset</code> with <code>Map</code>s.
 *   
 *  @author  Roozbeh Farahbod
 *  
 *  @see org.coreasm.util.Multiset
 */
public abstract class AbstractMultiset<E> implements Multiset<E> {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractMultiset.class);

	/** main data structure */
	protected Map<E,Integer> map;
	
	/**
	 * Creates a new abstract multiset. This constructor
	 * calls the <code>createMap()</code> method to create
	 * a custom map as its sole data structure. 
	 * 
	 * @see AbstractMultiset#createMap()
	 */
	public AbstractMultiset() {
		map = createMap();
	}
	
	/**
	 * Creates a new abstract multiset and adds the
	 * given elements to it. This constructor
	 * calls the default constructor first. 
	 * 
	 * @see AbstractMultiset#AbstractMultiset()
	 */
	public AbstractMultiset(E ... elements){ 
		this();
		for (E e: elements)
			this.add(e);
	}

	/**
	 * Creates a new abstract multiset and adds all the
	 * elements of collection <code>c</code> to it. This constructor
	 * first calls the default constructor. 
	 * 
	 * @see AbstractMultiset#AbstractMultiset()
	 * @see AbstractMultiset#addAll(Collection)
	 */
	public AbstractMultiset(Collection<? extends E> c) {
		this();
		this.addAll(c);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.util.Multiset#multiplicity(E)
	 */
	public int multiplicity(Object element) {
		Integer i = map.get(element);
		if (i != null)
			return i.intValue();
		else
			return 0;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	public int size() {
		int i = 0;
		for (Integer i_s: map.values())
			i += i_s.intValue(); 
		return i;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return map.containsKey(o);		
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<E> iterator() {
		return new Itr();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		Object[] a = new Object[this.size()];
		int i = 0;
		for (E e: map.keySet()) {
			for (int j=0; j < map.get(e).intValue(); j++) {
				a[i] = e;
				i++;
			}
		}
		return a;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int size = this.size();
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
            		a.getClass().getComponentType(), size);
		int i = 0;
		for (E e: map.keySet()) {
			for (int j=0; j < map.get(e).intValue(); j++) {
				a[i] = (T)e;
				i++;
			}
		}
        return a;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#add(E)
	 */
	public boolean add(E o) {
		map.put(o, new Integer(this.multiplicity(o) + 1));
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		int m = this.multiplicity(o);
		if (m == 0) {
			// remove zero counts
			if (map.containsKey(o))
				map.remove(o);
			return false;
		}
		if (m > 1) 
			map.put((E)o, new Integer(m -1));
		if (m == 1)
			map.remove(o);
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return map.keySet().containsAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends E> c) {
		int size = this.size();
		for (E e: c) 
			this.add(e);
		return size != this.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		int size = this.size();
		for (Object o: c) 
			this.remove(o);
		return size != this.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		logger.warn("AbstractMultiset.retainAll(c) is not tested.");
		boolean pass = false;  // is this multiset object changed?
		for (E e: map.keySet())
			if (!c.contains(e)) {
				pass = true;
				map.remove(e);
			}
		return pass;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * @see Multiset#toSet()
	 */
	public abstract Set<E> toSet();

	public String toString() {
		StringBuffer str = new StringBuffer();
		
		str.append("{| ");
		for (E e: this)
			str.append(e.toString() + ", ");
		if (!isEmpty()) 
			str.replace(str.length() - 2, str.length() - 1, "");
		str.append("|}");
		
		return str.toString();
	}
	
	/**
	 * Returns a sub-instance of <code>Map<E,Integer></code>. This
	 * is used by the constructor of this class to create the map, 
	 * which is the main data structure of this class.
	 */
	protected abstract Map<E,Integer> createMap();
	
	private class Itr implements Iterator<E> {

		//Iterator<E> baseItr = null;
		Iterator<Entry<E, Integer>> baseItr = null;
		E currentElement = null;
		E lastElementFetched = null;
		int currentElementRemains = 0;
		
		/**
		 * Creates a new iterator based on the given iterator
		 * @param base
		 */
		public Itr() {
			baseItr = map.entrySet().iterator();
		}
		
		/**
		 * @see Iterator#hasNext()
		 */
		public boolean hasNext() {
			// If there are more of the current element left, return true
			if (currentElementRemains > 0)
				return true;
			else
				return baseItr.hasNext();
		}

		/**
		 * @see Iterator#next()
		 */
		public E next() {
			if (!this.hasNext())
				throw new java.util.NoSuchElementException("next() has no more element.");
			
			// if this is the first call to next(), 
			// or the last element is passed over, fetch a new element
			if (currentElement == null || currentElementRemains == 0) {
				Entry<E, Integer> entry = baseItr.next();
				currentElement = entry.getKey();
				currentElementRemains = entry.getValue().intValue() - 1;
			} else 
				// otherwise, reduce the remain count of the current element 
				currentElementRemains--;

			lastElementFetched = currentElement;
			return currentElement;
		}

		/**
		 * @see Iterator#remove()
		 */
		public void remove() {
			if (lastElementFetched != null) {
				map.remove(lastElementFetched);
				lastElementFetched = null;
			} else 
				throw new IllegalStateException("Call next() prior to call remove().");
		}
		
	}
}
