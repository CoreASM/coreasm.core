package org.coreasm.engine.scheduler;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.NameElement;
import org.junit.Before;
import org.junit.Test;

public class DefaultSchedulingPolicyTest {

	private DefaultSchedulingPolicy policy;
	private Set<Element> set1;
	private Set<Element> emptyset;
	
	@Before
	public void setUp() throws Exception {
		policy = new DefaultSchedulingPolicy();
		set1 = new HashSet<Element>();
		for (int i=0; i < 5; i++)
			set1.add(new NameElement(String.valueOf(i)));
		emptyset = new HashSet<Element>();
	}

	@Test
	public void testGetNewSchedule() {
		checkIterator(policy.getNewSchedule(set1), 31, 80);
		checkIterator(policy.getNewSchedule(emptyset), 0, 0);
	}

	private void checkIterator(Iterator<Set<Element>> it, int count, int total) {
		int c = 0;
		int t = 0;
		while (it.hasNext()) {
			t += it.next().size();
			c++;
		}
		assertEquals(count, c);
		assertEquals(total, t);
	}
}
