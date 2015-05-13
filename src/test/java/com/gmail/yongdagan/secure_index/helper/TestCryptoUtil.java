package com.gmail.yongdagan.secure_index.helper;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestCryptoUtil {
	
	@Test
	public void testBase64() throws Exception {
		String str = "djsabhfjkdsaf";
		String encode = CryptoUtil.encodeBASE64(str.getBytes());
		byte[] decode = CryptoUtil.decodeBASE64(encode);
		assertArrayEquals(str.getBytes(), decode);
	}
	
	@Test
	public void testHmac() throws Exception {
		String s1 = "abc";
		String s2 = "abca";
		byte[] key = CryptoUtil.initKey(CryptoUtil.MAC);
		byte[] hmac1 = CryptoUtil.generateHMAC(key, s1.getBytes());
		byte[] hmac2 = CryptoUtil.generateHMAC(key, s2.getBytes());
		assertArrayEquals(hmac1, CryptoUtil.generateHMAC(key, "abc".getBytes()));
		assertTrue(!CryptoUtil.encodeBASE64(hmac1).equals(CryptoUtil.encodeBASE64(hmac2)));
	}
	
	@Test
	public void testAes() throws Exception {
		byte[] data = "abac".getBytes();
		byte[] key = CryptoUtil.initKey(CryptoUtil.AES);
		byte[] c = CryptoUtil.encryptAES(key, data);
		byte[] m = CryptoUtil.decryptAES(key, c);
		assertArrayEquals(data, m);
	}
	
}
