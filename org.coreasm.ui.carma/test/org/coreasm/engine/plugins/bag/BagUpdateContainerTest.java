/*	
 * BagUpdateContainerTest.java  	$Revision: 7 $
 * 
 * Copyright (C) 2008 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2009-01-28 07:54:19 +0100 (Mi, 28 Jan 2009) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.bag;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.plugins.bag.BagUpdateElement.BagUpdateType;
import org.coreasm.engine.plugins.number.NumberElement;
import org.coreasm.engine.plugins.string.StringElement;
import org.coreasm.util.HashMultiset;
import org.coreasm.util.Multiset;
import org.junit.Before;
import org.junit.Test;

/** 
 * Testing the BagUpdateContainer and its composition/aggregation algorithms.
 *   
 * @author  Roozbeh Farahbod
 * @version $Revision: 7 $, Last modified: $Date: 2009-01-28 07:54:19 +0100 (Mi, 28 Jan 2009) $
 */
public class BagUpdateContainerTest {

	NumberElement n1;
	NumberElement n5;
	NumberElement n6;
	NumberElement n7;
	NumberElement n43;
	NumberElement n52;
	StringElement strHi;
	StringElement strYou;
	Element e;
	List<Element> list;
	BagElement bag1;
	BagElement bag2;
	BagUpdateContainer composed1;
	BagUpdateContainer composed2;
	BagUpdateContainer composed3;
	BagUpdateContainer composed4;
	BagUpdateContainer updates1;
	BagUpdateContainer updates2;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		n1 = NumberElement.getInstance(1);
		n5 = NumberElement.getInstance(5);
		n6 = NumberElement.getInstance(6);
		n7 = NumberElement.getInstance(7);
		n43 = NumberElement.getInstance(43);
		n52 = NumberElement.getInstance(52);
		strHi = new StringElement("Hi");
		strYou = new StringElement("You");
		e = new Element();
		list = new ArrayList<Element>();
		list.add(n43);
		list.add(strHi);
		list.add(n43);
		bag1 = new BagElement(list);
		
		list = new ArrayList<Element>();
		list.add(n43);
		list.add(n43);
		bag2 = new BagElement(list);

		BagUpdateElement bue11 = new BagUpdateElement(BagUpdateType.REMOVE, n52);
		BagUpdateElement bue12 = new BagUpdateElement(BagUpdateType.REMOVE, n43);
		BagUpdateElement bue13 = new BagUpdateElement(BagUpdateType.ADD, strYou);
		Multiset<BagUpdateElement> set1 = new HashMultiset<BagUpdateElement>();
		set1.add(bue11);
		set1.add(bue12);
		set1.add(bue13);
		BagUpdateContainer buc1 = new BagUpdateContainer(set1);
		
		BagUpdateElement bue21 = new BagUpdateElement(BagUpdateType.ADD, n52);
		BagUpdateElement bue22 = new BagUpdateElement(BagUpdateType.ADD, n43);
		BagUpdateElement bue23 = new BagUpdateElement(BagUpdateType.ADD, n43);
		BagUpdateElement bue24 = new BagUpdateElement(BagUpdateType.REMOVE, strHi);
		Multiset<BagUpdateElement> set2 = new HashMultiset<BagUpdateElement>();
		set2.add(bue21);
		set2.add(bue22);
		set2.add(bue23);
		set2.add(bue24);
		BagUpdateContainer buc2 = new BagUpdateContainer(set2);

		composed1 = BagUpdateContainer.compose(buc1, buc2);
		composed2 = BagUpdateContainer.compose(buc2, buc1);

		BagUpdateElement bue31 = new BagUpdateElement(BagUpdateType.ADD, n1);
		BagUpdateElement bue32 = new BagUpdateElement(BagUpdateType.REMOVE, n5);
		List<BagUpdateElement> list1 = new ArrayList<BagUpdateElement>();
		list1.add(bue31);
		list1.add(bue32);
		BagUpdateContainer buc21 = new BagUpdateContainer(list1);
		
		BagUpdateElement bue41 = new BagUpdateElement(BagUpdateType.REMOVE, n1);
		BagUpdateElement bue42 = new BagUpdateElement(BagUpdateType.REMOVE, n6);
		List<BagUpdateElement> list2 = new ArrayList<BagUpdateElement>();
		list2.add(bue41);
		list2.add(bue42);
		BagUpdateContainer buc22 = new BagUpdateContainer(list2);
		
		BagUpdateElement bue51 = new BagUpdateElement(BagUpdateType.ADD, n7);
		BagUpdateElement bue52 = new BagUpdateElement(BagUpdateType.ADD, n6);
		BagUpdateElement bue53 = new BagUpdateElement(BagUpdateType.REMOVE, n7);
		List<BagUpdateElement> list3 = new ArrayList<BagUpdateElement>();
		list3.add(bue51);
		list3.add(bue52);
		list3.add(bue53);
		BagUpdateContainer buc23 = new BagUpdateContainer(list3);
		
		composed3 = BagUpdateContainer.compose(BagUpdateContainer.compose(buc21, buc22), buc23);
		
		Set<BagAbstractUpdateElement> set3 = new HashSet<BagAbstractUpdateElement>();
		set3.add(buc21);
		set3.add(buc22);
		set3.add(buc23);
		set3.add(composed2);
		
		updates1 = new BagUpdateContainer(set3);
		
		composed4 = BagUpdateContainer.compose(buc21, buc2);

		Set<BagAbstractUpdateElement> set4 = new HashSet<BagAbstractUpdateElement>();
		set4.add(buc21);
		set4.add(buc22);
		set4.add(buc23);
		set4.add(composed4);
		
		updates2 = new BagUpdateContainer(set4);
		
	}

	/**
	 * Test method for {@link org.coreasm.engine.plugins.bag.BagUpdateContainer#aggregateUpdates(org.coreasm.engine.plugins.bag.BagElement)}.
	 */
	@Test
	public void testAggregateUpdates() {
		Multiset<Element> mset;
		
		mset = new HashMultiset<Element>();
		mset.add(strYou);
		mset.add(n43);
		mset.add(n43);
		mset.add(n43);
		mset.add(n52);
		BagElement result1 = new BagElement(mset);
		
		mset = new HashMultiset<Element>();
		mset.add(strYou);
		mset.add(n43);
		mset.add(n43);
		mset.add(n43);
		BagElement result2 = new BagElement(mset);
		
		mset = new HashMultiset<Element>();
		mset.add(n6);
		mset.add(n43);
		mset.add(n43);
		BagElement result3 = new BagElement(mset);
		
		mset = new HashMultiset<Element>();
		mset.add(strYou);
		mset.add(n43);
		mset.add(n43);
		mset.add(n43);
		mset.add(n1);
		mset.add(n6);
		BagElement result4 = new BagElement(mset);
		
		mset = new HashMultiset<Element>();
		mset.add(n52);
		mset.add(n43);
		mset.add(n43);
		mset.add(n43);
		mset.add(n43);
		mset.add(n1);
		mset.add(n1);
		mset.add(n6);
		BagElement result5 = new BagElement(mset);
		
		boolean output = false;

		if (output) {
			System.out.println(bag1);
			System.out.println(composed1);
			System.out.println(composed1.aggregateUpdates(bag1));
			System.out.println();
		}
		assertEquals(result1, composed1.aggregateUpdates(bag1));

		if (output) {
			System.out.println(bag1);
			System.out.println(composed2);
			System.out.println(composed2.aggregateUpdates(bag1));
			System.out.println();
		}
		assertEquals(result2, composed2.aggregateUpdates(bag1));
		
		
		if (output) {
			System.out.println(bag2);
			System.out.println(composed3);
			System.out.println(composed3.aggregateUpdates());
			System.out.println(composed3.aggregateUpdates(bag2));
			System.out.println();
		}
		assertEquals(result3, composed3.aggregateUpdates(bag2));
		
		if (output) {
			System.out.println(bag2);
			System.out.println(updates1);
			System.out.println(updates1.aggregateUpdates());
			System.out.println(updates1.aggregateUpdates(bag2));
			System.out.println();
		}
		assertEquals(result4, updates1.aggregateUpdates(bag2));

		if (output) {
			System.out.println(bag2);
			System.out.println(updates2);
			System.out.println(updates2.aggregateUpdates());
			System.out.println(updates2.aggregateUpdates(bag2));
		}
		assertEquals(result5, updates2.aggregateUpdates(bag2));
	}

}
