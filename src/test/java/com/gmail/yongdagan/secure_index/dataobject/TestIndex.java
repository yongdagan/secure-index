package com.gmail.yongdagan.secure_index.dataobject;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestIndex {
	
	@Test
	public void testIndex() {
		Index index = new Index(0);
		String termName1 = "as";
		String termName2 = "to";
		index.addTerm(termName1);
		index.addTerm(termName1);
		index.addTerm(termName2);
		index.markDocId("xx.dat");
		index.addTerm(termName2);
		assertEquals(2, index.getSize());
		assertEquals(2, index.getDocNum());
		index.print();
	}

}
