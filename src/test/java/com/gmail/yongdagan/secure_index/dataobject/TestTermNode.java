package com.gmail.yongdagan.secure_index.dataobject;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTermNode {
	
	@Test
	public void testTermNode() {
		TermNode termNode = new TermNode();
		Integer docId = 1;
		termNode.addDoc(docId);
		termNode.addDoc(docId);
		termNode.addDoc(docId);
		docId = 2;
		termNode.addDoc(docId);
		assertEquals(2, termNode.getSize());
		termNode.print();
	}

}
