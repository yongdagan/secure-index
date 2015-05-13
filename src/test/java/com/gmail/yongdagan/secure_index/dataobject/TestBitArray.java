package com.gmail.yongdagan.secure_index.dataobject;

import static junit.framework.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class TestBitArray {

//	@Test
	public void test() {
		String str = "101010101010100101010101010101010101010100101111010101010101010";
		BitArray bitArray = new BitArray();
		for(int i = 0; i < str.length(); i ++) {
			bitArray.push(str.charAt(i));
		}
		
		bitArray = new BitArray(bitArray.toByteArray());
		for(int i = 0; i < str.length(); i ++) {
			assertEquals(str.charAt(i), bitArray.next());
		}
		
		bitArray = new BitArray();
		bitArray.push(str);
		bitArray = new BitArray(bitArray.toByteArray());
		for(int i = 0; i < str.length(); i ++) {
			assertEquals(str.charAt(i), bitArray.next());
		}
	}
	
//	@Test
	public void testGammaCode() {
		String str = "010010111000110011101011011111000011100011110010";
		BitArray bitArray = new BitArray();
		// encode
		for(int i = 1; i <= 10; i ++) {
			String part2 = Integer.toBinaryString(i).substring(1);
			int len = part2.length();
			for(int j = 0; j < len; j ++) {
				bitArray.push('1');
			}
			bitArray.push('0');
			bitArray.push(part2);
		}
		
		bitArray = new BitArray(bitArray.toByteArray());
		for(int i = 0; i < str.length(); i ++) {
			char tmp = bitArray.next();
			assertEquals(str.charAt(i), tmp);
			System.out.print(tmp);
		}
		while(bitArray.hasNext()) {
			System.out.print(bitArray.next());
		}
		
		
		// decode
		ArrayList<Integer> list = new ArrayList<Integer>();
		int k = 0;
		while(bitArray.hasNext()) {
			char c = bitArray.next();
			if(c == '0') {
				StringBuffer buffer = new StringBuffer("1");
				for(int i = 0; i < k; i ++) {
					buffer.append(bitArray.next());
				}
				list.add(Integer.valueOf(buffer.toString(), 2));
			} else {
				k ++;
			}
		}
		for(int i = 0; i < list.size(); i ++) {
			assertEquals(i+1, list.get(i).intValue());
		}
	}
	
	@Test
	public void testAesBugIsExisted() {
		String str = "101010";
		BitArray bitArray = new BitArray();
		// encode
		for(int i = 0; i < str.length(); i ++) {
			bitArray.push(str.charAt(i));
		}
		byte[] bytes = bitArray.toByteArray();
		for(int i = 0; i < bytes.length; i ++) {
			System.out.println(bytes[i]);
		}
	}
	
}
