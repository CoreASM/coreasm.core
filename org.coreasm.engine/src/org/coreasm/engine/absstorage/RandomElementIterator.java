package org.coreasm.engine.absstorage;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.coreasm.util.Tools;

public class RandomElementIterator implements Iterator<Element> {
	private final int size;
	private final List<Element> elements;
	private BitSet considered;
	private int numConsidered;
	private List<Element> remaining;
	
	public RandomElementIterator(Enumerable enumerable) {
		if (enumerable.supportsIndexedView()) {
			elements = enumerable.getIndexedView();
			if (enumerable.size() > 0)
				considered = new BitSet();
		}
		else {
			elements = null;
			remaining = new ArrayList<Element>(enumerable.enumerate());
		}
		size = elements.size();
	}

	@Override
	public boolean hasNext() {
		return considered != null || remaining != null && !remaining.isEmpty();
	}

	@Override
	public Element next() {
		if (!hasNext())
			throw new NoSuchElementException();
		if (considered != null) {
			if (numConsidered >= size / 2) {
				remaining = new ArrayList<Element>((size - 1) / 2 + 1);
				for (int i = 0; i < size; i++) {
					if (!considered.get(i))
						remaining.add(elements.get(i));
				}
				considered = null;
			}
			else {
				int i = Tools.randInt(size);
				while (considered.get(i))
					i = Tools.randInt(size);
				considered.set(i);
				numConsidered++;
				return elements.get(i);
			}
		}
		assert remaining != null;
		return remaining.remove(Tools.randInt(remaining.size()));
	}
}
