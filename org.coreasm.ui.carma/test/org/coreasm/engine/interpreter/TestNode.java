/*	
 * TestNode.java 
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

package org.coreasm.engine.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Roozbeh Farahbod
 *
 */
public class TestNode {

	public Node youngParent;
	public Node oldParent1;
	public Node oldParent2;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		youngParent = new ASTNode("Young", "Young", "Young", "Young", null);
		oldParent1 = new ASTNode("Old1", "Old1", "Old1", "Old1", null);
		oldParent2 = new ASTNode("Old2", "Old2", "Old2", "Old2", null);
		oldParent1.addChild(new Node("child1", "child1", null));
		oldParent2.addChild(new Node("child2", "child2", null));
		oldParent2.addChild(new Node("child3", "child3", null));
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#addChild(org.coreasm.engine.interpreter.Node)}.
	 */
	@Test
	public void testAddChildNode() {
		int size = 0;
		
		size = youngParent.getNumberOfChildren();
		youngParent.addChild(new Node("", "", null));
		assertEquals(size+1, youngParent.getNumberOfChildren());

		size = oldParent1.getNumberOfChildren();
		oldParent1.addChild(new Node("", "", null));
		assertEquals(size+1, oldParent1.getNumberOfChildren());

		size = oldParent2.getNumberOfChildren();
		oldParent2.addChild(new Node("", "", null));
		assertEquals(size+1, oldParent2.getNumberOfChildren());
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#addChild(java.lang.String, org.coreasm.engine.interpreter.Node)}.
	 */
	@Test
	public void testAddChildStringNode() {
		Node childNode = new Node("", "", null);
		oldParent1.addChild("alpha", childNode);

		Node node = oldParent1.getChildNode("alpha");
		assertEquals(childNode, node);
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#addChildAfter(org.coreasm.engine.interpreter.Node, java.lang.String, org.coreasm.engine.interpreter.Node)}.
	 */
	@Test
	public void testAddChildAfter() {
		Node childNode = new Node("", "", null);
		oldParent2.addChildAfter(oldParent2.getChildNodes().get(0), "", childNode);
		assertEquals(childNode, oldParent2.getChildNodes().get(1));
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#getChildNodes()}.
	 */
	@Test
	public void testGetChildNodes() {
		assertEquals(2, oldParent2.getChildNodes().size());
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#getChildNodesWithNames()}.
	 */
	@Test
	public void testGetChildNodesWithNames() {
		Node childNode = new Node("", "", null);
		oldParent1.addChild("alpha", childNode);

		assertEquals("alpha", oldParent1.getChildNodesWithNames().get(1).name);
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#getChildNodes(java.lang.String)}.
	 */
	@Test
	public void testGetChildNodesString() {
		oldParent1.addChild("alpha", new Node("", "", null));
		oldParent1.addChild("alpha", new Node("", "", null));

		assertEquals(2, oldParent1.getChildNodes("alpha").size());
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#getChildNode(java.lang.String)}.
	 */
	@Test
	public void testGetChildNode() {
		Node childNode = new Node("", "", null);
		oldParent1.addChild("alpha", childNode);

		assertEquals(childNode, oldParent1.getChildNode("alpha"));
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#getNextCSTNode()}.
	 */
	@Test
	public void testGetNextCSTNode() {
		Node childNode1 = new Node("", "", null);
		oldParent2.addChild("alpha", childNode1);
		Node childNode2 = new Node("", "", null);
		oldParent2.addChild("beta", childNode2);
		
		assertEquals(childNode2, childNode1.getNextCSTNode());
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#getFirstCSTNode()}.
	 */
	@Test
	public void testGetFirstCSTNode() {
		assertNull(youngParent.getFirstCSTNode());
		
		Node childNode = new Node("", "", null);
		youngParent.addChild(childNode);
		assertEquals(childNode, youngParent.getFirstCSTNode());
	}

	/**
	 * Test method for {@link org.coreasm.engine.interpreter.Node#getNumberOfChildren()}.
	 */
	@Test
	public void testGetNumberOfChildren() {
		assertEquals(2, oldParent2.getNumberOfChildren());
	}

}
