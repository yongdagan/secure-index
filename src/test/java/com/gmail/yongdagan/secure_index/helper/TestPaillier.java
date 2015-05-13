package com.gmail.yongdagan.secure_index.helper;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

public class TestPaillier {
	
	private Paillier paillier; 
	
	@Before
	public void init() {
		paillier = new Paillier(128);
		paillier.generateKeys();
	}
	
	private void initWithKeys() {
		String lambda = "3187683483552504903024294883062812100";
		String n = "114756605407890176530306453137552808873";
		String g = "3114265605670390667671057135866171145755229849645205709311336521560284077254";
		String mu = "51052647243578283707532005898376309457";
		paillier = new Paillier(128, n, g, lambda, mu);
	}
	
	@Test
	public void testGenerateKeys() {
		System.out.println("n:\t" + new BigInteger(paillier.getN()));
		System.out.println("g:\t" + new BigInteger(paillier.getG()));
		System.out.println("lambda:\t" + new BigInteger(paillier.getLambda()));
		System.out.println("mu:\t" + new BigInteger(paillier.getMu()));
	}
	
	@Test
	public void testConstructWithKeys() {
		initWithKeys();
		testGenerateKeys();
	}
	
	@Test
	public void testEncryptAndDecrypt() throws Exception {
		BigInteger m = new BigInteger("1243");
		BigInteger c = paillier.encrypt(m);
		BigInteger x = paillier.decrypt(c);
		assertEquals(m, x);
		
		c = new BigInteger(paillier.encrypt(m.toByteArray()));
		x = new BigInteger(paillier.decrypt(c.toByteArray()));
		assertEquals(m, x);
	}
	
	@Test
	public void testEncryptAndDecryptWithKeys() throws Exception {
		initWithKeys();
		testEncryptAndDecrypt();
	}
	
	@Test
	public void testAdd() throws Exception {
		BigInteger a = paillier.encrypt(new BigInteger("13"));
		BigInteger b = paillier.encrypt(new BigInteger("56"));
		BigInteger c = Paillier.add(a, b, paillier.getNsquare());
		assertEquals(69L, paillier.decrypt(c).longValue());
	}
	
	@Test
	public void testAddWithKeys() throws Exception {
		initWithKeys();
		testAdd();
	}

}
