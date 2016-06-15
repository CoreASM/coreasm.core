package org.coreasm.engine.plugins.math;

import java.math.BigInteger;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.plugins.collection.AbstractSetElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide the powerset of an enumerable.
 *   
 * @author Michael Stegmaier
 *
 */

public class PowerSetElement extends Element implements Enumerable, Collection<Element> {
	protected static final Logger logger = LoggerFactory.getLogger(PowerSetElement.class);

	private final ArrayList<Element> elements;
	private String denotationalValue = null;
	
	private PowerSetIndexedView indexedView = null;
	
	public PowerSetElement(Enumerable baseSet) {
		Collection<? extends Element> base = baseSet.enumerate();
		if (base.size() >= Integer.SIZE - 1)
			logger.warn("MathPlugin: Powerset function over a collection of more than " + (Integer.SIZE - 2) + " elements.");
		elements = new ArrayList<Element>(base);
	}
	
	@Override
	public boolean contains(Element e) {
		return contains((Object)e);
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Enumerable))
			return false;
		for (Element element: ((Enumerable)o).enumerate()) {
			if (!elements.contains(element))
				return false;
		}
		return true;
	}
	
	@Override
	public Collection<Element> enumerate() {
		if (supportsIndexedView())
			return getIndexedView();
		return this;
	}

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		PowerSetElement other = (PowerSetElement) obj;
		return elements.equals(other.elements);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object obj: c) {
			if (!contains(obj))
				return false;
		}
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public int size() {
		if (elements.size() >= Integer.SIZE - 1)
			return Integer.MAX_VALUE;
		return 1 << elements.size();
	}

	@Override
	public Object[] toArray() {
		if (elements.size() >= Integer.SIZE - 1)
			throw new CoreASMError("The base set of this powerset contains " + elements.size() + " elements. But this operation is only supported for up to " + (Integer.SIZE - 2) + " elements.");
		Object[] result = new Object[size()];
		int i = 0;
		for (Element e : this) {
			result[i] = e;
			i++;
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (elements.size() >= Integer.SIZE - 2)
			throw new CoreASMError("The base set of this powerset contains " + elements.size() + " elements. But this operation is only supported for up to " + (Integer.SIZE - 2) + " elements.");
		final int size = size();
		if (a.length < size)
			return (T[])toArray();
		int i = 0;
		for (Element e : this) {
			a[i] = (T)e;
			i++;
		}
		if (i < a.length)
			return Arrays.copyOf(a, i);
        return a;
	}

	@Override
	public String toString() {
		String string = "";
		for (Element member : this)
			string += (string.isEmpty() ? " " : ", ") + member;
		return "{" + string + " }";
	}
	
	@Override
	public String denotation() {
		if (denotationalValue == null) {
			denotationalValue = "";
			for (Element e : elements)
				denotationalValue += (denotationalValue.isEmpty() ? " " : ", ") + e.denotation();
			denotationalValue = "P({" + denotationalValue + " })";
		}
		return denotationalValue;
	}
	
	@Override
	public Iterator<Element> iterator() {
		return new PowerSetIterator();
	}

	private class PowerSetIterator implements Iterator<Element>  {
		private final BigInteger powersetSize;
		private final long smallPowersetSize; 
		private final boolean overSizeBaseSet;
		private BigInteger bigIndex;
		private long smallIndex;
		
		public PowerSetIterator() {
			final int baseSize = elements.size();
			overSizeBaseSet = baseSize >= Long.SIZE - 1;
			if (overSizeBaseSet) {
				powersetSize = BigInteger.ONE.shiftLeft(baseSize);
				smallPowersetSize = -1L;
			} else {
				smallPowersetSize = 1L << baseSize;
				powersetSize = null;
			}
			bigIndex = BigInteger.ZERO;
			smallIndex = 0;
		}

		@Override
		public boolean hasNext() {
			return overSizeBaseSet && bigIndex.compareTo(powersetSize) < 0 || smallIndex < smallPowersetSize;  
		}

		@Override
		public Element next() {
			if (!hasNext())
				throw new NoSuchElementException();
			if (overSizeBaseSet) {
				PowerSetMember value = new PowerSetMember(bigIndex);
				bigIndex.add(BigInteger.ONE);
				return value;
			}
			return new PowerSetMember(smallIndex++);
		}
	}
	
	private class PowerSetMember extends AbstractSetElement {
		private final BitSet keys = new BitSet();
		
		public PowerSetMember(int index) {
			for (int i = elements.size() - 1; i >= 0; i--) {
				if ((index & (1 << i)) != 0)
					keys.set(i);
			}
		}
		
		public PowerSetMember(long index) {
			for (int i = elements.size() - 1; i >= 0; i--) {
				if ((index & (1L << i)) != 0)
					keys.set(i);
			}
		}
		
		public PowerSetMember(BigInteger index) {
			for (int i = elements.size() - 1; i >= 0; i--) {
				if ((index.and(BigInteger.ONE.shiftLeft(i))).compareTo(BigInteger.ZERO) != 0)
					keys.set(i);
			}
		}

		@Override
		public Collection<? extends Element> enumerate() {
			return keySet();
		}

		@Override
		public boolean contains(Element e) {
			return containsKey(e);
		}

		@Override
		public int size() {
			return keys.cardinality();
		}

		@Override
		public boolean supportsIndexedView() {
			return false;
		}

		@Override
		public List<Element> getIndexedView() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public AbstractSetElement getNewInstance(Collection<? extends Element> set) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<? extends Element> getSet() {
			return keySet();
		}

		@Override
		public Map<Element, Element> getMap() {
			return new AbstractMap<Element, Element>() {

				@Override
				public Set<Entry<Element, Element>> entrySet() {
					return new AbstractSet<Entry<Element,Element>>() {

						@Override
						public Iterator<Entry<Element, Element>> iterator() {
							return new Iterator<Entry<Element,Element>>() {
								private Iterator<Element> keyIterator = keySet().iterator();

								@Override
								public boolean hasNext() {
									return keyIterator.hasNext();
								}

								@Override
								public Entry<Element, Element> next() {
									final Element key = keyIterator.next();
									return new Entry<Element, Element>() {

										@Override
										public Element getKey() {
											return key;
										}

										@Override
										public Element getValue() {
											return BooleanElement.TRUE;
										}

										@Override
										public Element setValue(Element value) {
											throw new UnsupportedOperationException();
										}
									};
								}
							};
						}

						@Override
						public int size() {
							return PowerSetMember.this.size();
						}
					};
				}
			};
		}

		@Override
		public boolean isEmpty() {
			return keys.isEmpty();
		}

		@Override
		public boolean containsKey(Element key) {
			return keys.get(elements.indexOf(key));
		}

		@Override
		public boolean containsValue(Element value) {
			return BooleanElement.TRUE.equals(value) && !isEmpty() || BooleanElement.FALSE.equals(value);
		}

		@Override
		public Element get(Element key) {
			return BooleanElement.valueOf(containsKey(key));
		}

		@Override
		public Set<? extends Element> keySet() {
			return new AbstractSet<Element>() {

				@Override
				public Iterator<Element> iterator() {
					return new Iterator<Element>() {
						private int cursor = keys.nextSetBit(0);

						@Override
						public boolean hasNext() {
							return cursor >= 0 && cursor < Integer.MAX_VALUE;
						}

						@Override
						public Element next() {
							if (!hasNext())
								throw new NoSuchElementException();
							int i = cursor;
							cursor = keys.nextSetBit(cursor + 1);
							return elements.get(i);
						}
					};
				}
				
				@Override
				public boolean contains(Object o) {
					if (!(o instanceof Element))
						return false;
					return containsKey((Element)o);
				}

				@Override
				public int size() {
					return PowerSetMember.this.size();
				}
			};
		}

		@Override
		public Collection<? extends Element> values() {
			return Collections.unmodifiableSet(new HashSet<Element>(Arrays.asList((isEmpty() ? new Element[] { BooleanElement.FALSE } : new Element[] { BooleanElement.FALSE, BooleanElement.TRUE }))));
		}

		@Override
		public String toString() {
			String string = "";
			for (Element key : keySet())
				string += (string.isEmpty() ? " " : ", ") + key;
			return "{" + string + " }";
		}

		@Override
		public int hashCode() {
			return keys.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;
			PowerSetMember other = (PowerSetMember) obj;
			return keys.equals(other.keys);
		}
	}
	
	private class PowerSetIndexedView extends AbstractList<Element> implements List<Element> {
		private final int size = PowerSetElement.this.size();

		@Override
		public int size() {
			return size;
		}
		
		@Override
		public Element get(int index) {
			return new PowerSetMember(index);
		}
	}

	public List<Element> getIndexedView() throws UnsupportedOperationException {
		if (indexedView != null)
			return indexedView;
		return indexedView = new PowerSetIndexedView();
	}

	public boolean supportsIndexedView() {
		return elements.size() < Integer.SIZE - 1;
	}
	
	public boolean add(Element o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends Element> c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
}
