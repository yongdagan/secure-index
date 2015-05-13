package com.gmail.yongdagan.secure_index.dataobject;

import org.junit.Test;

public class TestPair {
	
	@Test
	public void testPair() {
		Pair<Integer, String> p = new Pair<Integer, String>(1, "s");
		System.out.println(p.getFirst() + ":" + p.getSecond());
		
		Pair<Integer, Long> q = new Pair<Integer, Long>(2, 3L);
		System.out.println(q.getFirst() + ":" + q.getSecond());
	}

}
